package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.exception.EmptyConversationException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.MetricCacheRepository;
import ai.teamcollab.server.repository.PointInTimeSummaryRepository;
import ai.teamcollab.server.service.SystemSettingsService;
import ai.teamcollab.server.service.domain.ChatContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

/**
 * Responsible for generating conversation summaries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryGenerator {

    /**
     * Record to hold the response text and token usage from an AI model call.
     */
    private record AiResponse(String text, int promptTokens, int completionTokens) {
    }

    private final PointInTimeSummaryRepository pointInTimeSummaryRepository;
    private final MessageRepository messageRepository;
    private final MetricCacheRepository metricCacheRepository;
    private final AiModelFactory aiModelFactory;
    private final PromptBuilder promptBuilder;
    private final SystemSettingsService systemSettingsService;

    /**
     * Generates a point-in-time summary for a conversation.
     *
     * @param conversation the conversation to summarize
     * @param chatContext  the chat context
     * @return a CompletableFuture containing the generated PointInTimeSummary
     */
    public CompletableFuture<PointInTimeSummary> generateSummary(Conversation conversation, ChatContext chatContext) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(chatContext, "ChatContext cannot be null");

        log.debug("Generating point-in-time summary for conversation: {}", conversation.getId());

        // Get the most recent message
        final var messages = chatContext.getLastMessages();
        if (messages.isEmpty()) {
            throw new EmptyConversationException("Cannot generate summary for conversation with no messages");
        }

        final var latestMessage = messages.getLast();

        // Check if we already have a recent summary
        final var existingSummary = pointInTimeSummaryRepository.findMostRecentActiveByConversation(conversation);

        return CompletableFuture.supplyAsync(() -> {
            try {
                long messageCount = 0;
                if (existingSummary.isPresent()) {
                    final var lastSummaryMessage = existingSummary.get().getLatestMessage();
                    messageCount = messageRepository.countMessagesAfter(
                            conversation.getId(),
                            lastSummaryMessage.getId()
                    );
                } else {
                    messageCount = conversation.getMessageCacheCounter();
                }
                // Get the summary batch size from system settings
                final var currentSettings = systemSettingsService.getCurrentSettings();
                final var summaryBatchSize = currentSettings.getSummaryBatchSize();

                // If fewer messages have been added than the batch size, return the existing summary
                if (messageCount < summaryBatchSize) {
                    log.debug("Using existing summary as only {} messages have been added since last summary (batch size: {})",
                            messageCount, summaryBatchSize);
                    return existingSummary.get();
                }


                log.debug("Generating new point-in-time summary");

                final var start = now();

                final var topicsPrompt = promptBuilder.buildTopicsPrompt(chatContext);
                final var topicSummariesPrompt = promptBuilder.buildTopicSummariesPrompt(chatContext);
                final var assistantSummariesPrompt = promptBuilder.buildAssistantSummariesPrompt(conversation, chatContext);

                final var chatModel = aiModelFactory.createSummaryModel();
                final var llmModel = aiModelFactory.getActiveLlmModel(conversation);

                final var topicsResponse = callAndGetResponse(chatModel, topicsPrompt);
                final var topicSummariesResponse = callAndGetResponse(chatModel, topicSummariesPrompt);
                final var assistantSummariesResponse = callAndGetResponse(chatModel, assistantSummariesPrompt);

                final var end = now();
                final var duration = Duration.between(start, end).toMillis();

                // Create and save the summary
                final var summary = PointInTimeSummary.create(
                        conversation,
                        latestMessage,
                        topicsResponse.text(),
                        topicSummariesResponse.text(),
                        assistantSummariesResponse.text()
                );

                // Calculate total tokens
                final var totalInputTokens = topicsResponse.promptTokens() +
                        topicSummariesResponse.promptTokens() +
                        assistantSummariesResponse.promptTokens();
                final var totalOutputTokens = topicsResponse.completionTokens() +
                        topicSummariesResponse.completionTokens() +
                        assistantSummariesResponse.completionTokens();
                final var summaryLlmModel = currentSettings.getSummaryLlmModel();
                // Create metrics
                final var metrics = Metrics.builder()
                        .duration(duration)
                        .inputTokens(totalInputTokens)
                        .outputTokens(totalOutputTokens)
                        .llmModel(summaryLlmModel)
                        .additionalInfo("Summary generation")
                        .build();

                // Associate metrics with summary
                summary.addMetrics(metrics);
                metricCacheRepository.updateMetricCache(conversation, metrics);
                return pointInTimeSummaryRepository.save(summary);

            } catch (EmptyConversationException e) {
                log.error("Empty conversation error while generating summary: {}", e.getMessage(), e);
                throw e;
            } catch (IllegalArgumentException e) {
                log.error("Invalid argument while generating summary: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Invalid input for summary generation", e);
            } catch (Exception e) {
                log.error("Error generating summary: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to generate summary: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Calls the AI model with a prompt and returns the response text and token usage.
     *
     * @param chatModel the AI model to call
     * @param prompt    the prompt to send
     * @return an AiResponse containing the response text and token usage
     */
    private static AiResponse callAndGetResponse(ChatModel chatModel, Prompt prompt) {
        var aiResponse = chatModel.call(prompt);
        var result = aiResponse.getResult();
        var output = result.getOutput();
        var metadata = aiResponse.getMetadata();
        var usage = metadata.getUsage();

        return new AiResponse(
                output.getText(),
                usage.getPromptTokens(),
                usage.getCompletionTokens()
        );
    }
}

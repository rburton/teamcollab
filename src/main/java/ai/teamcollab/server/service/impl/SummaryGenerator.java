package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.exception.EmptyConversationException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.PointInTimeSummaryRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

/**
 * Responsible for generating conversation summaries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryGenerator {
    public static final int SUMMARY_BATCH_SIZE = 10;
    
    private final PointInTimeSummaryRepository pointInTimeSummaryRepository;
    private final MessageRepository messageRepository;
    private final AiModelFactory aiModelFactory;
    private final PromptBuilder promptBuilder;
    
    /**
     * Generates a point-in-time summary for a conversation.
     *
     * @param conversation the conversation to summarize
     * @param chatContext the chat context
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
                // If we have an existing summary, check if we need a new one
                if (existingSummary.isPresent()) {
                    final var lastSummaryMessage = existingSummary.get().getLatestMessage();

                    // Count messages created after the last summary message
                    final var messageCount = messageRepository.countMessagesAfter(
                            conversation.getId(),
                            lastSummaryMessage.getId()
                    );

                    // If less than 10 messages have been added since the last summary, return the existing summary
                    if (messageCount < SUMMARY_BATCH_SIZE) {
                        log.debug("Using existing summary as only {} messages have been added since last summary", messageCount);
                        return existingSummary.get();
                    }
                }

                log.debug("Generating new point-in-time summary");

                // Create prompts for each part of the summary
                final var topicsPrompt = promptBuilder.buildTopicsPrompt(chatContext);
                final var topicSummariesPrompt = promptBuilder.buildTopicSummariesPrompt(chatContext);
                final var assistantSummariesPrompt = promptBuilder.buildAssistantSummariesPrompt(conversation, chatContext);

                // Get the AI model
                final var chatModel = aiModelFactory.createModel(conversation);

                // Generate each part of the summary
                final var topicsAndKeyPoints = callAndGetResponse(chatModel, topicsPrompt);
                final var topicSummaries = callAndGetResponse(chatModel, topicSummariesPrompt);
                final var assistantSummaries = callAndGetResponse(chatModel, assistantSummariesPrompt);

                // Create and save the summary
                final var summary = PointInTimeSummary.create(
                        conversation,
                        latestMessage,
                        topicsAndKeyPoints,
                        topicSummaries,
                        assistantSummaries
                );

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
     * Calls the AI model with a prompt and returns the response text.
     *
     * @param chatModel the AI model to call
     * @param prompt the prompt to send
     * @return the response text
     */
    private static String callAndGetResponse(OpenAiChatModel chatModel, Prompt prompt) {
        var aiResponse = chatModel.call(prompt);
        var result = aiResponse.getResult();
        var output = result.getOutput();
        return output.getText();
    }
}
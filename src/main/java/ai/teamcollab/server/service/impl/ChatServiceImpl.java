package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.GptModel;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.exception.EmptyConversationException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.PointInTimeSummaryRepository;
import ai.teamcollab.server.service.ChatService;
import ai.teamcollab.server.service.SystemSettingsService;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    public static final int SUMMARY_BATCH_SIZE = 10;
    public static final String USER = "User";
    public static final String ASSISTANT = "Assistant";
    private final MessageRepository messageRepository;
    private final SystemSettingsService systemSettingsService;
    private final PointInTimeSummaryRepository pointInTimeSummaryRepository;

    @Autowired
    public ChatServiceImpl(MessageRepository messageRepository,
                           SystemSettingsService systemSettingsService,
                           PointInTimeSummaryRepository pointInTimeSummaryRepository) {
        this.messageRepository = messageRepository;
        this.systemSettingsService = systemSettingsService;
        this.pointInTimeSummaryRepository = pointInTimeSummaryRepository;
    }

    @Override
    public CompletableFuture<MessageResponse> process(Conversation conversation, Message recent, ChatContext chatContext) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(recent, "Message cannot be null");
        requireNonNull(chatContext, "ChatContext cannot be null");

        log.debug("Asynchronously processing message for conversation: {}, message: {}", conversation.getId(), recent.getId());
        log.debug("Using chat context - Purpose: {}, Project Overview: {}, History Size: {}",
                chatContext.getPurpose(), chatContext.getProjectOverview(), chatContext.getLastMessages().size());

        final var start = now();

        return CompletableFuture.supplyAsync(() -> {
            try {
                final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

                // Add system message with context
                messages.add(new SystemMessage(String.format(
                        "Conversation Purpose: %s\nProject Overview: %s",
                        chatContext.getPurpose(),
                        chatContext.getProjectOverview()
                )));

                // Add conversation history
                for (final var historyMessage : chatContext.getLastMessages()) {
                    if (historyMessage.equals(recent)) {
                        continue; // Skip the current message
                    }
                    if (historyMessage.isAssistant()) {
                        messages.add(new AssistantMessage(historyMessage.getContent()));
                    } else {
                        messages.add(new UserMessage(historyMessage.getContent()));
                    }
                }

                messages.add(new UserMessage(recent.getContent()));

                log.debug("Conversation history size: {}", messages.size());
                final var prompt = new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));

                log.debug("Sending prompt to OpenAI: {}", prompt);

                final var currentSettings = systemSettingsService.getCurrentSettings();

                final var llmModel = Optional.ofNullable(conversation)
                        .map(Conversation::getUser)
                        .map(User::getCompany)
                        .map(Company::getLlmModel)
                        .filter(Strings::isNotBlank)
                        .orElse(currentSettings.getLlmModel());

                final var model = GptModel.fromId(llmModel);
                final var chatModel = OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .apiKey(System.getenv("OPENAI_API_KEY"))
                                .build())
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(model.getId())
                                .temperature(model.getTemperature())
                                .build())
                        .build();
                final var aiResponse = chatModel.call(prompt);
                final var assistantMessage = aiResponse.getResult().getOutput();
                final var response = assistantMessage.getText();

                final var end = now();
                final var duration = Duration.between(start, end).toMillis();

                final var metadata = aiResponse.getMetadata();
                final var usage = metadata.getUsage();

                final var metrics = Metrics.builder()
                        .duration(duration)
                        .inputTokens(usage.getPromptTokens())
                        .outputTokens(usage.getCompletionTokens())
                        .provider("OpenAI")
                        .model(llmModel)
                        .build();

                recent.addMetrics(metrics);
                messageRepository.save(recent);

                log.debug("Received response from OpenAI: {}", response);

                return MessageResponse.builder()
                        .content(response)
                        .metrics(metrics)
                        .build();

            } catch (IllegalArgumentException e) {
                log.error("Invalid argument while processing message: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Invalid input for message processing", e);
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process message: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Generates a point-in-time summary for a conversation. The summary includes: - A list of topics and key points
     * talked about - A summary of each topic and key point with critical information - A summary for each assistant in
     * the conversation
     *
     * @param conversation the conversation to summarize
     * @param chatContext  the context of the conversation
     * @return a CompletableFuture containing the generated PointInTimeSummary
     */
    @Override
    public CompletableFuture<PointInTimeSummary> generatePointInTimeSummary(Conversation conversation, ChatContext chatContext) {
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
                final var topicsPrompt = createTopicsPrompt(chatContext);
                final var topicSummariesPrompt = createTopicSummariesPrompt(chatContext);
                final var assistantSummariesPrompt = createAssistantSummariesPrompt(conversation, chatContext);

                // Get the current settings and model
                final var currentSettings = systemSettingsService.getCurrentSettings();
                final var llmModel = Optional.ofNullable(conversation)
                        .map(Conversation::getUser)
                        .map(User::getCompany)
                        .map(Company::getLlmModel)
                        .filter(Strings::isNotBlank)
                        .orElse(currentSettings.getLlmModel());

                final var model = GptModel.fromId(llmModel);
                final var chatModel = OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .apiKey(System.getenv("OPENAI_API_KEY"))
                                .build())
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(model.getId())
                                .temperature(model.getTemperature()) // Lower temperature for more focused responses
                                .build())
                        .build();

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

    private static String callAndGetResponse(OpenAiChatModel chatModel, Prompt topicsPrompt) {
        var aiResponse = chatModel.call(topicsPrompt);
        var result = aiResponse.getResult();
        var output = result.getOutput();
        return output.getText();
    }

    /**
     * Creates a prompt to extract topics and key points from the conversation.
     */
    private Prompt createTopicsPrompt(ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at analyzing conversations and identifying the main topics and key points discussed. " +
                        "Extract a list of topics and key points from the conversation. " +
                        "Format your response as a bulleted list with main topics as headers and key points as sub-bullets. " +
                        "Be comprehensive but concise."
        ));

        // Add context
        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\nPlease analyze the following conversation and extract the main topics and key points:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview()
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = message.isAssistant() ? isAssistant(message) : isUser(message);

            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }

    private static String isUser(Message message) {
        return !message.isAssistant() ? message.getUser().getUsername() : USER;
    }

    private static String isAssistant(Message message) {
        return message.isAssistant() ? message.getAssistant().getName() : ASSISTANT;
    }

    /**
     * Creates a prompt to generate summaries for each topic and key point.
     */
    private Prompt createTopicSummariesPrompt(ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at summarizing complex discussions. " +
                        "For each topic and key point in the conversation, provide a concise summary that captures the critical information. " +
                        "Focus on information that would be important for continuing the conversation. " +
                        "Format your response with topic headers and summaries as paragraphs."
        ));

        // Add context
        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\nPlease summarize the following conversation by topic:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview()
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = message.isAssistant() ?
                    (message.getAssistant() != null ? message.getAssistant().getName() : "Assistant") :
                    (message.getUser() != null ? message.getUser().getUsername() : "User");

            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }

    /**
     * Creates a prompt to generate summaries for each assistant in the conversation.
     */
    private Prompt createAssistantSummariesPrompt(Conversation conversation, ChatContext chatContext) {
        final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

        // Add system message with instructions
        messages.add(new SystemMessage(
                "You are an expert at analyzing conversations and understanding the role of different participants. " +
                        "For each assistant in the conversation, provide a summary of their contributions and the critical points related to their expertise. " +
                        "Format your response with assistant names as headers and summaries as paragraphs."
        ));

        // Add context about assistants
        final var assistantsInfo = new StringBuilder("Assistants in this conversation:\n");
        for (final var assistant : conversation.getAssistants()) {
            assistantsInfo.append(String.format("- %s: %s\n", assistant.getName(), assistant.getExpertise()));
        }

        messages.add(new UserMessage(String.format(
                "Conversation Purpose: %s\nProject Overview: %s\n\n%s\n\nPlease summarize the contributions of each assistant:",
                chatContext.getPurpose(),
                chatContext.getProjectOverview(),
                assistantsInfo
        )));

        // Add conversation history
        for (final var message : chatContext.getLastMessages()) {
            final var sender = message.isAssistant() ? isAssistant(message) : isUser(message);

            messages.add(new UserMessage(String.format("%s: %s", sender, message.getContent())));
        }

        return new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));
    }
}

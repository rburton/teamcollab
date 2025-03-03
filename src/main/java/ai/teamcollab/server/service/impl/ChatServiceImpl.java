package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.service.ChatService;
import ai.teamcollab.server.service.SystemSettingsService;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final SystemSettingsService systemSettingsService;

    @Autowired
    public ChatServiceImpl(MessageRepository messageRepository, SystemSettingsService systemSettingsService) {
        this.messageRepository = messageRepository;
        this.systemSettingsService = systemSettingsService;
    }

    @Override
    public MessageResponse process(Conversation conversation, Message recent) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(recent, "Message cannot be null");

        log.debug("Processing message for conversation: {}, message: {}", conversation.getId(), recent.getId());

        final var start = now();

        try {
            final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();
            for (Message historyMessage : conversation.getMessages()) {
                if (historyMessage.equals(recent)) {
                    continue; // Skip the current message
                }
                if (Objects.nonNull(historyMessage.getUser())) {
                    messages.add(new UserMessage(historyMessage.getContent()));
                } else {
                    messages.add(new AssistantMessage(historyMessage.getContent()));
                }
            }

            messages.add(new UserMessage(recent.getContent()));

            log.debug("Conversation history size: {}", messages.size());
            Prompt prompt = new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));

            log.debug("Sending prompt to OpenAI: {}", prompt);

            final var currentSettings = systemSettingsService.getCurrentSettings();
            final var chatModel = OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .apiKey(System.getenv("OPENAI_API_KEY"))
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(currentSettings.getLlmModel())
                            .temperature(0.7)
                            .build())
                    .build();
            final var aiResponse = chatModel.call(prompt);
            final var assistantMessage = aiResponse.getResult().getOutput();
            final var response = assistantMessage.getText();

            final var end = now();
            long duration = Duration.between(start, end).toMillis();

            final var metadata = aiResponse.getMetadata();
            final var usage = metadata.getUsage();

            final var metrics = Metrics.builder()
                    .duration(duration)
                    .inputTokens(usage.getPromptTokens())
                    .outputTokens(usage.getCompletionTokens())
                    .provider("OpenAI")
                    .model(currentSettings.getLlmModel())
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
    }

}

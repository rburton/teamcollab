package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.service.ChatService;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;

    @Autowired
    public ChatServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public MessageResponse process(Conversation conversation, Message recent) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(recent, "Message cannot be null");

        log.debug("Processing message for conversation: {}, message: {}", conversation.getId(), recent.getId());

        final var start = now();

        try {
            // TODO: Implement actual LLM processing logic here
            final var response = "Sample response"; // Placeholder

            final var end = now();
            long duration = Duration.between(start, end).toMillis();

            final var metrics = Metrics.builder()
                    .duration(duration)
                    .inputTokens(100)  // Placeholder
                    .outputTokens(50)   // Placeholder
                    .provider("OpenAI") // Placeholder
                    .model("gpt-3.5-turbo") // Placeholder
                    .build();

            recent.setMetrics(metrics);
            messageRepository.save(recent);
            return MessageResponse.builder()
                    .content(response)
                    .metrics(metrics)
                    .build();

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process message", e);
        }
    }

}
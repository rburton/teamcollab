package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ConversationService {

    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ChatService chatService, MessageService messageService, UserRepository userRepository, MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public Conversation createConversation(Conversation conversation, Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        conversation.setUser(user);
        conversation.setCreatedAt(java.time.LocalDateTime.now());

        return conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + id));
    }

    @Async
    public CompletableFuture<Void> sendMessage(Long conversationId, Message message, User user) {
        log.debug("Asynchronously sending message for conversation: {}, user: {}", conversationId, user.getId());
        try {
            final var conversation = findConversationById(conversationId);
            final var savedMessage = messageService.createMessage(message, conversationId, user);
            return chatService.process(conversation, savedMessage)
                    .thenAccept(response -> log.debug("Message processed successfully for conversation: {}", conversationId))
                    .exceptionally(throwable -> {
                        log.error("Error processing message for conversation {}: {}", conversationId, throwable.getMessage(), throwable);
                        // Convert checked exception to unchecked
                        if (throwable instanceof RuntimeException) {
                            throw (RuntimeException) throwable;
                        }
                        throw new RuntimeException("Failed to process message", throwable);
                    });
        } catch (Exception e) {
            log.error("Error sending message for conversation {}: {}", conversationId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public List<Message> findMessagesByConversation(Long conversationId) {
        log.debug("Fetching messages for conversation {}", conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Message> getUserMessages(Long userId) {
        log.debug("Fetching messages for user {}", userId);
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

}

package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageRow;
import ai.teamcollab.server.templates.TemplatePath;
import ai.teamcollab.server.templates.TemplateVariableName;
import ai.teamcollab.server.templates.ThymeleafTemplateRender;
import ai.teamcollab.server.ws.domain.WsMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ai.teamcollab.server.controller.WebSocketController.DIRECT_MESSAGE_TOPIC;
import static ai.teamcollab.server.templates.TemplatePath.CONVERSATION_MESSAGE_TEMPLATE;
import static ai.teamcollab.server.templates.TemplateVariableName.MESSAGE;
import static java.util.Collections.reverse;

@Slf4j
@Service
public class ConversationService {

    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThymeleafTemplateRender thymeleafTemplateRender;

    @Autowired
    public ConversationService(ChatService chatService, MessageService messageService, UserRepository userRepository,
                               MessageRepository messageRepository, ConversationRepository conversationRepository,
                               SimpMessagingTemplate messagingTemplate, ThymeleafTemplateRender thymeleafTemplateRender) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
        this.thymeleafTemplateRender = thymeleafTemplateRender;
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

    public Message addToConversation(Long conversationId, Message message, User user) {
        log.debug("Asynchronously sending message for conversation: {}, user: {}", conversationId, user.getId());
        return messageService.createMessage(message, conversationId, user);
    }

    private ChatContext buildChatContext(Conversation conversation, String sessionId) {
        final var lastMessages = findMessagesByConversation(conversation.getId());

        return ChatContext.builder()
                .purpose(conversation.getPurpose())
                .projectOverview(conversation.getProject().getOverview())
                .lastMessages(lastMessages)
                .build();
    }

    @Async
    public CompletableFuture<Void> sendMessage(Long messageId, String sessionId) {
        try {
            final var message = messageRepository.findById(messageId).orElseThrow();
            final var conversation = message.getConversation();
            final var chatContext = buildChatContext(conversation, sessionId);

            return chatService.process(conversation, message, chatContext)
                    .thenAccept(response -> {
                        final var responseMessage = Message.builder()
                                .content(response.getContent())
                                .persona(Persona.builder()
                                        .name("System")
                                        .build())
                                .createdAt(LocalDateTime.now())
                                .build();
                        final var row = MessageRow.from(responseMessage);
                        final var html = thymeleafTemplateRender.renderToHtml(CONVERSATION_MESSAGE_TEMPLATE, Map.of(MESSAGE, row));
                        messagingTemplate.convertAndSendToUser(sessionId, DIRECT_MESSAGE_TOPIC, WsMessageResponse.turbo(html));
                        log.debug("Message processed successfully for conversation: {}", message.getId());
                    })
                    .exceptionally(throwable -> {
                        log.error("Error processing message for conversation {}: {}", conversation.getId(), throwable.getMessage(), throwable);
                        // Convert checked exception to unchecked
                        if (throwable instanceof RuntimeException) {
                            throw (RuntimeException) throwable;
                        }
                        throw new RuntimeException("Failed to process message", throwable);
                    });
        } catch (Exception e) {
            log.error("Error sending message for conversation {}: {}", messageId, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    public List<Message> findMessagesByConversation(Long conversationId) {
        log.debug("Fetching messages for conversation {}", conversationId);
        final var messages = messageRepository.findTop10ByConversationIdOrderByCreatedAtDesc(conversationId);
        reverse(messages);
        return messages;
    }

    public List<Message> getUserMessages(Long userId) {
        log.debug("Fetching messages for user {}", userId);
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

}

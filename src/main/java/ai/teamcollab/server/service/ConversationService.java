package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.PointInTimeSummary;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageRow;
import ai.teamcollab.server.templates.ThymeleafTemplateRender;
import ai.teamcollab.server.ws.domain.WsMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ai.teamcollab.server.controller.WebSocketController.DIRECT_MESSAGE_TOPIC;
import static ai.teamcollab.server.templates.TemplatePath.ASSISTANT_STATUSES_TEMPLATE;
import static ai.teamcollab.server.templates.TemplatePath.CONVERSATION_MESSAGE_TEMPLATE;
import static ai.teamcollab.server.templates.TemplateVariableName.ASSISTANTS;
import static ai.teamcollab.server.templates.TemplateVariableName.MESSAGE;
import static ai.teamcollab.server.templates.TemplateVariableName.STATUS;
import static java.util.Collections.reverse;
import static java.util.Objects.nonNull;

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
    private final AuditService auditService;

    @Autowired
    public ConversationService(ChatService chatService, MessageService messageService, UserRepository userRepository,
                               MessageRepository messageRepository, ConversationRepository conversationRepository,
                               SimpMessagingTemplate messagingTemplate, ThymeleafTemplateRender thymeleafTemplateRender,
                               AuditService auditService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
        this.thymeleafTemplateRender = thymeleafTemplateRender;
        this.auditService = auditService;
    }

    public Conversation createConversation(Conversation conversation, Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        conversation.setUser(user);
        conversation.setCreatedAt(java.time.LocalDateTime.now());

        final var savedConversation = conversationRepository.save(conversation);

        // Create audit event for conversation creation
        auditService.createAuditEvent(
            Audit.AuditActionType.CONVERSATION_CREATED,
            user,
            "Conversation created with purpose: " + conversation.getPurpose(),
            "Conversation",
            savedConversation.getId()
        );

        return savedConversation;
    }

    /**
     * Gets conversations for a user, including all conversations owned by the user
     * and all non-private conversations created by other users.
     *
     * @param userId the ID of the user
     * @return a list of conversations accessible to the user
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    public List<Conversation> getUserConversations(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return conversationRepository.findByUserIdOrIsPrivateFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Gets only the conversations owned by a user.
     *
     * @param userId the ID of the user
     * @return a list of conversations owned by the user
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    public List<Conversation> getUserOwnedConversations(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Finds a conversation by ID.
     *
     * @param id the ID of the conversation to find
     * @return the conversation
     * @throws IllegalArgumentException if no conversation is found with the given ID
     */
    public Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + id));
    }

    /**
     * Finds a conversation by ID with its assistants eagerly fetched.
     *
     * @param id the ID of the conversation to find
     * @return the conversation with eagerly fetched assistants
     * @throws IllegalArgumentException if no conversation is found with the given ID
     */
    public Conversation findConversationByIdWithAssistant(Long id) {
        return conversationRepository.findByIdWithAssistant(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + id));
    }

    public Message addToConversation(Long conversationId, Message message, Long userId) {
        final var user = userRepository.findById(userId).orElseThrow();
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
            final var messageConversation = message.getConversation();
            final var conversation = conversationRepository.findByIdWithAssistant(messageConversation.getId())
                    .orElseThrow();
            final var chatContext = buildChatContext(conversation, sessionId);

            chatService.generatePointInTimeSummary(conversation, chatContext);

            return chatService.process(conversation, message, chatContext)
                    .thenAccept(response -> {
                        final var responseMessage = Message.builder()
                                .content(response.getContent())
                                .assistant(message.getAssistant())
                                .createdAt(LocalDateTime.now())
                                .build();

                        // Save the response message to the conversation
                        final var savedMessage = messageService.createMessage(responseMessage, conversation.getId(), message.getUser());
                        if (nonNull(response.getMetrics())) {
                            savedMessage.addMetrics(response.getMetrics());
                            messageRepository.save(savedMessage);
                        }

                        final var row = MessageRow.from(responseMessage);
                        final var html = thymeleafTemplateRender.renderToHtml(CONVERSATION_MESSAGE_TEMPLATE, Map.of(MESSAGE, row));
                        final var assistants = conversation.getAssistants();
                        final var htmlStatus = thymeleafTemplateRender.renderToHtml(ASSISTANT_STATUSES_TEMPLATE, Map.of(ASSISTANTS, assistants, STATUS, "Ready"));

                        messagingTemplate.convertAndSendToUser(sessionId, DIRECT_MESSAGE_TOPIC, WsMessageResponse.turbo(List.of(html, htmlStatus)));
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
        final var messages = messageRepository.findTop10ByConversationIdAndDeletedFalseOrderByCreatedAtDesc(conversationId);
        reverse(messages);
        return messages;
    }

    public List<Message> getUserMessages(Long userId) {
        log.debug("Fetching messages for user {}", userId);
        return messageRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Resets a conversation by soft deleting all messages and generating a point-in-time summary.
     *
     * @param conversationId the ID of the conversation to reset
     * @param userId the ID of the user performing the reset
     * @return the generated point-in-time summary
     * @throws IllegalArgumentException if the conversation is not found or the user is not authorized
     */
    @Transactional
    public CompletableFuture<PointInTimeSummary> resetConversation(Long conversationId, Long userId) {
        log.debug("Resetting conversation {} for user {}", conversationId, userId);

        // Find the conversation
        final var conversation = findConversationByIdWithAssistant(conversationId);

        // Check if the user is authorized to reset the conversation
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!user.equals(conversation.getUser())) {
            log.warn("Unauthorized attempt to reset conversation {} by user {}", conversationId, userId);
            throw new IllegalArgumentException("User is not authorized to reset this conversation");
        }

        // Build chat context before deleting messages
        final var chatContext = buildChatContext(conversation, userId.toString());

        // Generate a point-in-time summary before deleting messages
        final var summaryFuture = chatService.generatePointInTimeSummary(conversation, chatContext);

        // Soft delete all messages in the conversation
        final var deletedCount = messageService.softDeleteAllMessagesInConversation(conversationId);
        log.debug("Soft deleted {} messages in conversation {}", deletedCount, conversationId);

        // Create audit event for conversation reset
        auditService.createAuditEvent(
            Audit.AuditActionType.CONVERSATION_RESET,
            user,
            "Conversation reset with " + deletedCount + " messages soft deleted",
            "Conversation",
            conversationId
        );

        return summaryFuture;
    }

    /**
     * Sets the privacy status of a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param userId the ID of the user attempting to change the privacy status
     * @param isPrivate true to make the conversation private, false to make it public
     * @return the updated conversation
     * @throws IllegalArgumentException if the conversation is not found or the user is not authorized
     */
    @Transactional
    public Conversation setConversationPrivacy(Long conversationId, Long userId, boolean isPrivate) {
        log.debug("Setting privacy for conversation {} to {} by user {}", conversationId, isPrivate, userId);

        // Find the conversation
        final var conversation = findConversationById(conversationId);

        // Check if the user is authorized to change the privacy status
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (!user.equals(conversation.getUser())) {
            log.warn("Unauthorized attempt to change privacy of conversation {} by user {}", conversationId, userId);
            throw new IllegalArgumentException("User is not authorized to change the privacy of this conversation");
        }

        // Set the privacy status
        conversation.setPrivate(isPrivate);

        // Save the conversation
        final var savedConversation = conversationRepository.save(conversation);

        // Create audit event for privacy change
        auditService.createAuditEvent(
            isPrivate ? Audit.AuditActionType.CONVERSATION_MADE_PRIVATE : Audit.AuditActionType.CONVERSATION_MADE_PUBLIC,
            user,
            "Conversation privacy changed to " + (isPrivate ? "private" : "public"),
            "Conversation",
            conversationId
        );

        return savedConversation;
    }

    /**
     * Checks if a user can access a conversation.
     * A user can access a conversation if they own it or if it's not private.
     *
     * @param conversationId the ID of the conversation
     * @param userId the ID of the user
     * @return true if the user can access the conversation, false otherwise
     */
    public boolean canUserAccessConversation(Long conversationId, Long userId) {
        final var conversation = findConversationById(conversationId);
        return conversation.getUser().getId().equals(userId) || !conversation.isPrivate();
    }
}

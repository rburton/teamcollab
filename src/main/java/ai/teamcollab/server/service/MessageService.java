package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Log4j2
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageService(MessageRepository messageRepository,
                         ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Message createMessage(Message message, Long conversationId, User user) {
        requireNonNull(message, "Message cannot be null");
        requireNonNull(user, "User cannot be null");

        log.debug("Creating message for conversation {} by user {}", conversationId, user.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + conversationId));

        if (!user.equals(conversation.getUser())) {
            log.warn("Unauthorized attempt to post message in conversation {} by user {}", conversationId, user.getId());
            throw new IllegalArgumentException("User is not authorized to post in this conversation");
        }

        message.setConversation(conversation);
        message.setUser(user);
        message.setCreatedAt(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);
        log.debug("Successfully created message {} in conversation {}", savedMessage.getId(), conversationId);

        return savedMessage;
    }

    public List<Message> getConversationMessages(Long conversationId) {
        log.debug("Fetching messages for conversation {}", conversationId);
        return messageRepository.findByUserIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Message> getUserMessages(Long userId) {
        log.debug("Fetching messages for user {}", userId);
        return messageRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}

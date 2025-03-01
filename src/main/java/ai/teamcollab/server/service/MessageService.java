package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Message createMessage(Message message, Long conversationId, User user) {
        requireNonNull(message, "Message cannot be null");
        requireNonNull(user, "User cannot be null");

        log.debug("Creating message for conversation {} by user {}", conversationId, user.getId());

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with id: " + conversationId));

        if (!user.equals(conversation.getUser())) {
            log.warn("Unauthorized attempt to post message in conversation {} by user {}", conversationId, user.getId());
            throw new IllegalArgumentException("User is not authorized to post in this conversation");
        }

        message.setConversation(conversation);
        message.setUser(user);
        message.setCreatedAt(LocalDateTime.now());

        final var savedMessage = messageRepository.save(message);
        log.debug("Successfully created message {} in conversation {}", savedMessage.getId(), conversationId);

        return savedMessage;
    }

}

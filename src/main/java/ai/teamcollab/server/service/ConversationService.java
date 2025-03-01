package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ConversationService {

    private final ChatService chatService;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
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

    @Transactional
    public void sendMessage(Long conversationId, Message message, User user) {
        final var conversation = findConversationById(conversationId);
        final var savedMessage = messageService.createMessage(message, conversationId, user);
        chatService.process(conversation, savedMessage);
    }

    public List<Message> findMessagesByConversation(Long conversationId) {
        log.debug("Fetching messages for conversation {}", conversationId);
        return messageRepository.findByUserIdOrderByCreatedAtAsc(conversationId);
    }

    public List<Message> getUserMessages(Long userId) {
        log.debug("Fetching messages for user {}", userId);
        return messageRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
}

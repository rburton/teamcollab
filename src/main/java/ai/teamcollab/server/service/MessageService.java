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
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final BookmarkService bookmarkService;

    @Autowired
    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository, BookmarkService bookmarkService) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.bookmarkService = bookmarkService;
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

    /**
     * Returns a collection of message IDs that have been bookmarked by the specified user.
     *
     * @param messageIds the collection of message IDs to check
     * @param userId the ID of the user
     * @return a collection of message IDs that have been bookmarked by the user
     */
    @Transactional(readOnly = true)
    public Set<Long> getBookmarkedMessageIds(Collection<Long> messageIds, Long userId) {
        requireNonNull(messageIds, "Message IDs cannot be null");
        requireNonNull(userId, "User ID cannot be null");

        log.debug("Checking bookmarked messages for user {} among {} message IDs", userId, messageIds.size());

        // Get all bookmarked messages for the user
        final var bookmarkedMessages = bookmarkService.getBookmarkedMessages(userId);

        // Filter the bookmarked messages to only include those in the provided collection of message IDs
        return bookmarkedMessages.stream()
                .map(Message::getId)
                .filter(messageIds::contains)
                .collect(Collectors.toSet());
    }

}

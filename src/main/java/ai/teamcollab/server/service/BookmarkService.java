package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Bookmark;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.repository.BookmarkRepository;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.UserRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookmarkService(
            @NonNull BookmarkRepository bookmarkRepository,
            @NonNull MessageRepository messageRepository,
            @NonNull UserRepository userRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Bookmark bookmarkMessage(Long userId, Long messageId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        final var message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (bookmarkRepository.existsByUserIdAndMessageId(userId, messageId)) {
            throw new IllegalArgumentException("Message already bookmarked");
        }

        final var bookmark = new Bookmark(user, message);
        return bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void unbookmarkMessage(Long userId, Long messageId) {
        if (!bookmarkRepository.existsByUserIdAndMessageId(userId, messageId)) {
            throw new IllegalArgumentException("Bookmark not found");
        }
        bookmarkRepository.deleteByUserIdAndMessageId(userId, messageId);
    }

    @Transactional(readOnly = true)
    public List<Message> getBookmarkedMessages(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Bookmark::getMessage)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isMessageBookmarked(Long userId, Long messageId) {
        return bookmarkRepository.existsByUserIdAndMessageId(userId, messageId);
    }
}
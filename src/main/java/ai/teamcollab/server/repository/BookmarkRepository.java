package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Bookmark;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends CrudRepository<Bookmark, Long> {
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Bookmark> findByUserIdAndMessageId(Long userId, Long messageId);
    boolean existsByUserIdAndMessageId(Long userId, Long messageId);
    void deleteByUserIdAndMessageId(Long userId, Long messageId);
}
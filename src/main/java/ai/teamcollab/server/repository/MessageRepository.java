package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(Long conversationId);
    List<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(Long conversationId);
    List<Message> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);
    List<Message> findByUserIdAndDeletedFalseOrderByCreatedAtAsc(Long userId);
    List<Message> findTop10ByConversationIdAndDeletedFalseOrderByCreatedAtDesc(Long conversationId);

    /**
     * Count messages in a conversation that were created after a specific message.
     * This is used to determine if enough new messages have been added since the last summary.
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID to count from
     * @return the number of messages created after the specified message
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.deleted = false AND m.createdAt > (SELECT m2.createdAt FROM Message m2 WHERE m2.id = :messageId)")
    int countMessagesAfter(@Param("conversationId") Long conversationId, @Param("messageId") Long messageId);

    /**
     * Soft delete all messages in a conversation.
     *
     * @param conversationId the conversation ID
     * @param deletedAt the timestamp when the messages were deleted
     * @return the number of messages that were soft deleted
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.deleted = true, m.deletedAt = :deletedAt WHERE m.conversation.id = :conversationId AND m.deleted = false")
    int softDeleteAllByConversationId(@Param("conversationId") Long conversationId, @Param("deletedAt") LocalDateTime deletedAt);
}

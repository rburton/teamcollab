package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Message;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Message> findByUserIdOrderByCreatedAtAsc(Long userId);
    List<Message> findTop10ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    /**
     * Count messages in a conversation that were created after a specific message.
     * This is used to determine if enough new messages have been added since the last summary.
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID to count from
     * @return the number of messages created after the specified message
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.createdAt > (SELECT m2.createdAt FROM Message m2 WHERE m2.id = :messageId)")
    int countMessagesAfter(@Param("conversationId") Long conversationId, @Param("messageId") Long messageId);
}

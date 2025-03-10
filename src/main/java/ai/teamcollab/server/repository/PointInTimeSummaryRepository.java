package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.PointInTimeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing PointInTimeSummary entities.
 */
@Repository
public interface PointInTimeSummaryRepository extends JpaRepository<PointInTimeSummary, Long> {

    /**
     * Find the most recent active summary for a conversation.
     *
     * @param conversation the conversation
     * @return the most recent active summary, if any
     */
    @Query("SELECT p FROM PointInTimeSummary p WHERE p.conversation = :conversation AND p.isActive = true ORDER BY p.createdAt DESC")
    Optional<PointInTimeSummary> findMostRecentActiveByConversation(@Param("conversation") Conversation conversation);

    /**
     * Find all summaries for a conversation, ordered by creation time (newest first).
     *
     * @param conversation the conversation
     * @return list of summaries
     */
    List<PointInTimeSummary> findByConversationOrderByCreatedAtDesc(Conversation conversation);

    /**
     * Find all active summaries for a conversation, ordered by creation time (newest first).
     *
     * @param conversation the conversation
     * @return list of active summaries
     */
    List<PointInTimeSummary> findByConversationAndIsActiveTrueOrderByCreatedAtDesc(Conversation conversation);
}
package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.MetricCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetricCacheRepository extends JpaRepository<MetricCache, Long> {

    /**
     * Finds a MetricCache by conversation ID, provider, and model.
     *
     * @param conversationId the ID of the conversation
     * @param provider the provider of the AI model
     * @param model the specific AI model used
     * @return an Optional containing the MetricCache if found, or empty if not found
     */
    @Query("SELECT mc FROM MetricCache mc WHERE mc.conversation.id = :conversationId AND mc.provider = :provider AND mc.model = :model")
    Optional<MetricCache> findByConversationAndProviderAndModel(
            @Param("conversationId") Long conversationId,
            @Param("provider") String provider,
            @Param("model") String model);

    /**
     * Updates the metrics in a MetricCache using SQL.
     * This method avoids race conditions by performing the update directly in the database.
     *
     * @param metricCacheId the ID of the MetricCache to update
     * @param duration the duration to add to the total duration
     * @param inputTokens the input tokens to add to the total input tokens
     * @param outputTokens the output tokens to add to the total output tokens
     * @return the number of rows affected (should be 1 if successful)
     */
    @Modifying
    @Transactional
    @Query("UPDATE MetricCache mc SET " +
           "mc.totalDuration = mc.totalDuration + :duration, " +
           "mc.messageCount = mc.messageCount + 1, " +
           "mc.totalInputTokens = mc.totalInputTokens + :inputTokens, " +
           "mc.totalOutputTokens = mc.totalOutputTokens + :outputTokens, " +
           "mc.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE mc.id = :metricCacheId")
    int incrementMetricsById(
            @Param("metricCacheId") Long metricCacheId,
            @Param("duration") long duration,
            @Param("inputTokens") int inputTokens,
            @Param("outputTokens") int outputTokens);

    /**
     * Finds all MetricCache entries for a specific conversation.
     *
     * @param conversationId the ID of the conversation
     * @return a list of MetricCache entries for the conversation
     */
    @Query("SELECT mc FROM MetricCache mc WHERE mc.conversation.id = :conversationId")
    List<MetricCache> findByConversationId(@Param("conversationId") Long conversationId);
}

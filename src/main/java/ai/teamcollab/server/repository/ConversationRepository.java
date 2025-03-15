package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Conversation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends CrudRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds all conversations that are either owned by the specified user or are not private.
     * 
     * @param userId the ID of the user
     * @return a list of conversations that are either owned by the user or are not private
     */
    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId OR c.isPrivate = false ORDER BY c.createdAt DESC")
    List<Conversation> findByUserIdOrIsPrivateFalseOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Finds a conversation by ID and eagerly fetches its assistants.
     * 
     * @param id the ID of the conversation to find
     * @return the conversation with eagerly fetched assistants, or empty if not found
     */
    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.conversationAssistants ca LEFT JOIN FETCH ca.assistant WHERE c.id = :id")
    Optional<Conversation> findByIdWithAssistant(@Param("id") Long id);

    /**
     * Increments the message cache counter for a conversation in a thread-safe manner.
     * 
     * @param id the ID of the conversation
     * @return the number of rows affected (should be 1 if successful)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Conversation c SET c.messageCacheCounter = c.messageCacheCounter + 1 WHERE c.id = :id")
    int incrementMessageCacheCounter(@Param("id") Long id);
}

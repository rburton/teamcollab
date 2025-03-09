package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Conversation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends CrudRepository<Conversation, Long> {
    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Finds a conversation by ID and eagerly fetches its assistants.
     * 
     * @param id the ID of the conversation to find
     * @return the conversation with eagerly fetched assistants, or empty if not found
     */
    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.assistants WHERE c.id = :id")
    Optional<Conversation> findByIdWithAssistant(@Param("id") Long id);
}

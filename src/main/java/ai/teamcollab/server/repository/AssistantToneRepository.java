package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.AssistantTone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing AssistantTone entities.
 */
@Repository
public interface AssistantToneRepository extends JpaRepository<AssistantTone, Long> {
    
    /**
     * Find an AssistantTone by its name (case-insensitive).
     *
     * @param name the name of the tone
     * @return an Optional containing the AssistantTone if found, or empty if not found
     */
    Optional<AssistantTone> findByNameIgnoreCase(String name);
    
    /**
     * Find an AssistantTone by its display name (case-insensitive).
     *
     * @param displayName the display name of the tone
     * @return an Optional containing the AssistantTone if found, or empty if not found
     */
    Optional<AssistantTone> findByDisplayNameIgnoreCase(String displayName);
}
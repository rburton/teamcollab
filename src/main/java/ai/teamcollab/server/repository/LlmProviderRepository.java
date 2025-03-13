package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.LlmProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing LlmProvider entities.
 */
@Repository
public interface LlmProviderRepository extends CrudRepository<LlmProvider, Long> {
    
    /**
     * Find an LlmProvider by name.
     *
     * @param name the name of the provider
     * @return an Optional containing the LlmProvider if found, or empty if not found
     */
    Optional<LlmProvider> findByName(String name);
}
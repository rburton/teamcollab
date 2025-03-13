package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.LlmProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing LlmModel entities.
 */
@Repository
public interface LlmModelRepository extends CrudRepository<LlmModel, Long> {
    
    /**
     * Find an LlmModel by model ID.
     *
     * @param modelId the ID of the model
     * @return an Optional containing the LlmModel if found, or empty if not found
     */
    Optional<LlmModel> findByModelId(String modelId);
    
    /**
     * Find all LlmModels for a specific provider.
     *
     * @param provider the provider
     * @return a list of LlmModels for the provider
     */
    List<LlmModel> findByProvider(LlmProvider provider);
    
    /**
     * Find an LlmModel by name and provider.
     *
     * @param name the name of the model
     * @param provider the provider
     * @return an Optional containing the LlmModel if found, or empty if not found
     */
    Optional<LlmModel> findByNameAndProvider(String name, LlmProvider provider);
}
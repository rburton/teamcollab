package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.LlmProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Cacheable(value = "llmModelsByModelId", key = "#modelId")
    Optional<LlmModel> findByModelId(String modelId);

    /**
     * Find all LlmModels for a specific provider.
     *
     * @param provider the provider
     * @return a list of LlmModels for the provider
     */
    @Cacheable(value = "llmModelsByProvider", key = "#provider.id")
    List<LlmModel> findByProvider(LlmProvider provider);

    /**
     * Find an LlmModel by name and provider.
     *
     * @param name the name of the model
     * @param provider the provider
     * @return an Optional containing the LlmModel if found, or empty if not found
     */
    @Cacheable(value = "llmModelsByNameAndProvider", key = "{#name, #provider.id}")
    Optional<LlmModel> findByNameAndProvider(String name, LlmProvider provider);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "llmModels")
    List<LlmModel> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "llmModels", key = "#id")
    Optional<LlmModel> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "llmModels", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "llmModels", allEntries = true),
            @CacheEvict(value = "llmModelsByModelId", key = "#result.modelId"),
            @CacheEvict(value = "llmModelsByProvider", key = "#result.provider.id"),
            @CacheEvict(value = "llmModelsByNameAndProvider", key = "{#result.name, #result.provider.id}")
        }
    )
    <S extends LlmModel> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "llmModels", allEntries = true),
        @CacheEvict(value = "llmModelsByModelId", allEntries = true),
        @CacheEvict(value = "llmModelsByProvider", allEntries = true),
        @CacheEvict(value = "llmModelsByNameAndProvider", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "llmModels", allEntries = true),
        @CacheEvict(value = "llmModelsByModelId", key = "#entity.modelId"),
        @CacheEvict(value = "llmModelsByProvider", key = "#entity.provider.id"),
        @CacheEvict(value = "llmModelsByNameAndProvider", key = "{#entity.name, #entity.provider.id}")
    })
    void delete(LlmModel entity);
}

package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.LlmProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Cacheable(value = "llmProvidersByName", key = "#name")
    Optional<LlmProvider> findByName(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "llmProviders")
    Iterable<LlmProvider> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "llmProviders", key = "#id")
    Optional<LlmProvider> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "llmProviders", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "llmProviders", allEntries = true),
            @CacheEvict(value = "llmProvidersByName", key = "#result.name")
        }
    )
    <S extends LlmProvider> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "llmProviders", allEntries = true),
        @CacheEvict(value = "llmProvidersByName", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "llmProviders", allEntries = true),
        @CacheEvict(value = "llmProvidersByName", key = "#entity.name")
    })
    void delete(LlmProvider entity);
}

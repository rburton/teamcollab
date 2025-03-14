package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.AssistantTone;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    @Cacheable(value = "assistantTonesByName", key = "#name.toLowerCase()")
    Optional<AssistantTone> findByNameIgnoreCase(String name);

    /**
     * Find an AssistantTone by its display name (case-insensitive).
     *
     * @param displayName the display name of the tone
     * @return an Optional containing the AssistantTone if found, or empty if not found
     */
    @Cacheable(value = "assistantTonesByDisplayName", key = "#displayName.toLowerCase()")
    Optional<AssistantTone> findByDisplayNameIgnoreCase(String displayName);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "assistantTones")
    List<AssistantTone> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "assistantTones", key = "#id")
    Optional<AssistantTone> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "assistantTones", key = "#result.id"),
            @CachePut(value = "assistantTonesByName", key = "#result.name.toLowerCase()"),
            @CachePut(value = "assistantTonesByDisplayName", key = "#result.displayName.toLowerCase()")
        },
        evict = {
            @CacheEvict(value = "assistantTones", allEntries = true)
        }
    )
    <S extends AssistantTone> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "assistantTones", allEntries = true),
        @CacheEvict(value = "assistantTonesByName", allEntries = true),
        @CacheEvict(value = "assistantTonesByDisplayName", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "assistantTones", allEntries = true),
        @CacheEvict(value = "assistantTonesByName", allEntries = true),
        @CacheEvict(value = "assistantTonesByDisplayName", allEntries = true)
    })
    void delete(AssistantTone entity);
}

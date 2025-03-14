package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.SystemSettings;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    /**
     * Find the current system settings.
     *
     * @return an Optional containing the current SystemSettings if found, or empty if not found
     */
    @Cacheable(value = "currentSystemSettings")
    @Query(value = "SELECT s FROM SystemSettings s ORDER BY s.id ASC")
    Optional<SystemSettings> findCurrent();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "systemSettings", key = "#id")
    Optional<SystemSettings> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "systemSettings", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "systemSettings", allEntries = true),
            @CacheEvict(value = "currentSystemSettings", allEntries = true)
        }
    )
    <S extends SystemSettings> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "systemSettings", allEntries = true),
        @CacheEvict(value = "currentSystemSettings", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "systemSettings", allEntries = true),
        @CacheEvict(value = "currentSystemSettings", allEntries = true)
    })
    void delete(SystemSettings entity);
}

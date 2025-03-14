package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Role;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {

    /**
     * Find a role by name.
     *
     * @param name the name of the role
     * @return an Optional containing the Role if found, or empty if not found
     */
    @Cacheable(value = "rolesByName", key = "#name")
    Optional<Role> findByName(String name);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "roles")
    Iterable<Role> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "roles", key = "#id")
    Optional<Role> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "roles", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "roles", allEntries = true),
            @CacheEvict(value = "rolesByName", key = "#result.name")
        }
    )
    <S extends Role> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roles", allEntries = true),
        @CacheEvict(value = "rolesByName", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "roles", allEntries = true),
        @CacheEvict(value = "rolesByName", key = "#entity.name")
    })
    void delete(Role entity);
}

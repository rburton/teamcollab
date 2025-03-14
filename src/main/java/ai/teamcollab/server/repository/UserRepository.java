package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    /**
     * Find a user by username.
     *
     * @param username the username
     * @return an Optional containing the User if found, or empty if not found
     */
    @Cacheable(value = "usersByUsername", key = "#username")
    Optional<User> findByUsername(String username);

    /**
     * Find a user by email.
     *
     * @param email the email
     * @return an Optional containing the User if found, or empty if not found
     */
    @Cacheable(value = "usersByEmail", key = "#email")
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given username exists.
     *
     * @param username the username
     * @return true if a user with the given username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user with the given email exists.
     *
     * @param email the email
     * @return true if a user with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users for a specific company.
     *
     * @param companyId the company ID
     * @return a list of users for the company
     */
    @Cacheable(value = "usersByCompany", key = "#companyId")
    List<User> findByCompanyId(Long companyId);

    /**
     * Count the number of enabled users for a specific company.
     *
     * @param companyId the company ID
     * @return the count of enabled users for the company
     */
    @Cacheable(value = "enabledUserCountByCompany", key = "#companyId")
    long countByCompanyIdAndEnabledTrue(Long companyId);

    /**
     * Count the number of disabled users for a specific company.
     *
     * @param companyId the company ID
     * @return the count of disabled users for the company
     */
    @Cacheable(value = "disabledUserCountByCompany", key = "#companyId")
    long countByCompanyIdAndEnabledFalse(Long companyId);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "users")
    List<User> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "users", key = "#id")
    Optional<User> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "users", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "usersByUsername", key = "#result.username"),
            @CacheEvict(value = "usersByEmail", key = "#result.email"),
            @CacheEvict(value = "usersByCompany", key = "#result.company?.id"),
            @CacheEvict(value = "enabledUserCountByCompany", key = "#result.company?.id"),
            @CacheEvict(value = "disabledUserCountByCompany", key = "#result.company?.id")
        }
    )
    <S extends User> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "usersByUsername", allEntries = true),
        @CacheEvict(value = "usersByEmail", allEntries = true),
        @CacheEvict(value = "usersByCompany", allEntries = true),
        @CacheEvict(value = "enabledUserCountByCompany", allEntries = true),
        @CacheEvict(value = "disabledUserCountByCompany", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "users", allEntries = true),
        @CacheEvict(value = "usersByUsername", key = "#entity.username"),
        @CacheEvict(value = "usersByEmail", key = "#entity.email"),
        @CacheEvict(value = "usersByCompany", key = "#entity.company?.id"),
        @CacheEvict(value = "enabledUserCountByCompany", key = "#entity.company?.id"),
        @CacheEvict(value = "disabledUserCountByCompany", key = "#entity.company?.id")
    })
    void delete(User entity);
}

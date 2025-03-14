package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Company;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Company entities.
 */
@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "companies")
    List<Company> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "companies", key = "#id")
    Optional<Company> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "companies", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "companies", allEntries = true)
        }
    )
    <S extends Company> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "companies", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "companies", allEntries = true)
    })
    void delete(Company entity);
}

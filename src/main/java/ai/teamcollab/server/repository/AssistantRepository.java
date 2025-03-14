package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Assistant;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssistantRepository extends CrudRepository<Assistant, Long> {

    /**
     * Find assistants that belong to a specific company or are not associated with any company.
     *
     * @param companyId the company ID
     * @return a list of assistants
     */
    @Cacheable(value = "assistantsByCompany", key = "#companyId")
    List<Assistant> findByCompanyIdOrCompanyIdIsNull(Long companyId);

    /**
     * Find assistants that either belong to a specific company (or no company) or are not already in a specific conversation.
     *
     * @param companyId the company ID
     * @param conversationId the conversation ID
     * @return a list of assistants
     */
    @Cacheable(value = "assistantsNotInConversation", key = "{#companyId, #conversationId}")
    @Query("SELECT a FROM Assistant a WHERE (a.company.id = :companyId OR a.company.id IS NULL) OR NOT EXISTS (SELECT ca FROM ConversationAssistant ca WHERE ca.assistant = a AND ca.conversation.id = :conversationId)")
    List<Assistant> findAssistantsNotInConversation(@Param("companyId") Long companyId, @Param("conversationId") Long conversationId);

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "assistants")
    List<Assistant> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "assistants", key = "#id")
    Optional<Assistant> findById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(
        put = {
            @CachePut(value = "assistants", key = "#result.id")
        },
        evict = {
            @CacheEvict(value = "assistants", allEntries = true),
            @CacheEvict(value = "assistantsByCompany", allEntries = true),
            @CacheEvict(value = "assistantsNotInConversation", allEntries = true)
        }
    )
    <S extends Assistant> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "assistants", allEntries = true),
        @CacheEvict(value = "assistantsByCompany", allEntries = true),
        @CacheEvict(value = "assistantsNotInConversation", allEntries = true)
    })
    void deleteById(Long id);

    /**
     * {@inheritDoc}
     */
    @Override
    @Caching(evict = {
        @CacheEvict(value = "assistants", allEntries = true),
        @CacheEvict(value = "assistantsByCompany", allEntries = true),
        @CacheEvict(value = "assistantsNotInConversation", allEntries = true)
    })
    void delete(Assistant entity);
}

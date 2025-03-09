package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Assistant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantRepository extends CrudRepository<Assistant, Long> {
    List<Assistant> findByCompanyIdOrCompanyIdIsNull(Long companyId);

    List<Assistant> findByCompanyIdAndConversationsIdNotOrConversationsIsEmpty(Long companyId, Long conversationId);
}

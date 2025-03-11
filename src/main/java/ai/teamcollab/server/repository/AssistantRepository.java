package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Assistant;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssistantRepository extends CrudRepository<Assistant, Long> {
    List<Assistant> findByCompanyIdOrCompanyIdIsNull(Long companyId);

    @Query("SELECT a FROM Assistant a WHERE (a.company.id = :companyId OR a.company.id IS NULL) OR NOT EXISTS (SELECT ca FROM ConversationAssistant ca WHERE ca.assistant = a AND ca.conversation.id = :conversationId)")
    List<Assistant> findAssistantsNotInConversation(@Param("companyId") Long companyId, @Param("conversationId") Long conversationId);
}

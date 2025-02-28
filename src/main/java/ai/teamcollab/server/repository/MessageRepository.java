package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);
}
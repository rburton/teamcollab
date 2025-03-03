package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByCompanyId(Long companyId);
    long countByCompanyIdAndEnabledTrue(Long companyId);
    long countByCompanyIdAndEnabledFalse(Long companyId);
}

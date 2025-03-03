package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    @Query(value = "SELECT s FROM SystemSettings s ORDER BY s.id ASC")
    Optional<SystemSettings> findCurrent();
}

package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    /**
     * Finds top 10 metrics ordered by duration in descending order
     * @return List of top 10 metrics
     */
    List<Metrics> findTop10ByOrderByDurationDesc();
}
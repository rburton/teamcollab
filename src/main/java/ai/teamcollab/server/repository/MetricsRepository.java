package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Metrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    /**
     * Finds top 10 metrics ordered by duration in descending order
     * @return List of top 10 metrics
     */
    @Query("SELECT m FROM Metrics m JOIN FETCH m.message WHERE m IN (SELECT m2 FROM Metrics m2 ORDER BY m2.duration DESC)")
    List<Metrics> findTop10ByOrderByDurationDesc();

    /**
     * Finds metrics ordered by message creation date in descending order with pagination
     * @param pageable pagination information
     * @return Page of metrics
     */
    @Query("SELECT m FROM Metrics m JOIN FETCH m.message WHERE m IN (SELECT m2 FROM Metrics m2 ORDER BY m2.message.createdAt DESC)")
    Page<Metrics> findAllOrderByMessageCreatedAtDesc(Pageable pageable);

    /**
     * Finds metrics ordered by message creation date in descending order with pagination
     * @param pageable pagination information
     * @return Page of metrics
     */
    @Query("SELECT m FROM Metrics m ORDER BY m.id DESC")
    Page<Metrics> findAllCreatedAtDesc(Pageable pageable);

    /**
     * Finds all metrics for a specific company within a date range
     * @param companyId the ID of the company
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return List of metrics for the company within the date range
     */
    @Query("SELECT m FROM Metrics m WHERE m.message.user.company.id = :companyId " +
           "AND m.message.createdAt BETWEEN :startDate AND :endDate")
    List<Metrics> findByCompanyAndDateRange(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

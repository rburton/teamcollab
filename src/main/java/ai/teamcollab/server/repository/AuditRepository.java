package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for accessing and manipulating Audit entities.
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    /**
     * Find all audit events for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of audit events
     */
    List<Audit> findByUserId(Long userId);

    /**
     * Find all audit events for a specific company.
     *
     * @param companyId the ID of the company
     * @return a list of audit events
     */
    List<Audit> findByCompanyId(Long companyId);

    /**
     * Find all audit events of a specific action type.
     *
     * @param actionType the type of action
     * @return a list of audit events
     */
    List<Audit> findByActionType(Audit.AuditActionType actionType);

    /**
     * Find all audit events of a specific action type with pagination.
     *
     * @param actionType the type of action
     * @param pageable the pagination information
     * @return a page of audit events
     */
    Page<Audit> findByActionType(Audit.AuditActionType actionType, Pageable pageable);

    /**
     * Find all audit events within a specific time range.
     *
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return a list of audit events
     */
    List<Audit> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find all audit events for a specific entity.
     *
     * @param entityType the type of entity
     * @param entityId the ID of the entity
     * @return a list of audit events
     */
    List<Audit> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find all audit events for a specific company and action type.
     *
     * @param companyId the ID of the company
     * @param actionType the type of action
     * @return a list of audit events
     */
    List<Audit> findByCompanyIdAndActionType(Long companyId, Audit.AuditActionType actionType);

    /**
     * Find all audit events for a specific user and action type.
     *
     * @param userId the ID of the user
     * @param actionType the type of action
     * @return a list of audit events
     */
    List<Audit> findByUserIdAndActionType(Long userId, Audit.AuditActionType actionType);

    /**
     * Find the most recent audit events, limited by count.
     *
     * @param limit the maximum number of events to return
     * @return a list of audit events
     */
    @Query("SELECT a FROM Audit a ORDER BY a.timestamp DESC LIMIT :limit")
    List<Audit> findMostRecentAudits(@Param("limit") int limit);

    /**
     * Find all audit events with pagination.
     *
     * @param pageable the pagination information
     * @return a page of audit events
     */
    Page<Audit> findAll(Pageable pageable);

    /**
     * Find the most recent audit events with pagination.
     *
     * @param pageable the pagination information
     * @return a page of audit events
     */
    @Query("SELECT a FROM Audit a ORDER BY a.timestamp DESC")
    Page<Audit> findMostRecentAudits(Pageable pageable);
}

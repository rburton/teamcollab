package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.PlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PlanDetailRepository extends JpaRepository<PlanDetail, Long> {
    
    @Query("SELECT pd FROM PlanDetail pd " +
           "WHERE pd.plan.id = :planId " +
           "AND pd.effectiveDate <= :date " +
           "ORDER BY pd.effectiveDate DESC " +
           "LIMIT 1")
    Optional<PlanDetail> findActivePriceForPlan(@Param("planId") Long planId, 
                                               @Param("date") LocalDate date);
    @Query("SELECT pd FROM PlanDetail pd " +
           "WHERE pd.monthlyPrice IS NULL " +
           "ORDER BY pd.createdAt DESC " +
           "LIMIT 1")
    Optional<PlanDetail> findFreePlan();
}
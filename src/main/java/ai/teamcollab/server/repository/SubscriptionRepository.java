package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s " +
            "WHERE s.company.id = :companyId " +
            "AND s.startDate <= :date " +
            "AND (s.endDate IS NULL OR s.endDate >= :date)")
    Optional<Subscription> findActiveSubscriptionForCompany(@Param("companyId") Long companyId,
                                                            @Param("date") LocalDate date);

    List<Subscription> findByCompanyId(Long companyId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
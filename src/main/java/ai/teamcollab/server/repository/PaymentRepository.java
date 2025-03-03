package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findBySubscriptionId(Long subscriptionId);
    
    List<Payment> findBySubscriptionIdAndPaymentDateBetween(
        Long subscriptionId, 
        LocalDate startDate, 
        LocalDate endDate
    );
    
    List<Payment> findByStripePaymentStatus(String status);
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
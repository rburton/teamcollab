package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Payment;
import ai.teamcollab.server.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    @Transactional
    public Payment createPayment(Long subscriptionId, BigDecimal amount, String stripePaymentIntentId) {
        log.info("Creating payment for subscription: {} with amount: {}", subscriptionId, amount);

        final var subscription = subscriptionService.findById(subscriptionId);

        var payment = Payment.builder()
                .subscription(subscription)
                .amount(amount)
                .paymentDate(LocalDate.now())
                .stripePaymentIntentId(stripePaymentIntentId)
                .stripePaymentStatus("pending")
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePaymentStatus(String stripePaymentIntentId, String status) {
        log.info("Updating payment status for payment intent: {} to: {}", stripePaymentIntentId, status);

        var payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with intent ID: " + stripePaymentIntentId));

        payment.setStripePaymentStatus(status);

        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getSubscriptionPayments(Long subscriptionId) {
        return paymentRepository.findBySubscriptionId(subscriptionId);
    }

    @Transactional(readOnly = true)
    public List<Payment> getSubscriptionPaymentsBetweenDates(
            Long subscriptionId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return paymentRepository.findBySubscriptionIdAndPaymentDateBetween(
                subscriptionId,
                startDate,
                endDate
        );
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStripePaymentStatus(status);
    }
}
package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private Validator validator;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        var company = Company.builder()
            .name("Test Company")
            .stripeCustomerId("cus_123456")
            .build();
            
        var plan = Plan.builder()
            .name("Test Plan")
            .description("Test Description")
            .build();
            
        subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId("sub_123456")
            .startDate(LocalDate.now().plusDays(1))
            .build();
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var payment = Payment.builder()
            .subscription(subscription)
            .amount(new BigDecimal("99.99"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("pi_123456")
            .stripePaymentStatus("succeeded")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenSubscriptionIsNull_thenValidationFails() {
        var payment = Payment.builder()
            .amount(new BigDecimal("99.99"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("pi_123456")
            .stripePaymentStatus("succeeded")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Subscription is required");
    }

    @Test
    void whenAmountIsNegative_thenValidationFails() {
        var payment = Payment.builder()
            .subscription(subscription)
            .amount(new BigDecimal("-99.99"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("pi_123456")
            .stripePaymentStatus("succeeded")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Amount must be positive");
    }

    @Test
    void whenAmountHasTooManyDecimals_thenValidationFails() {
        var payment = Payment.builder()
            .subscription(subscription)
            .amount(new BigDecimal("99.999"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("pi_123456")
            .stripePaymentStatus("succeeded")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Amount must have at most 10 digits and 2 decimal places");
    }

    @Test
    void whenStripePaymentIntentIdFormatIsInvalid_thenValidationFails() {
        var payment = Payment.builder()
            .subscription(subscription)
            .amount(new BigDecimal("99.99"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("invalid_id")
            .stripePaymentStatus("succeeded")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Invalid Stripe payment intent ID format");
    }

    @Test
    void whenPaymentStatusIsInvalid_thenValidationFails() {
        var payment = Payment.builder()
            .subscription(subscription)
            .amount(new BigDecimal("99.99"))
            .paymentDate(LocalDate.now())
            .stripePaymentIntentId("pi_123456")
            .stripePaymentStatus("invalid_status")
            .build();

        var violations = validator.validate(payment);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Invalid payment status");
    }
}
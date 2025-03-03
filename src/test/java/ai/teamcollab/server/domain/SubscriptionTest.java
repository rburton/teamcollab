package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionTest {

    private Validator validator;
    private Company company;
    private Plan plan;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        company = Company.builder()
            .name("Test Company")
            .stripeCustomerId("cus_123456")
            .build();

        plan = Plan.builder()
            .name("Test Plan")
            .description("Test Description")
            .build();
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId("sub_123456")
            .startDate(LocalDate.now().plusDays(1))
            .build();

        var violations = validator.validate(subscription);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenCompanyIsNull_thenValidationFails() {
        var subscription = Subscription.builder()
            .plan(plan)
            .stripeSubscriptionId("sub_123456")
            .startDate(LocalDate.now().plusDays(1))
            .build();

        var violations = validator.validate(subscription);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Company is required");
    }

    @Test
    void whenStripeSubscriptionIdFormatIsInvalid_thenValidationFails() {
        var subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId("invalid_id")
            .startDate(LocalDate.now().plusDays(1))
            .build();

        var violations = validator.validate(subscription);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Invalid Stripe subscription ID format");
    }

    @Test
    void whenStartDateIsInPast_thenValidationFails() {
        var subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId("sub_123456")
            .startDate(LocalDate.now().minusDays(1))
            .build();

        var violations = validator.validate(subscription);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Start date must be today or in the future");
    }

    @Test
    void whenEndDateIsInPast_thenValidationFails() {
        var subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId("sub_123456")
            .startDate(LocalDate.now().plusDays(1))
            .endDate(LocalDate.now().minusDays(1))
            .build();

        var violations = validator.validate(subscription);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("End date must be today or in the future");
    }
}

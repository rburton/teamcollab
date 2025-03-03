package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PlanDetailTest {

    private Validator validator;
    private Plan plan;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        plan = Plan.builder()
            .name("Test Plan")
            .description("Test Description")
            .build();
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var planDetail = PlanDetail.builder()
            .plan(plan)
            .effectiveDate(LocalDate.now().plusDays(1))
            .monthlyPrice(new BigDecimal("19.99"))
            .build();

        var violations = validator.validate(planDetail);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenPlanIsNull_thenValidationFails() {
        var planDetail = PlanDetail.builder()
            .effectiveDate(LocalDate.now().plusDays(1))
            .monthlyPrice(new BigDecimal("19.99"))
            .build();

        var violations = validator.validate(planDetail);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Plan is required");
    }

    @Test
    void whenEffectiveDateIsInPast_thenValidationFails() {
        var planDetail = PlanDetail.builder()
            .plan(plan)
            .effectiveDate(LocalDate.now().minusDays(1))
            .monthlyPrice(new BigDecimal("19.99"))
            .build();

        var violations = validator.validate(planDetail);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Effective date must be today or in the future");
    }

    @Test
    void whenMonthlyPriceIsNegative_thenValidationFails() {
        var planDetail = PlanDetail.builder()
            .plan(plan)
            .effectiveDate(LocalDate.now().plusDays(1))
            .monthlyPrice(new BigDecimal("-19.99"))
            .build();

        var violations = validator.validate(planDetail);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Monthly price must be positive");
    }

    @Test
    void whenMonthlyPriceHasTooManyDecimals_thenValidationFails() {
        var planDetail = PlanDetail.builder()
            .plan(plan)
            .effectiveDate(LocalDate.now().plusDays(1))
            .monthlyPrice(new BigDecimal("19.999"))
            .build();

        var violations = validator.validate(planDetail);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Monthly price must have at most 10 digits and 2 decimal places");
    }
}
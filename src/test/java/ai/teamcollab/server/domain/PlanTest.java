package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var plan = Plan.builder()
            .name("Basic Plan")
            .description("A basic plan for small teams")
            .build();

        var violations = validator.validate(plan);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNull_thenValidationFails() {
        var plan = Plan.builder()
            .description("A basic plan for small teams")
            .build();

        var violations = validator.validate(plan);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Plan name is required");
    }

    @Test
    void whenNameIsTooShort_thenValidationFails() {
        var plan = Plan.builder()
            .name("ab")
            .description("A basic plan for small teams")
            .build();

        var violations = validator.validate(plan);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Plan name must be between 3 and 255 characters");
    }

    @Test
    void whenDescriptionIsTooLong_thenValidationFails() {
        var description = "a".repeat(1001);
        var plan = Plan.builder()
            .name("Basic Plan")
            .description(description)
            .build();

        var violations = validator.validate(plan);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("Description cannot exceed 1000 characters");
    }
}
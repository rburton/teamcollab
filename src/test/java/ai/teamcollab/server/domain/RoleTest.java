package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    private Validator validator;
    private Role role;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        role = new Role(null, "USER");
    }

    @Test
    void defaultConstructor_ShouldCreateInstance() {
        Role role = new Role();
        assertNotNull(role);
        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    void parameterizedConstructor_ShouldInitializeFieldsCorrectly() {
        assertEquals("USER", role.getName());
        assertNull(role.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void name_WhenBlank_ShouldFailValidation(String name) {
        role.setName(name);
        var violations = validator.validate(role);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void setAndGetName_ShouldWorkCorrectly() {
        role.setName("ADMIN");
        assertEquals("ADMIN", role.getName());
    }

    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        assertNull(role.getId()); // Initial value should be null

        role.setId(1L);
        assertEquals(1L, role.getId());
    }

    @Test
    void validRole_ShouldPassValidation() {
        var violations = validator.validate(role);
        assertTrue(violations.isEmpty());
    }
}
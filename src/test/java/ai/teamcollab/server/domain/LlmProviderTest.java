package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmProviderTest {

    private Validator validator;
    private LlmProvider provider;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        provider = new LlmProvider(null, "OpenAI", now, new HashSet<>());
    }

    @Test
    void defaultConstructor_ShouldCreateInstance() {
        LlmProvider provider = new LlmProvider();
        assertNotNull(provider);
        assertNull(provider.getId());
        assertNull(provider.getName());
        assertNull(provider.getUpdatedAt());
        assertNotNull(provider.getModels());
        assertTrue(provider.getModels().isEmpty());
    }

    @Test
    void parameterizedConstructor_ShouldInitializeFieldsCorrectly() {
        assertEquals("OpenAI", provider.getName());
        assertEquals(now, provider.getUpdatedAt());
        assertNull(provider.getId());
        assertNotNull(provider.getModels());
        assertTrue(provider.getModels().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void name_WhenBlank_ShouldFailValidation(String name) {
        provider.setName(name);
        var violations = validator.validate(provider);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void setAndGetName_ShouldWorkCorrectly() {
        provider.setName("Gemini");
        assertEquals("Gemini", provider.getName());
    }

    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        assertNull(provider.getId()); // Initial value should be null

        provider.setId(1L);
        assertEquals(1L, provider.getId());
    }

    @Test
    void setAndGetUpdatedAt_ShouldWorkCorrectly() {
        LocalDateTime newTime = LocalDateTime.now().plusDays(1);
        provider.setUpdatedAt(newTime);
        assertEquals(newTime, provider.getUpdatedAt());
    }

    @Test
    void validProvider_ShouldPassValidation() {
        var violations = validator.validate(provider);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_ShouldCreateValidInstance() {
        LlmProvider builtProvider = LlmProvider.builder()
                .id(1L)
                .name("OpenAI")
                .updatedAt(now)
                .build();

        assertNotNull(builtProvider);
        assertEquals(1L, builtProvider.getId());
        assertEquals("OpenAI", builtProvider.getName());
        assertEquals(now, builtProvider.getUpdatedAt());
        assertNotNull(builtProvider.getModels());
        assertTrue(builtProvider.getModels().isEmpty());
    }
}
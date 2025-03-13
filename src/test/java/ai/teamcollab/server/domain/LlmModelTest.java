package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmModelTest {

    private Validator validator;
    private LlmModel model;
    private LlmProvider provider;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        provider = LlmProvider.builder()
                .id(1L)
                .name("OpenAI")
                .updatedAt(LocalDateTime.now())
                .build();
                
        model = new LlmModel(
                null, 
                "GPT-4", 
                "gpt-4", 
                "GPT-4", 
                0.7, 
                BigDecimal.valueOf(30.00), 
                BigDecimal.valueOf(60.00), 
                provider
        );
    }

    @Test
    void defaultConstructor_ShouldCreateInstance() {
        LlmModel model = new LlmModel();
        assertNotNull(model);
        assertNull(model.getId());
        assertNull(model.getName());
        assertNull(model.getModelId());
        assertNull(model.getLabel());
        assertNull(model.getTemperature());
        assertNull(model.getInputPricePerMillion());
        assertNull(model.getOutputPricePerMillion());
        assertNull(model.getProvider());
    }

    @Test
    void parameterizedConstructor_ShouldInitializeFieldsCorrectly() {
        assertEquals("GPT-4", model.getName());
        assertEquals("gpt-4", model.getModelId());
        assertEquals("GPT-4", model.getLabel());
        assertEquals(0.7, model.getTemperature());
        assertEquals(BigDecimal.valueOf(30.00), model.getInputPricePerMillion());
        assertEquals(BigDecimal.valueOf(60.00), model.getOutputPricePerMillion());
        assertEquals(provider, model.getProvider());
        assertNull(model.getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void name_WhenBlank_ShouldFailValidation(String name) {
        model.setName(name);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void modelId_WhenBlank_ShouldFailValidation(String modelId) {
        model.setModelId(modelId);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("modelId")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void label_WhenBlank_ShouldFailValidation(String label) {
        model.setLabel(label);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("label")));
    }

    @Test
    void inputPricePerMillion_WhenNull_ShouldFailValidation() {
        model.setInputPricePerMillion(null);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("inputPricePerMillion")));
    }

    @Test
    void outputPricePerMillion_WhenNull_ShouldFailValidation() {
        model.setOutputPricePerMillion(null);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("outputPricePerMillion")));
    }

    @Test
    void provider_WhenNull_ShouldFailValidation() {
        model.setProvider(null);
        var violations = validator.validate(model);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("provider")));
    }

    @Test
    void temperature_CanBeNull() {
        model.setTemperature(null);
        var violations = validator.validate(model);
        assertTrue(violations.isEmpty());
    }

    @Test
    void setAndGetName_ShouldWorkCorrectly() {
        model.setName("GPT-3.5");
        assertEquals("GPT-3.5", model.getName());
    }

    @Test
    void setAndGetModelId_ShouldWorkCorrectly() {
        model.setModelId("gpt-3.5-turbo");
        assertEquals("gpt-3.5-turbo", model.getModelId());
    }

    @Test
    void setAndGetLabel_ShouldWorkCorrectly() {
        model.setLabel("GPT-3.5 Turbo");
        assertEquals("GPT-3.5 Turbo", model.getLabel());
    }

    @Test
    void setAndGetTemperature_ShouldWorkCorrectly() {
        model.setTemperature(0.5);
        assertEquals(0.5, model.getTemperature());
    }

    @Test
    void setAndGetInputPricePerMillion_ShouldWorkCorrectly() {
        model.setInputPricePerMillion(BigDecimal.valueOf(1.50));
        assertEquals(BigDecimal.valueOf(1.50), model.getInputPricePerMillion());
    }

    @Test
    void setAndGetOutputPricePerMillion_ShouldWorkCorrectly() {
        model.setOutputPricePerMillion(BigDecimal.valueOf(2.00));
        assertEquals(BigDecimal.valueOf(2.00), model.getOutputPricePerMillion());
    }

    @Test
    void setAndGetProvider_ShouldWorkCorrectly() {
        LlmProvider newProvider = LlmProvider.builder()
                .id(2L)
                .name("Gemini")
                .updatedAt(LocalDateTime.now())
                .build();
        model.setProvider(newProvider);
        assertEquals(newProvider, model.getProvider());
    }

    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        assertNull(model.getId()); // Initial value should be null

        model.setId(1L);
        assertEquals(1L, model.getId());
    }

    @Test
    void validModel_ShouldPassValidation() {
        var violations = validator.validate(model);
        assertTrue(violations.isEmpty());
    }

    @Test
    void builder_ShouldCreateValidInstance() {
        LlmModel builtModel = LlmModel.builder()
                .id(1L)
                .name("GPT-4")
                .modelId("gpt-4")
                .label("GPT-4")
                .temperature(0.7)
                .inputPricePerMillion(BigDecimal.valueOf(30.00))
                .outputPricePerMillion(BigDecimal.valueOf(60.00))
                .provider(provider)
                .build();

        assertNotNull(builtModel);
        assertEquals(1L, builtModel.getId());
        assertEquals("GPT-4", builtModel.getName());
        assertEquals("gpt-4", builtModel.getModelId());
        assertEquals("GPT-4", builtModel.getLabel());
        assertEquals(0.7, builtModel.getTemperature());
        assertEquals(BigDecimal.valueOf(30.00), builtModel.getInputPricePerMillion());
        assertEquals(BigDecimal.valueOf(60.00), builtModel.getOutputPricePerMillion());
        assertEquals(provider, builtModel.getProvider());
    }
}
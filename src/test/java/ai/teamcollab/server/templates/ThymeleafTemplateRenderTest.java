package ai.teamcollab.server.templates;

import ai.teamcollab.server.config.TestThymeleafConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = TestThymeleafConfig.class)
class ThymeleafTemplateRenderTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    private ThymeleafTemplateRender templateRender;

    @BeforeEach
    void setUp() {
        templateRender = new ThymeleafTemplateRender(templateEngine);
    }

    @Test
    void render_withValidTemplateAndValues_shouldRenderTemplate() {
        // Arrange
        final var templatePath = "test/template";
        final var values = Map.<String, Object>of("key1", "value1", "key2", "value2");
        final var expectedResult = "<html><body>Rendered template</body></html>";

        when(templateEngine.process(eq(templatePath), any(Context.class))).thenReturn(expectedResult);

        // Act
        final var result = templateRender.render(templatePath, values);

        // Assert
        assertEquals(expectedResult, result);
        verify(templateEngine).process(eq(templatePath), any(Context.class));
    }

    @Test
    void render_withEmptyValues_shouldRenderTemplateWithEmptyContext() {
        // Arrange
        final var templatePath = "test/template";
        final var values = new HashMap<String, Object>();
        final var expectedResult = "<html><body>Rendered template</body></html>";

        when(templateEngine.process(eq(templatePath), any(Context.class))).thenReturn(expectedResult);

        // Act
        final var result = templateRender.render(templatePath, values);

        // Assert
        assertEquals(expectedResult, result);
        verify(templateEngine).process(eq(templatePath), any(Context.class));
    }

    @Test
    void render_withEmptyTemplatePath_shouldThrowIllegalArgumentException() {
        // Arrange
        final var templatePath = "";
        final var values = Map.<String, Object>of("key", "value");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> templateRender.render(templatePath, values));
    }

    @Test
    void render_withNullValues_shouldThrowNullPointerException() {
        // Arrange
        final var templatePath = "test/template";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> templateRender.render(templatePath, null));
    }

    @Test
    void render_whenTemplateEngineThrowsException_shouldWrapAndRethrow() {
        // Arrange
        final var templatePath = "test/template";
        final var values = Map.<String, Object>of("key", "value");

        when(templateEngine.process(eq(templatePath), any(Context.class)))
            .thenThrow(new RuntimeException("Template processing error"));

        // Act & Assert
        final var exception = assertThrows(RuntimeException.class, () -> templateRender.render(templatePath, values));
        assertTrue(exception.getMessage().contains("Failed to render template"));
        assertTrue(exception.getCause().getMessage().contains("Template processing error"));
    }
}

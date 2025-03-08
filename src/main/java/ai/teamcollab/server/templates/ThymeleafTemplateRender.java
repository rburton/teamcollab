package ai.teamcollab.server.templates;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

/**
 * A utility class for rendering Thymeleaf templates. Given a template path and a map of values, it renders the template
 * as a string.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThymeleafTemplateRender {

    private final SpringTemplateEngine templateEngine;

    /**
     * Renders a Thymeleaf template with the provided values.
     *
     * @param templatePath the path to the template (without the .html extension)
     * @param values       the values to be used in the template
     * @return the rendered template as a string
     * @throws IllegalArgumentException if the template path is null or empty
     */
    public String renderToHtml(@NonNull final TemplatePath templatePath, @NonNull final Map<TemplateVariableName, Object> values) {
        final var context = templatePath.validate(values);

        return this.render(templatePath.getPath(), context);
    }

    /**
     * Renders a Thymeleaf template with the provided values.
     *
     * @param templatePath the path to the template (without the .html extension)
     * @param values       the values to be used in the template
     * @return the rendered template as a string
     * @throws IllegalArgumentException if the template path is null or empty
     */
    public String render(@NonNull final TemplatePath templatePath, @NonNull final Map<String, Object> values) {
        templatePath.validateString(values);
        return this.render(templatePath.getPath(), values);
    }

    /**
     * Renders a Thymeleaf template with the provided values.
     *
     * @param templatePath the path to the template (without the .html extension)
     * @param values       the values to be used in the template
     * @return the rendered template as a string
     * @throws IllegalArgumentException if the template path is null or empty
     */
    public String render(@NonNull final String templatePath, @NonNull final Map<String, Object> values) {
        log.debug("Rendering template: {} with values: {}", templatePath, values);

        if (templatePath.isBlank()) {
            throw new IllegalArgumentException("Template path cannot be empty");
        }

        try {
            final var context = new Context();
            context.setVariables(values);

            final var result = templateEngine.process(templatePath, context);
            log.debug("Template rendered successfully");
            return result;
        } catch (final Exception e) {
            log.error("Failed to render template: {}", templatePath, e);
            throw new RuntimeException("Failed to render template: " + templatePath, e);
        }
    }
}
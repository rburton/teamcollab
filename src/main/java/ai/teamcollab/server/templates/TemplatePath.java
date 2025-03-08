package ai.teamcollab.server.templates;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ai.teamcollab.server.templates.TemplateVariableName.MESSAGE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Getter
public enum TemplatePath {
    CONVERSATION_MESSAGE_TEMPLATE("conversations/message.xhtml", MESSAGE);

    private final String path;
    private final Set<String> variables;

    TemplatePath(String path, String... variables) {
        this.path = path;
        this.variables = Arrays.stream(variables)
                .collect(toUnmodifiableSet());
    }

    TemplatePath(String path, TemplateVariableName... variables) {
        this.path = path;
        this.variables = Arrays.stream(variables)
                .map(TemplateVariableName::getKey)
                .collect(toUnmodifiableSet());
    }

    public Map<String, Object> validateString(Map<String, Object> context) {
        if (context.keySet().containsAll(variables)) {
            return context;
        }
        final var missingVariables = variables.stream()
                .filter(var -> !context.containsKey(var))
                .collect(joining(","));
        throw new IllegalStateException("Missing variables in context: " + missingVariables);
    }

    public Map<String, Object> validate(Map<TemplateVariableName, Object> context) {
        final var provided = context.keySet()
                .stream()
                .map(TemplateVariableName::getKey)
                .collect(toUnmodifiableSet());
        if (provided.containsAll(variables)) {
            final var newContext = new HashMap<String, Object>();
            context.forEach((key, value) -> newContext.put(key.getKey(), value));
            return newContext;
        }
        final var missingVariables = variables.stream()
                .filter(var -> !provided.contains(var))
                .collect(joining(","));
        throw new IllegalStateException("Missing variables in context: " + missingVariables);
    }

}

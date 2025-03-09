package ai.teamcollab.server.templates;

import lombok.Getter;

@Getter
public enum TemplateVariableName {
    MESSAGE("message"),
    ASSISTANTS("assistants"),
    STATUS("status"),
    ;

    private final String key;

    TemplateVariableName(String message) {
        this.key = message;
    }
}

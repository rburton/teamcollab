package ai.teamcollab.server.api.domain;

import jakarta.validation.constraints.NotNull;

public class AddPersonaRequest {
    @NotNull(message = "Persona ID is required")
    private Long personaId;

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }
}

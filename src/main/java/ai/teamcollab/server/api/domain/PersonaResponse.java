package ai.teamcollab.server.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("expertises")
    private String expertises;

    public static PersonaResponse fromPersona(ai.teamcollab.server.domain.Persona persona) {
        return new PersonaResponse(persona.getId(), persona.getName(), persona.getExpertises());
    }
}

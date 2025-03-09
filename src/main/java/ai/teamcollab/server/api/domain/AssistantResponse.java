package ai.teamcollab.server.api.domain;

import ai.teamcollab.server.domain.Assistant;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssistantResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("expertise")
    private String expertise;

    @JsonProperty("description")
    private String description;

    public static AssistantResponse fromAssistant(Assistant assistant) {
        return new AssistantResponse(assistant.getId(), assistant.getName(), assistant.getExpertise(), assistant.getExpertisePrompt());
    }
}

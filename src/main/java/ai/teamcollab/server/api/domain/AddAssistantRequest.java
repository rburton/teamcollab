package ai.teamcollab.server.api.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddAssistantRequest {
    @NotNull(message = "Assistant ID is required")
    private Long assistantId;
}

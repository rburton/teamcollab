package ai.teamcollab.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Overview is required")
    @Size(min = 3, max = 255, message = "Overview must be between 3 and 255 characters")
    private String overview;
    
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 1000, message = "Purpose must be between 10 and 1000 characters")
    private String purpose;
}
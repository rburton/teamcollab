package ai.teamcollab.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserDTO {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$",
        message = "Password must be at least 8 characters long and contain at least one number and one special character"
    )
    private String password;

    // Default constructor for form binding
    public CreateUserDTO() {
    }

    // All-args constructor for builder
    public CreateUserDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
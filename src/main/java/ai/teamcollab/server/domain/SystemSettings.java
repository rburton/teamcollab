package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// import static ai.teamcollab.server.domain.Provider.OPENAI;
import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_settings")
public class SystemSettings {
    @Id
    @Column(name = "system_setting_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "llm_model", nullable = false)
    @Builder.Default
    private String llmModel = "gpt-3.5-turbo";

    @NotBlank
    @Column(name = "llm_provider", nullable = false)
    @Builder.Default
    private String llmProvider = "openai";

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

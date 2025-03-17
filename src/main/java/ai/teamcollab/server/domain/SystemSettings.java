package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_settings")
public class SystemSettings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "system_setting_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "llm_model_id", nullable = false)
    private LlmModel llmModel;

    @ManyToOne
    @JoinColumn(name = "summary_llm_model_id", nullable = false)
    private LlmModel summaryLlmModel;

    @ManyToOne
    @JoinColumn(name = "assistant_interaction_llm_model_id", nullable = false)
    private LlmModel assistantInteractionLlmModel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "summary_batch_size")
    private int summaryBatchSize = 10;
}

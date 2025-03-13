package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents a Language Learning Model (LLM) such as GPT-4, Gemini, etc.
 * Each model belongs to a provider and has specific pricing and configuration.
 */
@Entity(name = "llm_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "provider")
@Builder
public class LlmModel implements Serializable {

    @Id
    @Column(name = "llm_model_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "model_id", nullable = false)
    private String modelId;

    @NotBlank
    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "temperature")
    private Double temperature;

    @NotNull
    @Column(name = "input_price_per_million", nullable = false)
    private BigDecimal inputPricePerMillion;

    @NotNull
    @Column(name = "output_price_per_million", nullable = false)
    private BigDecimal outputPricePerMillion;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "llm_provider_id", nullable = false)
    private LlmProvider provider;
}
package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents a Language Learning Model (LLM) provider such as OpenAI, Gemini, etc. Each provider can have multiple LLM
 * models.
 */
@Entity(name = "llm_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "models")
@Builder
public class LlmProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String OPEN_AI = "OpenAI";
    public static final String GEMINI = "Gemini";

    @Id
    @Column(name = "llm_provider_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "provider", cascade = ALL, fetch = LAZY)
    private Set<LlmModel> models = new HashSet<>();

    public boolean isOpenAi() {
        return OPEN_AI.equalsIgnoreCase(name);
    }

    public boolean isGemini() {
        return GEMINI.equalsIgnoreCase(name);
    }

    /**
     * Finds a model by its ID.
     *
     * @param modelId the ID of the model to find
     * @return the model with the given ID
     * @throws java.util.NoSuchElementException if no model with the given ID is found
     */
    public LlmModel findModelById(String modelId) {
        return models.stream()
                .filter(model -> model.getModelId().equalsIgnoreCase(modelId))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Gets all models for this provider.
     *
     * @return a list of models for this provider
     */
    public List<LlmModel> getModels() {
        return new ArrayList<>(models);
    }

    /**
     * Gets the label for this provider.
     *
     * @return the label for this provider
     */
    public String getLabel() {
        return name;
    }
}

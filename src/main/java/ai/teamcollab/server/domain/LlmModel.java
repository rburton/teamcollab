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
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.text.NumberFormat.getCurrencyInstance;
import static java.util.Locale.US;

/**
 * Represents a Language Learning Model (LLM) such as GPT-4, Gemini, etc. Each model belongs to a provider and has
 * specific pricing and configuration.
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

    private static final long serialVersionUID = 1L;

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

    @Column(name = "overview")
    private String overview;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "context_size")
    private long contextSize;

    @NotNull
    @Column(name = "input_price_per_million", nullable = false)
    private BigDecimal inputPricePerMillion;

    @NotNull
    @Column(name = "output_price_per_million", nullable = false)
    private BigDecimal outputPricePerMillion;

    @NotNull
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "llm_provider_id", nullable = false)
    private LlmProvider provider;

    private static final BigDecimal MILLION = new BigDecimal("1000000");
    private static final BigDecimal TOKEN_DIVISOR = new BigDecimal("1000");

    /**
     * Calculates the cost for the given number of input and output tokens.
     *
     * @param inputTokens  the number of input tokens
     * @param outputTokens the number of output tokens
     * @return the calculated cost
     * @throws IllegalArgumentException if token counts are negative
     */
    public BigDecimal calculate(int inputTokens, int outputTokens) {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("Token counts cannot be negative");
        }

        // Calculate cost for input tokens
        final var promptPricePer1K = inputPricePerMillion.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);
        final var inputCost = BigDecimal.valueOf(inputTokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(promptPricePer1K);

        // Calculate cost for output tokens
        final var completionPricePer1K = outputPricePerMillion.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);
        final var outputCost = BigDecimal.valueOf(outputTokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(completionPricePer1K);

        // Return total cost
        return inputCost.add(outputCost);
    }

    /**
     * Formats the given amount as a currency string.
     *
     * @param amount the amount to format
     * @return the formatted amount
     */
    public String format(@NonNull BigDecimal amount) {
        final var usdFormat = getCurrencyInstance(US);
        return usdFormat.format(amount);
    }

    /**
     * Returns a string representation of this model suitable for display in a dropdown.
     *
     * @return the dropdown display string
     */
    public String getDropdown() {
        return provider.getLabel() + " " + modelId + " " + format(inputPricePerMillion.add(outputPricePerMillion)) +
                " (in: " + format(inputPricePerMillion) + " out: " + format(outputPricePerMillion) + ")";
    }
}

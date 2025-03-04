package ai.teamcollab.server.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.text.NumberFormat.getCurrencyInstance;
import static java.util.Locale.US;

@Getter
public enum GptModel {

    CHATGPT_4o_LATEST("chatgpt-4o-latest", 5.00, 15.00),
    CHATGPT_4o("gpt-4o", 2.50, 10.00),
    CHATGPT_4o_MINI("gpt-4o-mini", 0.15, 0.60),
    O1("o1", 15.00, 60.00),
    O1_MINI("o1-mini", 1.10, 4.40),
    O3_MINI("o3-mini", 1.10, 4.40),
    GPT_3_5_TURBO("gpt-3.5-turbo", 1.50, 2.00),
    GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k", 3.00, 4.00),
    GPT_3_5_TURBO_INSTRUCT("gpt-3.5-turbo-instruct", 1.50, 2.00),
    GPT_4("gpt-4", 30.00, 60.00),
    GPT_4_TURBO("gpt-4-turbo", 30.00, 60.00),
    GPT_4_32K("gpt-4-32k", 60.00, 120.00);

    private final BigDecimal MILLION = new BigDecimal("1000000");         // Price is per 1K tokens
    private final BigDecimal TOKEN_DIVISOR = new BigDecimal("1000");         // Price is per 1K tokens

    private final String id;
    private final BigDecimal promptPricePer1M;
    private final BigDecimal completionPricePer1M;
    private final BigDecimal promptPricePer1K;
    private final BigDecimal completionPricePer1K;

    GptModel(String id, double promptPrice, double completionPrice) {
        this.id = id;
        this.promptPricePer1M = BigDecimal.valueOf(promptPrice);
        this.completionPricePer1M = BigDecimal.valueOf(completionPrice);
        this.promptPricePer1K = promptPricePer1M.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);

        this.completionPricePer1K = completionPricePer1M.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);
    }

    public String getDropdown() {
        return id + " " + format(promptPricePer1M.add(completionPricePer1M)) + " (in: " + format(this.promptPricePer1M) + " out: " + format(completionPricePer1M) +  ")";
    }

    public String format(BigDecimal amount) {
        final var usdFormat = getCurrencyInstance(US);
        return usdFormat.format(amount);
    }

    public static GptModel fromId(String name) {
        for (final var model : GptModel.values()) {
            if (model.id.equalsIgnoreCase(name)) {
                return model;
            }
        }
        throw new IllegalArgumentException("No model found with name: " + name);
    }

    public BigDecimal calculate(int inputTokens, int outputTokens) {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("Token counts cannot be negative");
        }

        // Calculate cost for input tokens
        final var inputCost = BigDecimal.valueOf(inputTokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(promptPricePer1K);

        final var outputCost = BigDecimal.valueOf(outputTokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(completionPricePer1K);


        // Return total cost
        return inputCost.add(outputCost);
    }
}

package ai.teamcollab.server.domain;

import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import static ai.teamcollab.server.domain.Provider.GEMINI;
import static ai.teamcollab.server.domain.Provider.OPENAI;
import static java.text.NumberFormat.getCurrencyInstance;
import static java.util.Locale.US;

@Getter
public enum GptModel {

    CHATGPT_4o_LATEST(OPENAI, "chatgpt-4o-latest", 0.7, 5.00, 15.00),
    CHATGPT_4o(OPENAI, "gpt-4o", 0.7, 2.50, 10.00),
    CHATGPT_4o_MINI(OPENAI, "gpt-4o-mini", 0.7, 0.15, 0.60),
    O1(OPENAI, "o1", 0.7, 15.00, 60.00),
    O1_MINI(OPENAI, "o1-mini", 0.7, 1.10, 4.40),
    O3_MINI(OPENAI, "o3-mini", null, 1.10, 4.40),
    GPT_3_5_TURBO(OPENAI, "gpt-3.5-turbo", 0.7, 1.50, 2.00),
    GPT_3_5_TURBO_16K(OPENAI, "gpt-3.5-turbo-16k", 0.7, 3.00, 4.00),
    GPT_3_5_TURBO_INSTRUCT(OPENAI, "gpt-3.5-turbo-instruct", 0.7, 1.50, 2.00),
    GPT_4(OPENAI, "gpt-4", 0.7, 30.00, 60.00),
    GPT_4_TURBO(OPENAI, "gpt-4-turbo", 0.7, 30.00, 60.00),
    GPT_4_32K(OPENAI, "gpt-4-32k", 0.7, 60.00, 120.00),
    GEMINI_2_0_FLASH(GEMINI, "gemini-2.0-flash", null, 0.15, 0.60),
    GEMINI_2_0_FLASH_LITE(GEMINI, "gemini-2.0-flash-lite", null, 0.15, 0.60),
    GEMINI_1_5_FLASH(GEMINI, "gemini-1.5-flash", null, 0.075, 0.30),
    GEMINI_1_5_PRO(GEMINI, "gemini-1.5-pro", null, 0.3125, 1.25),
    ;

    private final BigDecimal MILLION = new BigDecimal("1000000");         // Price is per 1K tokens
    private final BigDecimal TOKEN_DIVISOR = new BigDecimal("1000");         // Price is per 1K tokens

    private final Provider provider;
    private final String id;
    private final BigDecimal promptPricePer1M;
    private final BigDecimal completionPricePer1M;
    private final BigDecimal promptPricePer1K;
    private final BigDecimal completionPricePer1K;
    private final Double temperature;

    GptModel(Provider provider, String id, Double temperature, double promptPrice, double completionPrice) {
        this.provider = provider;
        this.id = id;
        this.temperature = temperature;
        this.promptPricePer1M = BigDecimal.valueOf(promptPrice);
        this.completionPricePer1M = BigDecimal.valueOf(completionPrice);
        this.promptPricePer1K = promptPricePer1M.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);

        this.completionPricePer1K = completionPricePer1M.divide(MILLION, 6, RoundingMode.HALF_UP)
                .multiply(TOKEN_DIVISOR);
    }

    public static List<GptModel> findByProvider(@NonNull Provider provider) {
        return Arrays.stream(GptModel.values())
                .filter(model -> model.getProvider() == provider)
                .toList();
    }

    public static GptModel fromId(@NonNull String name) {
        for (final var model : GptModel.values()) {
            if (model.id.equalsIgnoreCase(name)) {
                return model;
            }
        }
        throw new IllegalArgumentException("No model found with name: " + name);
    }

    public String getDropdown() {
        return id + " " + format(promptPricePer1M.add(completionPricePer1M)) + " (in: " + format(this.promptPricePer1M) + " out: " + format(completionPricePer1M) + ")";
    }

    public String format(@NonNull BigDecimal amount) {
        final var usdFormat = getCurrencyInstance(US);
        return usdFormat.format(amount);
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

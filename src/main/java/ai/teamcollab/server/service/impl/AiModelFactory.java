package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.lang.System.getenv;

/**
 * Factory for creating AI models with appropriate configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelFactory {
    private final SystemSettingsService systemSettingsService;

    /**
     * Creates an OpenAI chat model configured based on the conversation and system settings.
     *
     * @param conversation the conversation to create the model for
     * @return a configured OpenAiChatModel
     */
    public ChatModel createModel(Conversation conversation) {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        final var llmModel = Optional.ofNullable(conversation)
                .map(Conversation::getUser)
                .map(User::getCompany)
                .map(Company::getLlmModel)
                .orElse(currentSettings.getLlmModel());

        return createChatModelFromLlmModel(llmModel);
    }

    /**
     * Creates an OpenAI chat model specifically for generating summaries.
     * Uses the summaryLlmModel from SystemSettings.
     *
     * @return a configured OpenAiChatModel for summaries
     */
    public ChatModel createSummaryModel() {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        final var summaryLlmModel = currentSettings.getSummaryLlmModel();

        log.debug("Creating summary model using: {}", summaryLlmModel.getName());
        return createChatModelFromLlmModel(summaryLlmModel);
    }

    /**
     * Creates an OpenAI chat model specifically for assistant interaction decisions.
     * Uses the assistantInteractionLlmModel from SystemSettings.
     *
     * @return a configured OpenAiChatModel for assistant interaction decisions
     */
    public ChatModel createAssistantInteractionModel() {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        final var assistantInteractionLlmModel = currentSettings.getAssistantInteractionLlmModel();

        log.debug("Creating assistant interaction model using: {}", assistantInteractionLlmModel.getName());
        return createChatModelFromLlmModel(assistantInteractionLlmModel);
    }

    /**
     * Helper method to create a ChatModel from an LlmModel.
     *
     * @param llmModel the LLM model to use
     * @return a configured ChatModel
     */
    private ChatModel createChatModelFromLlmModel(LlmModel llmModel) {
        final var provider = llmModel.getProvider();
        if (provider.isOpenAi()) {
            log.info("Using OpenAPI Client with model: {}", llmModel.getModelId());
            return OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .apiKey(getenv("OPENAI_API_KEY"))
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(llmModel.getModelId())
                            .temperature(llmModel.getTemperature())
                            .build())
                    .build();
        }
        log.info("Using Gemini Client with model: {}", llmModel.getModelId());
        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(getenv("GEMINI_FLASH_2_0_KEY"))
                        .baseUrl(getenv("GEMINI_URL"))
                        .completionsPath(getenv("GEMINI_COMPLETIONS_PATH"))
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(llmModel.getModelId())
                        .temperature(llmModel.getTemperature())
                        .build())
                .build();
    }

    /**
     * Gets the LLM model ID for a conversation.
     *
     * @param conversation the conversation to get the model for
     * @return the LLM model ID
     */
    public LlmModel getActiveLlmModel(Conversation conversation) {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        return Optional.ofNullable(conversation)
                .map(Conversation::getUser)
                .map(User::getCompany)
                .map(Company::getLlmModel)
                .orElse(currentSettings.getLlmModel());
    }

    /**
     * Gets the LLM model used for assistant interaction decisions.
     *
     * @return the LLM model for assistant interaction decisions
     */
    public LlmModel getAssistantInteractionLlmModel() {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        return currentSettings.getAssistantInteractionLlmModel();
    }

}

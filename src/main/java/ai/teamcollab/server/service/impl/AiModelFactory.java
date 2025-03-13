package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.LlmModelRepository;
import ai.teamcollab.server.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Factory for creating AI models with appropriate configuration.
 */
@Component
@RequiredArgsConstructor
public class AiModelFactory {
    private final SystemSettingsService systemSettingsService;
    private final LlmModelRepository llmModelRepository;

    /**
     * Creates an OpenAI chat model configured based on the conversation and system settings.
     *
     * @param conversation the conversation to create the model for
     * @return a configured OpenAiChatModel
     */
    public OpenAiChatModel createModel(Conversation conversation) {
        final var currentSettings = systemSettingsService.getCurrentSettings();
        final var llmModel = Optional.ofNullable(conversation)
                .map(Conversation::getUser)
                .map(User::getCompany)
                .map(Company::getLlmModel)
                .orElse(currentSettings.getLlmModel());

        return OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .apiKey(System.getenv("OPENAI_API_KEY"))
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

}

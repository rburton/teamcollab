package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.repository.LlmModelRepository;
import ai.teamcollab.server.repository.SystemSettingsRepository;
import ai.teamcollab.server.service.SystemSettingsService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final SystemSettingsRepository systemSettingsRepository;
    private final LlmModelRepository llmModelRepository;

    @Autowired
    public SystemSettingsServiceImpl(SystemSettingsRepository systemSettingsRepository, LlmModelRepository llmModelRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
        this.llmModelRepository = llmModelRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettings getCurrentSettings() {
        log.debug("Fetching current system settings");
        return systemSettingsRepository.findCurrent()
                .orElseGet(() -> {
                    log.info("No system settings found, creating default settings");
                    // Find the default LLM model (GPT-3.5-turbo from OpenAI)
                    var defaultModel = llmModelRepository.findByModelId("gpt-3.5-turbo")
                            .orElseGet(() -> {
                                log.warn("Default model 'gpt-3.5-turbo' not found, using first available model");
                                var models = llmModelRepository.findAll();
                                var iterator = models.iterator();
                                if (!iterator.hasNext()) {
                                    throw new IllegalStateException("No LLM models found in the database");
                                }
                                return iterator.next();
                            });

                    return systemSettingsRepository.save(SystemSettings.builder()
                            .llmModel(defaultModel)
                            .build());
                });
    }

    @Override
    @Transactional
    public SystemSettings updateSettings(@NonNull SystemSettings settings) {
        log.debug("Updating system settings: {}", settings);

        var current = getCurrentSettings();
        current.setLlmModel(settings.getLlmModel());

        return systemSettingsRepository.save(current);
    }
}

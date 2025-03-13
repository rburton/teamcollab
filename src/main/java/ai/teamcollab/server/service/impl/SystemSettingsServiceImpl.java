package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.SystemSettings;
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

    @Autowired
    public SystemSettingsServiceImpl(SystemSettingsRepository systemSettingsRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettings getCurrentSettings() {
        log.debug("Fetching current system settings");
        return systemSettingsRepository.findCurrent()
                .orElseGet(() -> {
                    log.info("No system settings found, creating default settings");
                    return systemSettingsRepository.save(SystemSettings.builder().build());
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

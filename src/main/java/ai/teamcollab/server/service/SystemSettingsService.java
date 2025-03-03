package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.SystemSettings;
import lombok.NonNull;

public interface SystemSettingsService {
    /**
     * Get the current system settings. If no settings exist, creates default settings.
     *
     * @return the current system settings
     */
    SystemSettings getCurrentSettings();

    /**
     * Update the system settings.
     *
     * @param settings the new settings to apply
     * @return the updated settings
     */
    SystemSettings updateSettings(@NonNull SystemSettings settings);
}
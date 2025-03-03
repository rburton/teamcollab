package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.repository.SystemSettingsRepository;
import ai.teamcollab.server.service.impl.SystemSettingsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemSettingsServiceTest {

    @Mock
    private SystemSettingsRepository systemSettingsRepository;

    private SystemSettingsService systemSettingsService;

    @BeforeEach
    void setUp() {
        systemSettingsService = new SystemSettingsServiceImpl(systemSettingsRepository);
    }

    @Test
    void getCurrentSettings_WhenNoSettings_CreatesDefault() {
        // Given
        when(systemSettingsRepository.findCurrent()).thenReturn(Optional.empty());
        when(systemSettingsRepository.save(any(SystemSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SystemSettings result = systemSettingsService.getCurrentSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLlmModel()).isEqualTo("gpt-3.5-turbo");
    }

    @Test
    void getCurrentSettings_WhenSettingsExist_ReturnsSettings() {
        // Given
        var existingSettings = SystemSettings.builder()
                .llmModel("gpt-4")
                .build();
        when(systemSettingsRepository.findCurrent()).thenReturn(Optional.of(existingSettings));

        // When
        SystemSettings result = systemSettingsService.getCurrentSettings();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLlmModel()).isEqualTo("gpt-4");
    }

    @Test
    void updateSettings_UpdatesSuccessfully() {
        // Given
        var currentSettings = SystemSettings.builder()
                .llmModel("gpt-3.5-turbo")
                .build();
        var newSettings = SystemSettings.builder()
                .llmModel("gpt-4")
                .build();

        when(systemSettingsRepository.findCurrent()).thenReturn(Optional.of(currentSettings));
        when(systemSettingsRepository.save(any(SystemSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SystemSettings result = systemSettingsService.updateSettings(newSettings);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLlmModel()).isEqualTo("gpt-4");
    }
}
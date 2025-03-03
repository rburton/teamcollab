package ai.teamcollab.server.controller;

import ai.teamcollab.server.config.SecurityConfig;
import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.service.SystemSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemController.class)
@Import({SecurityConfig.class, SystemControllerTest.TestConfig.class})
class SystemControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public SystemSettingsService systemSettingsService() {
            return Mockito.mock(SystemSettingsService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @BeforeEach
    void setUp() {
        Mockito.reset(systemSettingsService);
        when(systemSettingsService.getCurrentSettings())
            .thenReturn(SystemSettings.builder()
                .llmModel("gpt-3.5-turbo")
                .build());
        when(systemSettingsService.updateSettings(any(SystemSettings.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void showSettings_WithSuperAdmin_ShouldSucceed() throws Exception {
        // When/Then
        mockMvc.perform(get("/system"))
                .andExpect(status().isOk())
                .andExpect(view().name("system/settings"))
                .andExpect(model().attributeExists("settings"))
                .andExpect(model().attribute("settings", systemSettingsService.getCurrentSettings()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void showSettings_WithoutSuperAdmin_ShouldFail() throws Exception {
        mockMvc.perform(get("/system"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateSettings_WithValidData_ShouldRedirect() throws Exception {
        // Given
        String newModel = "gpt-4";
        var expectedSettings = SystemSettings.builder()
                .llmModel(newModel)
                .build();

        // When/Then
        mockMvc.perform(post("/system/update")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("llmModel", newModel))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/system"));

        verify(systemSettingsService).updateSettings(any(SystemSettings.class));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void updateSettings_WithInvalidData_ShouldReturnForm() throws Exception {
        // When/Then
        mockMvc.perform(post("/system/update")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("llmModel", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("system/settings"))
                .andExpect(model().hasErrors());

        verify(systemSettingsService).getCurrentSettings();
        verify(systemSettingsService, Mockito.never()).updateSettings(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSettings_WithoutSuperAdmin_ShouldFail() throws Exception {
        // Given
        String newModel = "gpt-4";

        // When/Then
        mockMvc.perform(post("/system/update")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("llmModel", newModel))
                .andExpect(status().isForbidden());

        // Verify no updates were made
        verify(systemSettingsService).getCurrentSettings();
    }
}

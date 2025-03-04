package ai.teamcollab.server.controller;

import ai.teamcollab.server.config.TestSecurityConfig;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.service.CompanyService;
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
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemController.class)
@Import({TestSecurityConfig.class, SystemControllerTest.TestConfig.class})
class SystemControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public SystemSettingsService systemSettingsService() {
            return mock(SystemSettingsService.class);
        }

        @Bean
        @Primary
        public CompanyService companyService() {
            return mock(CompanyService.class);
        }

        @Bean
        @Primary
        public MessageSource messageSource() {
            var messageSource = new ResourceBundleMessageSource();
            messageSource.setBasename("messages");
            messageSource.setDefaultEncoding("UTF-8");
            return messageSource;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SystemSettingsService systemSettingsService;

    @Autowired
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        Mockito.reset(systemSettingsService);
        Mockito.reset(companyService);

        when(systemSettingsService.getCurrentSettings())
            .thenReturn(SystemSettings.builder()
                .llmModel("gpt-3.5-turbo")
                .build());
        when(systemSettingsService.updateSettings(any(SystemSettings.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(companyService.getAllCompanies())
            .thenReturn(List.of(
                Company.builder()
                    .id(1L)
                    .name("Test Company 1")
                    .build(),
                Company.builder()
                    .id(2L)
                    .name("Test Company 2")
                    .build()
            ));
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

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void listCompanies_WithSuperAdmin_ShouldSucceed() throws Exception {
        // When/Then
        mockMvc.perform(get("/system/companies"))
                .andExpect(status().isOk())
                .andExpect(view().name("system/companies"))
                .andExpect(model().attributeExists("companies"))
                .andExpect(model().attribute("companies", companyService.getAllCompanies()));

        verify(companyService).getAllCompanies();
    }

    @Test
    @WithMockUser(roles = "USER")
    void listCompanies_WithoutSuperAdmin_ShouldFail() throws Exception {
        // When/Then
        mockMvc.perform(get("/system/companies"))
                .andExpect(status().isForbidden());

        verify(companyService, never()).getAllCompanies();
    }
}

package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.SystemSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemAdminController.class)
@Import({SystemAdminControllerTest.TestSecurityConfig.class, SystemAdminControllerTest.TestConfig.class})
class SystemAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private SystemSettingsService systemSettingsService;

    private Company mockCompany;
    private SystemSettings mockSystemSettings;

    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        public CompanyService companyService() {
            return mock(CompanyService.class);
        }

        @Bean
        public SystemSettingsService systemSettingsService() {
            return mock(SystemSettingsService.class);
        }

        @Bean
        public SystemAdminController systemAdminController(
                CompanyService companyService,
                SystemSettingsService systemSettingsService) {
            return new SystemAdminController(companyService, systemSettingsService);
        }
    }

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/system/admin/**").hasRole("SUPER_ADMIN")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        mockCompany = new Company();
        mockCompany.setId(1L);
        mockCompany.setName("Test Company");

        mockSystemSettings = new SystemSettings();
        mockSystemSettings.setLlmModel("gpt-3.5-turbo");
    }

    @Test
    @WithMockCompanyUser(username = "admin", roles = {"SUPER_ADMIN"}, companyId = 1L)
    void showCompanySettings_WhenUserIsSuperAdmin_ReturnsSettingsPage() throws Exception {
        // Arrange
        when(companyService.getCompanyById(1L)).thenReturn(mockCompany);
        when(systemSettingsService.getCurrentSettings()).thenReturn(mockSystemSettings);

        // Create CSRF token
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Act & Assert
        mockMvc.perform(get("/system/admin/companies/1/settings")
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().isOk())
                .andExpect(view().name("system/admin/company-settings"))
                .andExpect(model().attributeExists("company"))
                .andExpect(model().attributeExists("systemSettings"));
    }

    @Test
    @WithMockCompanyUser(username = "admin", roles = {"ADMIN"}, companyId = 1L)
    void showCompanySettings_WhenUserIsNotSuperAdmin_ReturnsForbidden() throws Exception {
        // Create CSRF token
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Act & Assert
        mockMvc.perform(get("/system/admin/companies/1/settings")
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCompanyUser(username = "admin", roles = {"SUPER_ADMIN"}, companyId = 1L)
    void updateCompanyLlmModel_WhenUserIsSuperAdmin_UpdatesCompanyLlmModel() throws Exception {
        // Arrange
        String newLlmModel = "gpt-4";
        when(companyService.updateCompanyLlmModel(1L, newLlmModel)).thenReturn(mockCompany);

        // Create CSRF token
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/system/admin/companies/1/settings/llm-model")
                .param("llmModel", newLlmModel)
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/system/admin/companies/1/settings"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockCompanyUser(username = "admin", roles = {"SUPER_ADMIN"}, companyId = 1L)
    void updateCompanyLlmModel_WhenResetToDefault_ClearsCompanyLlmModel() throws Exception {
        // Arrange
        when(companyService.updateCompanyLlmModel(1L, null)).thenReturn(mockCompany);

        // Create CSRF token
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/system/admin/companies/1/settings/llm-model")
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/system/admin/companies/1/settings"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
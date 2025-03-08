package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.UserStats;
import ai.teamcollab.server.service.RoleService;
import ai.teamcollab.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ai.teamcollab.server.controller.WithMockCompanyUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyAdminController.class)
@Import({CompanyAdminControllerTest.TestSecurityConfig.class, CompanyAdminControllerTest.TestConfig.class})
class CompanyAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private User mockUser;
    private Company mockCompany;

    @Configuration
    @EnableWebMvc
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public RoleService roleService() {
            return mock(RoleService.class);
        }

        @Bean
        public CompanyAdminController companyAdminController(
                UserService userService, 
                RoleService roleService) {
            return new CompanyAdminController(userService, roleService);
        }
    }

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/company/admin/**").hasRole("ADMIN")
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

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setCompany(mockCompany);
    }

    @Test
    @WithMockCompanyUser(username = "admin", roles = {"ADMIN"}, companyId = 1L)
    void dashboard_WhenUserIsAdmin_ReturnsAdminDashboard() throws Exception {
        // Arrange
        var stats = UserStats.builder()
                .activeUsers(3L)
                .disabledUsers(2L)
                .build();

        when(userService.getUserStats(anyLong())).thenReturn(stats);

        // Create CSRF token
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        // Act & Assert
        mockMvc.perform(get("/company/admin")
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().isOk())
                .andExpect(view().name("company/admin/dashboard"))
                .andExpect(model().attributeExists("stats"))
                .andExpect(model().attribute("stats", stats));
    }

    @Test
    @WithMockCompanyUser(username = "user", roles = {"USER"}, companyId = 1L)
    void dashboard_WhenUserIsNotAdmin_ReturnsForbidden() throws Exception {
        CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");

        mockMvc.perform(get("/company/admin")
                .sessionAttr("_csrf", csrfToken))
                .andExpect(status().isForbidden());
    }

}

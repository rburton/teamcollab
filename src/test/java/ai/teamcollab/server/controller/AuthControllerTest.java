package ai.teamcollab.server.controller;

import ai.teamcollab.server.config.AuthTestConfig;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Import(AuthTestConfig.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
            .standaloneSetup(authController)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void whenRegisterSuccessful_thenRedirectToDashboardAndAuthenticated() throws Exception {
        // Prepare test data
        Company company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setCompany(company);

        System.out.println("[DEBUG_LOG] Starting registration test");
        Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG_LOG] Initial security context: " + initialAuth);
        System.out.println("[DEBUG_LOG] Initial auth details: " + 
            (initialAuth != null ? 
                "Name: " + initialAuth.getName() + 
                ", Authorities: " + initialAuth.getAuthorities() +
                ", Authenticated: " + initialAuth.isAuthenticated()
                : "null"));

        // Mock service responses
        when(companyService.createCompany(any(Company.class)))
            .thenAnswer(invocation -> {
                Company arg = invocation.getArgument(0);
                System.out.println("[DEBUG_LOG] createCompany called with: " + arg);
                return company;
            });
        when(userService.registerNewUser(any(User.class), eq("USER")))
            .thenAnswer(invocation -> {
                User arg = invocation.getArgument(0);
                System.out.println("[DEBUG_LOG] registerNewUser called with: " + arg);
                return user;
            });

        // Perform registration request
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "testuser")
                .param("password", "password")
                .param("email", "test@example.com")
                .param("company.name", "Test Company")
                .param("company.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andDo(result -> System.out.println("[DEBUG_LOG] Response: " + result.getResponse().getContentAsString()));

        System.out.println("[DEBUG_LOG] Verifying authentication");

        // Verify authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG_LOG] Final authentication object: " + authentication);
        System.out.println("[DEBUG_LOG] Final auth details: " + 
            (authentication != null ? 
                "Name: " + authentication.getName() + 
                ", Authorities: " + authentication.getAuthorities() +
                ", Authenticated: " + authentication.isAuthenticated() +
                ", Principal: " + authentication.getPrincipal()
                : "null"));

        assertThat(authentication)
            .as("Authentication should not be null")
            .isNotNull();

        assertThat(authentication.getName())
            .as("Authentication name should match registered user")
            .isEqualTo("testuser");

        assertThat(authentication.getAuthorities())
            .as("User should have USER role")
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        assertThat(authentication.isAuthenticated())
            .as("User should be authenticated")
            .isTrue();

        System.out.println("[DEBUG_LOG] Authentication verified successfully");
    }

    @Test
    @WithMockUser(username = "existinguser", roles = "USER")
    void whenRegisterWithExistingUsername_thenRedirectToRegisterWithError() throws Exception {
        System.out.println("[DEBUG_LOG] Starting existing username test");
        Authentication initialAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG_LOG] Initial security context: " + initialAuth);
        System.out.println("[DEBUG_LOG] Initial auth details: " + 
            (initialAuth != null ? 
                "Name: " + initialAuth.getName() + 
                ", Authorities: " + initialAuth.getAuthorities() +
                ", Authenticated: " + initialAuth.isAuthenticated()
                : "null"));

        when(userService.registerNewUser(any(User.class), eq("USER")))
                .thenAnswer(invocation -> {
                    User arg = invocation.getArgument(0);
                    System.out.println("[DEBUG_LOG] registerNewUser called with: " + arg);
                    throw new IllegalArgumentException("Username already exists");
                });

        System.out.println("[DEBUG_LOG] Mocked service to throw exception");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .param("username", "existinguser")
                .param("password", "password")
                .param("email", "test@example.com")
                .param("company.name", "Test Company")
                .param("company.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "Username already exists"))
                .andDo(result -> System.out.println("[DEBUG_LOG] Error test response: " + result.getResponse().getContentAsString()));

        // Verify authentication remains unchanged
        Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("[DEBUG_LOG] Final authentication object: " + finalAuth);
        System.out.println("[DEBUG_LOG] Final auth details: " + 
            (finalAuth != null ? 
                "Name: " + finalAuth.getName() + 
                ", Authorities: " + finalAuth.getAuthorities() +
                ", Authenticated: " + finalAuth.isAuthenticated() +
                ", Principal: " + finalAuth.getPrincipal()
                : "null"));

        assertThat(finalAuth)
            .as("Authentication should not be null")
            .isNotNull();

        assertThat(finalAuth.getName())
            .as("Authentication name should remain unchanged")
            .isEqualTo("existinguser");

        assertThat(finalAuth.getAuthorities())
            .as("User should retain USER role")
            .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

        assertThat(finalAuth.isAuthenticated())
            .as("User should remain authenticated")
            .isTrue();
    }
}

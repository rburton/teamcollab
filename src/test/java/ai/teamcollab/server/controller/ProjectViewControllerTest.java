package ai.teamcollab.server.controller;

import ai.teamcollab.server.config.SecurityConfig;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.service.ProjectService;
import ai.teamcollab.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectViewControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ProjectService projectService() {
            return Mockito.mock(ProjectService.class);
        }

        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {

        User testUser = new User();
        Company company = new Company();
        company.setId(1L);
        testUser.setCompany(company);
        when(userService.loadUserByUsername(anyString())).thenReturn(testUser);
    }

    @Test
    @WithMockUser
    void index_WithAuthenticatedUser_ReturnsProjectsList() throws Exception {
        // Given
        ProjectResponse project = ProjectResponse.builder()
                .id(1L)
                .name("Test Project")
                .overview("Test Topic")
                .createdAt(LocalDateTime.now())
                .build();

        when(projectService.getProjectsByCompany(anyLong())).thenReturn(List.of(project));

        // When & Then
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/index"))
                .andExpect(model().attributeExists("projects"));
    }

    @Test
    void index_WithUnauthenticatedUser_RedirectsToLogin() throws Exception {
        // When & Then
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void newProject_WithAuthenticatedUser_ReturnsNewProjectForm() throws Exception {
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/new"))
                .andExpect(model().attributeExists("projectCreateRequest"));
    }

    @Test
    @WithMockUser
    void create_WithValidData_RedirectsToConversation() throws Exception {
        // Given
        ProjectResponse.ConversationResponse conversationResponse = ProjectResponse.ConversationResponse.builder()
                .id(1L)
                .purpose("Test Purpose")
                .createdAt(LocalDateTime.now())
                .build();

        ProjectResponse projectResponse = ProjectResponse.builder()
                .id(1L)
                .name("Test Project")
                .overview("Test Overview")
                .createdAt(LocalDateTime.now())
                .conversation(conversationResponse)
                .build();

        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(post("/projects/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Test Project")
                .param("overview", "Test Overview")
                .param("purpose", "Test Purpose"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations/1"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser
    void create_WithInvalidData_ReturnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/projects/create")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "") // Invalid: empty name
                .param("overview", "te") // Invalid: too short
                .param("purpose", "short")) // Invalid: too short
                .andExpect(status().isOk())
                .andExpect(view().name("projects/new"))
                .andExpect(model().attributeHasFieldErrors("projectCreateRequest", "name", "overview", "purpose"));
    }
}

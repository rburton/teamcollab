package ai.teamcollab.server.controller;

import ai.teamcollab.server.config.SecurityConfig;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.service.ProjectService;
import ai.teamcollab.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectViewController.class)
@Import(SecurityConfig.class)
class ProjectViewControllerTest {

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
                .topic("Test Topic")
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
}

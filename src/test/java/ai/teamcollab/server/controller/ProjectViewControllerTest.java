package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Project;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectResponse;

import java.time.LocalDateTime;
import ai.teamcollab.server.dto.ConversationCreateRequest;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProjectViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @Mock
    private ConversationService conversationService;

    @InjectMocks
    private ProjectViewController projectViewController;

    private User testUser;
    private Project testProject;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        // Set up company
        testCompany = new Company();
        testCompany.setId(1L);
        testCompany.setName("Test Company");

        // Set up user with company
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setCompany(testCompany);

        // Set up project with company
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setCompany(testCompany);

        // Reset mocks
        org.mockito.Mockito.reset(projectService, conversationService);

        var viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("classpath:/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(projectViewController)
                .setViewResolvers(viewResolver)
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .build();

        // Set up security context with test user
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                testUser, null, java.util.Collections.emptyList());
        var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
    }

    @Test
    void createConversation_Success() throws Exception {
        // Arrange
        when(projectService.getProjectEntityById(eq(1L), eq(1L))).thenReturn(testProject);

        Conversation savedConversation = new Conversation("Test Purpose", testUser);
        savedConversation.setId(1L);
        savedConversation.setProject(testProject);
        when(conversationService.createConversation(any(Conversation.class), eq(1L))).thenReturn(savedConversation);

        // Act & Assert
        mockMvc.perform(post("/projects/1/conversations")
                        .with(csrf())
                        .with(user(testUser))
                        .param("purpose", "Test Purpose")
                        .param("projectId", "1"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations/1"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify interactions
        org.mockito.Mockito.verify(projectService).getProjectEntityById(eq(1L), eq(1L));
        org.mockito.Mockito.verify(conversationService).createConversation(any(Conversation.class), eq(1L));
    }

    @Test
    void createConversation_ValidationError() throws Exception {
        // Arrange
        var projectResponse = ProjectResponse.builder()
                .id(1L)
                .name("Test Project")
                .overview("Test Overview")
                .createdAt(LocalDateTime.now())
                .build();
        when(projectService.getProjectById(eq(1L), eq(1L))).thenReturn(projectResponse);

        // Act & Assert
        mockMvc.perform(post("/projects/1/conversations")
                        .with(csrf())
                        .with(user(testUser))
                        .param("purpose", "short") // too short purpose
                        .param("projectId", "1"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("conversationCreateRequest", "purpose"))
                .andExpect(model().attributeExists("project"))
                .andExpect(view().name("projects/show"));

        // Verify interactions
        org.mockito.Mockito.verify(projectService).getProjectById(eq(1L), eq(1L));
        org.mockito.Mockito.verify(conversationService, org.mockito.Mockito.never()).createConversation(any(), any());
    }
}

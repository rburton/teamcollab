package ai.teamcollab.server.controller;

import ai.teamcollab.server.api.ProjectController;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.exception.GlobalExceptionHandler;
import ai.teamcollab.server.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ProjectCreateRequest validRequest;
    private ProjectResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(post("/").with(csrf()))
                .build();

        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        validRequest = new ProjectCreateRequest();
        validRequest.setName("Test Project");
        validRequest.setOverview("Test Topic");
        validRequest.setPurpose("This is a test project purpose with sufficient length");

        mockResponse = ProjectResponse.builder()
                .id(1L)
                .name(validRequest.getName())
                .overview(validRequest.getOverview())
                .createdAt(LocalDateTime.now())
                .conversation(ProjectResponse.ConversationResponse.builder()
                        .id(1L)
                        .purpose(validRequest.getPurpose())
                        .createdAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @Test
    @WithMockUser
    void createProject_ValidRequest_ReturnsCreated() throws Exception {
        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mockResponse.getId()))
                .andExpect(jsonPath("$.name").value(mockResponse.getName()))
                .andExpect(jsonPath("$.overview").value(mockResponse.getOverview()))
                .andExpect(jsonPath("$.conversation.id").value(mockResponse.getConversation().getId()))
                .andExpect(jsonPath("$.conversation.purpose").value(mockResponse.getConversation().getPurpose()));
    }

    @Test
    @WithMockUser
    void createProject_InvalidRequest_ReturnsBadRequest() throws Exception {
        ProjectCreateRequest invalidRequest = new ProjectCreateRequest();
        invalidRequest.setName(""); // Invalid name
        invalidRequest.setOverview(""); // Invalid topic
        invalidRequest.setPurpose("short"); // Invalid purpose

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.topic").exists())
                .andExpect(jsonPath("$.errors.purpose").exists());
    }

    @Test
    @WithMockUser
    void createProject_UserNotFound_ReturnsBadRequest() throws Exception {
        when(projectService.createProject(any(ProjectCreateRequest.class)))
                .thenThrow(new IllegalStateException("User not found"));

        mockMvc.perform(post("/api/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}

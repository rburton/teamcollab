package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Role;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.UserService;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@Ignore
@ExtendWith(MockitoExtension.class)
@WithMockUser(username = "user", roles = "USER")
public class ConversationControllerTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @InjectMocks
    private ConversationController conversationController;

    private User testUser;
    private Conversation testConversation;

    private RequestPostProcessor userAuth() {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
            return request;
        };
    }

    @BeforeEach
    void setUp() {
        testUser = new User("user", "password", "test@example.com");
        testUser.setId(1L);
        Role userRole = new Role("USER");
        userRole.setId(1L);
        testUser.addRole(userRole);

        testConversation = new Conversation("Test Topic", "Test Purpose", testUser.getId());
        testConversation.setId(1L);
        testConversation.setCreatedAt(LocalDateTime.now());

        mockMvc = MockMvcBuilders
                .standaloneSetup(conversationController)
                .defaultRequest(get("/").with(userAuth()))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

//    @Test
    void indexShouldDisplayConversationsAndForm() throws Exception {
        when(conversationService.getUserConversations(testUser.getId())).thenReturn(Collections.singletonList(testConversation));

        mockMvc.perform(get("/conversations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("conversations/index"))
                .andExpect(model().attributeExists("conversation"))
                .andExpect(model().attributeExists("conversations"));
    }

//    @Test
    void createShouldRedirectOnSuccess() throws Exception {
        when(conversationService.createConversation(any(Conversation.class), eq(testUser.getId())))
                .thenReturn(testConversation);

        mockMvc.perform(post("/conversations")
                        .with(csrf())
                        .with(userAuth())
                        .param("topic", "Test Topic")
                        .param("purpose", "Test Purpose"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void createShouldShowErrorsOnInvalidInput() throws Exception {
        mockMvc.perform(post("/conversations")
                        .with(csrf())
                        .with(userAuth())
                        .param("topic", "") // Invalid: empty topic
                        .param("purpose", "")) // Invalid: empty purpose
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.conversation"));
    }

//    @Test
    void createShouldHandleServiceException() throws Exception {
        when(conversationService.createConversation(any(Conversation.class), eq(testUser.getId())))
                .thenThrow(new IllegalArgumentException("Test error"));

        mockMvc.perform(post("/conversations")
                        .with(csrf())
                        .with(userAuth())
                        .param("topic", "Test Topic")
                        .param("purpose", "Test Purpose"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void showShouldDisplayConversation() throws Exception {
        when(conversationService.getConversationById(1L)).thenReturn(testConversation);

        mockMvc.perform(get("/conversations/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("conversations/show"))
                .andExpect(model().attributeExists("conversation"));
    }

//    @Test
    void showShouldDenyAccessToOtherUserConversation() throws Exception {
        User otherUser = new User("other", "password", "other@example.com");
        otherUser.setId(2L);
        Conversation otherConversation = new Conversation("Other Topic", "Other Purpose", otherUser.getId());
        otherConversation.setId(2L);

        when(conversationService.getConversationById(2L)).thenReturn(otherConversation);

        mockMvc.perform(get("/conversations/2")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assert exception instanceof IllegalArgumentException;
                    assert "Access denied".equals(exception.getMessage());
                });
    }

//    @Test
    void showShouldHandleNotFoundConversation() throws Exception {
        when(conversationService.getConversationById(999L))
                .thenThrow(new IllegalArgumentException("Conversation not found with id: 999"));

        mockMvc.perform(get("/conversations/999")
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assert exception instanceof IllegalArgumentException;
                    assert exception.getMessage().contains("Conversation not found");
                });
    }
}

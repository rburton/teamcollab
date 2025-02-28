package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Role;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import ai.teamcollab.server.config.TestConfig;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ConversationController.class)
@Import(TestConfig.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    private User user;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        user = new User();
        user.setId(1L);
        user.addRole(userRole);

        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser(user);

        message = new Message();
        message.setContent("Test message");

        // Set up mock responses
        when(conversationService.getConversationById(1L)).thenReturn(conversation);
    }

//    @Test
//    void show_Success() throws Exception {
//        when(messageService.getConversationMessages(1L))
//                .thenReturn(Arrays.asList(new Message(), new Message()));
//
//        mockMvc.perform(get("/conversations/1")
//                        .with(SecurityMockMvcRequestPostProcessors.user(user).roles("USER")))
//                .andExpect(status().isOk())
//                .andExpect(view().name("conversations/show"))
//                .andExpect(model().attributeExists("conversation"))
//                .andExpect(model().attributeExists("messages"))
//                .andExpect(model().attributeExists("newMessage"));
//
//        verify(conversationService).getConversationById(1L);
//        verify(messageService).getConversationMessages(1L);
//    }

    @Test
    void postMessage_Success() throws Exception {
        when(messageService.createMessage(any(Message.class), eq(1L), eq(user)))
                .thenReturn(message);

        mockMvc.perform(post("/conversations/1/messages")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .param("content", "Test message"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations/1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(messageService, times(1)).createMessage(any(Message.class), eq(1L), eq(user));
    }

    @Test
    void postMessage_ValidationError() throws Exception {
        mockMvc.perform(post("/conversations/1/messages")
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .param("content", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/conversations/1"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.newMessage"));
    }

//    @Test
//    void postMessage_UnauthorizedUser() throws Exception {
//        when(messageService.createMessage(any(Message.class), eq(1L), eq(user)))
//                .thenThrow(new IllegalArgumentException("User is not authorized"));
//
//        mockMvc.perform(post("/conversations/1/messages")
//                        .with(csrf())
//                        .with(SecurityMockMvcRequestPostProcessors.user(user).roles("USER"))
//                        .param("content", "Test message"))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/conversations/1"))
//                .andExpect(flash().attributeExists("errorMessage"));
//
//        verify(messageService).createMessage(any(Message.class), eq(1L), eq(user));
//    }
}

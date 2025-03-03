package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ai.teamcollab.server.domain.GptModel.GPT_3_5_TURBO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class MetricsViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MetricsService metricsService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageSource messageSource;

    private MetricsViewController metricsViewController;

    private Conversation conversation;
    private List<Message> messages;
    private User user;
    private Set<Persona> personas;

    @BeforeEach
    void setUp() {
        metricsViewController = new MetricsViewController(metricsService, messageSource, conversationService);
        mockMvc = MockMvcBuilders.standaloneSetup(metricsViewController).build();

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        Persona persona = new Persona();
        persona.setId(1L);
        persona.setName("Test Persona");
        personas = Set.of(persona);

        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setPurpose("Test Purpose");
        conversation.setUser(user);
        conversation.setPersonas(personas);
        conversation.setCreatedAt(LocalDateTime.now());

        Message message1 = new Message();
        message1.setId(1L);
        Metrics metrics1 = new Metrics();
        metrics1.setDuration(1000L);
        metrics1.setInputTokens(100);
        metrics1.setOutputTokens(150);
        message1.addMetrics(metrics1);

        Message message2 = new Message();
        message2.setId(2L);
        Metrics metrics2 = new Metrics();
        metrics2.setDuration(2000L);
        metrics2.setInputTokens(200);
        metrics2.setOutputTokens(250);
        message2.addMetrics(metrics2);

        messages = List.of(message1, message2);
    }

    @Test
    void showConversationMetrics_Success() throws Exception {
        when(conversationService.findConversationById(anyLong())).thenReturn(conversation);
        when(conversationService.findMessagesByConversation(anyLong())).thenReturn(messages);

        mockMvc.perform(get("/metrics/conversation/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("metrics/conversation"))
                .andExpect(model().attribute("conversation", conversation))
                .andExpect(model().attribute("messageCount", 2))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("personas", personas))
                .andExpect(model().attribute("totalDuration", 3000L))
                .andExpect(model().attribute("totalInputTokens", 300))
                .andExpect(model().attribute("totalOutputTokens", 400))
                .andExpect(model().attribute("inputTokensCost", "0.0003"))   // 300 tokens * $0.0010 per 1K
                .andExpect(model().attribute("outputTokensCost", "0.0008"))  // 400 tokens * $0.0020 per 1K
                .andExpect(model().attribute("totalCost", "0.0011"));        // 0.0003 + 0.0008
    }

    @Test
    void calculateInputTokensCost_Success() {
        var controller = new MetricsViewController(metricsService, messageSource, conversationService);
        var result = controller.calculateInputTokensCost(GPT_3_5_TURBO.getId(), 1000);
        var expected = new BigDecimal("0.0010").setScale(4, RoundingMode.HALF_UP);
        org.junit.jupiter.api.Assertions.assertEquals(0, result.compareTo(expected),
                "Expected " + expected + " but got " + result);
    }

    @Test
    void calculateOutputTokensCost_Success() {
        var controller = new MetricsViewController(metricsService, messageSource, conversationService);
        var result = controller.calculateOutputTokensCost(GPT_3_5_TURBO.getId(), 1000);
        var expected = new BigDecimal("0.0020").setScale(4, RoundingMode.HALF_UP);
        org.junit.jupiter.api.Assertions.assertEquals(0, result.compareTo(expected),
                "Expected " + expected + " but got " + result);
    }

    @Test
    void showConversationMetrics_Error() throws Exception {
        when(conversationService.findConversationById(anyLong()))
                .thenThrow(new IllegalArgumentException("Conversation not found"));
        when(messageSource.getMessage(any(), any(), any()))
                .thenReturn("Error fetching metrics for conversation 1: Conversation not found");

        mockMvc.perform(get("/metrics/conversation/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/metrics"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}

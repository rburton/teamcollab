package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Role;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

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

    private Company company;

    @BeforeEach
    void setUp() {
        metricsViewController = new MetricsViewController(metricsService, messageSource, conversationService);
        mockMvc = MockMvcBuilders.standaloneSetup(metricsViewController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setCompany(company);

        var superAdminRole = Role.builder()
                .id(1L)
                .name("SUPER_ADMIN")
                .build();
        user.addRole(superAdminRole);

        // Set up security context
        var authentication = new UsernamePasswordAuthenticationToken(user, null, 
            List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

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
        metrics1.setModel(GPT_3_5_TURBO.getId());
        message1.addMetrics(metrics1);

        Message message2 = new Message();
        message2.setId(2L);
        Metrics metrics2 = new Metrics();
        metrics2.setDuration(2000L);
        metrics2.setInputTokens(200);
        metrics2.setOutputTokens(250);
        metrics2.setModel(GPT_3_5_TURBO.getId());
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
                .andExpect(model().attribute("inputTokensCost", "0.0006"))   // 300 tokens * $0.0020 per 1K
                .andExpect(model().attribute("outputTokensCost", "0.0008"))  // 400 tokens * $0.0020 per 1K
                .andExpect(model().attribute("totalCost", "0.0014"));        // 0.0006 + 0.0008
    }

    @Test
    void calculateInputTokensCost_Success() {
        var result = metricsViewController.calculateInputTokensCost(GPT_3_5_TURBO.getId(), 1000);
        var expected = new BigDecimal("0.0020").setScale(4, RoundingMode.HALF_UP);
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

    @Test
    void showCompanyCosts_Success() throws Exception {
        var costs = new HashMap<String, BigDecimal>();
        costs.put("daily", new BigDecimal("10.5000"));
        costs.put("weekly", new BigDecimal("50.7500"));
        costs.put("monthly", new BigDecimal("150.2500"));

        when(metricsService.getCompanyCosts(1L)).thenReturn(costs);

        mockMvc.perform(get("/metrics/company/1/costs")
                    .principal(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(view().name("metrics/company-costs"))
                .andExpect(model().attribute("dailyCost", "10.5000"))
                .andExpect(model().attribute("weeklyCost", "50.7500"))
                .andExpect(model().attribute("monthlyCost", "150.2500"))
                .andExpect(model().attribute("companyId", 1L))
                .andExpect(model().attribute("companyName", "Test Company"));
    }

    @Test
    void showCompanyCosts_Unauthorized() throws Exception {
        var unauthorizedUser = new User();
        unauthorizedUser.setId(2L);
        var unauthorizedCompany = new Company();
        unauthorizedCompany.setId(2L);
        unauthorizedCompany.setName("Unauthorized Company");
        unauthorizedUser.setCompany(unauthorizedCompany);

        when(messageSource.getMessage(eq("metrics.error.unauthorized"), isNull(), any()))
                .thenReturn("You are not authorized to access this company's metrics");

        // Test with wrong role
        mockMvc.perform(get("/metrics/company/1/costs")
                    .principal(new UsernamePasswordAuthenticationToken(unauthorizedUser, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/metrics"))
                .andExpect(flash().attribute("errorMessage", "You are not authorized to access this company's metrics"));

        // Test with correct role but wrong company
        mockMvc.perform(get("/metrics/company/1/costs")
                    .principal(new UsernamePasswordAuthenticationToken(unauthorizedUser, null, List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/metrics"))
                .andExpect(flash().attribute("errorMessage", "You are not authorized to access this company's metrics"));
    }

    @Test
    void showCompanyCosts_Error() throws Exception {
        when(metricsService.getCompanyCosts(1L))
                .thenThrow(new RuntimeException("Failed to fetch company costs"));
        when(messageSource.getMessage(eq("metrics.error.company.costs.fetch"), any(), any()))
                .thenReturn("Error fetching costs for company 1: Failed to fetch company costs");

        mockMvc.perform(get("/metrics/company/1/costs")
                    .principal(new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/metrics"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}

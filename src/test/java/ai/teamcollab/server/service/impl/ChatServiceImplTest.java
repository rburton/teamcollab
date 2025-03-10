package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.exception.MonthlyLimitExceededException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.SystemSettingsService;
import ai.teamcollab.server.service.domain.ChatContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.openai.OpenAiChatModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SystemSettingsService systemSettingsService;

    @Mock
    private MetricsRepository metricsRepository;

    @Mock
    private OpenAiChatModel openAiChatModel;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(messageRepository, systemSettingsService, metricsRepository);
    }

    @Test
    void process_whenCompanyHasNoSpendingLimit_shouldNotThrowException() {
        // Arrange
        final var company = new Company();
        company.setId(1L);
        company.setName("Test Company");
        company.setMonthlySpendingLimit(null); // No spending limit

        final var user = new User();
        user.setId(1L);
        user.setCompany(company);

        final var conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser(user);

        final var message = new Message();
        message.setId(1L);
        message.setContent("Test message");
        message.setConversation(conversation);

        final var chatContext = ChatContext.builder()
                .purpose("Test purpose")
                .projectOverview("Test overview")
                .lastMessages(new ArrayList<>())
                .build();

        // No need to mock metrics repository as it shouldn't be called

        // Act & Assert - No exception should be thrown
        chatService.process(conversation, message, chatContext);
    }

    @Test
    void process_whenCompanyHasNotExceededLimit_shouldNotThrowException() {
        // Arrange
        final var company = new Company();
        company.setId(1L);
        company.setName("Test Company");
        company.setMonthlySpendingLimit(new BigDecimal("100.00")); // $100 limit

        final var user = new User();
        user.setId(1L);
        user.setCompany(company);

        final var conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser(user);

        final var message = new Message();
        message.setId(1L);
        message.setContent("Test message");
        message.setConversation(conversation);

        final var chatContext = ChatContext.builder()
                .purpose("Test purpose")
                .projectOverview("Test overview")
                .lastMessages(new ArrayList<>())
                .build();

        // Mock current month's spending to be less than the limit
        final var now = LocalDateTime.now();
        final var currentMonth = YearMonth.from(now);
        final var startDate = currentMonth.atDay(1).atStartOfDay();
        final var endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        final var metrics1 = new Metrics();
        metrics1.setModel("gpt-3.5-turbo");
        metrics1.setInputTokens(100);
        metrics1.setOutputTokens(200);

        final var metrics2 = new Metrics();
        metrics2.setModel("gpt-3.5-turbo");
        metrics2.setInputTokens(300);
        metrics2.setOutputTokens(400);

        when(metricsRepository.findByCompanyAndDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(metrics1, metrics2));

        // Act & Assert - No exception should be thrown
        chatService.process(conversation, message, chatContext);
    }

    @Test
    void process_whenCompanyHasExceededLimit_shouldThrowMonthlyLimitExceededException() {
        // Arrange
        final var company = new Company();
        company.setId(1L);
        company.setName("Test Company");
        company.setMonthlySpendingLimit(new BigDecimal("0.01")); // $0.01 limit (extremely low for testing)

        final var user = new User();
        user.setId(1L);
        user.setCompany(company);

        final var conversation = new Conversation();
        conversation.setId(1L);
        conversation.setUser(user);

        final var message = new Message();
        message.setId(1L);
        message.setContent("Test message");
        message.setConversation(conversation);

        final var chatContext = ChatContext.builder()
                .purpose("Test purpose")
                .projectOverview("Test overview")
                .lastMessages(new ArrayList<>())
                .build();

        // Mock current month's spending to exceed the limit
        final var metrics1 = Metrics.builder()
                .model("gpt-3.5-turbo")
                .inputTokens(10000)
                .outputTokens(20000)
                .build();

        final var metrics2 = Metrics.builder()
                .model("gpt-3.5-turbo")
                .inputTokens(30000)
                .outputTokens(40000)
                .build();

        // Mock the metricsRepository to return a cost that exceeds the limit
        when(metricsRepository.findByCompanyAndDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(metrics1, metrics2));

        // Act & Assert
        // Since we're testing that the monthly limit is exceeded, we can directly verify
        // that the checkMonthlySpendingLimit method throws a MonthlyLimitExceededException

        // We need to mock the calculateCurrentMonthSpending method to return a value that exceeds the limit
        // This is a private method, so we can't mock it directly, but we can verify that the
        // metricsRepository is called with the correct parameters and returns the metrics we set up

        // Let's directly test the ChatServiceImpl.checkMonthlySpendingLimit method by using reflection
        // to make it accessible
        try {
            // Get the checkMonthlySpendingLimit method
            java.lang.reflect.Method checkMethod = ChatServiceImpl.class.getDeclaredMethod("checkMonthlySpendingLimit", Company.class);
            checkMethod.setAccessible(true);

            // Call the method and expect it to throw a MonthlyLimitExceededException
            try {
                checkMethod.invoke(chatService, company);
                fail("Expected MonthlyLimitExceededException but no exception was thrown");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // The actual exception is wrapped in an InvocationTargetException
                assertTrue(e.getCause() instanceof MonthlyLimitExceededException,
                        "Expected MonthlyLimitExceededException but got " + e.getCause().getClass().getName());
            }

            // Verify that the metricsRepository was called with the correct parameters
            verify(metricsRepository).findByCompanyAndDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        } catch (Exception e) {
            fail("Failed to test checkMonthlySpendingLimit method: " + e.getMessage());
        }
    }
}

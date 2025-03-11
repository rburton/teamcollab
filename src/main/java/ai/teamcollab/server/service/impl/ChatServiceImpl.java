package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.GptModel;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.exception.MonthlyLimitExceededException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.ChatService;
import ai.teamcollab.server.service.SystemSettingsService;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    private final MessageRepository messageRepository;
    private final SystemSettingsService systemSettingsService;
    private final MetricsRepository metricsRepository;

    @Autowired
    public ChatServiceImpl(MessageRepository messageRepository, SystemSettingsService systemSettingsService, MetricsRepository metricsRepository) {
        this.messageRepository = messageRepository;
        this.systemSettingsService = systemSettingsService;
        this.metricsRepository = metricsRepository;
    }

    /**
     * Calculates the current month's spending for a company.
     *
     * @param company the company
     * @return the current month's spending as a BigDecimal
     */
    private BigDecimal calculateCurrentMonthSpending(Company company) {
        if (company == null) {
            return BigDecimal.ZERO;
        }

        // Get the start and end dates for the current month
        final var now = LocalDateTime.now();
        final var currentMonth = YearMonth.from(now);
        final var startDate = currentMonth.atDay(1).atStartOfDay();
        final var endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Get all metrics for the company for the current month
        final var metrics = metricsRepository.findByCompanyAndDateRange(company.getId(), startDate, endDate);

        // Calculate the total spending
        return metrics.stream()
                .map(Metrics::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if a company has exceeded its monthly spending limit.
     *
     * @param company the company
     * @throws MonthlyLimitExceededException if the company has exceeded its monthly spending limit
     */
    private void checkMonthlySpendingLimit(Company company) {
        if (company == null || company.getMonthlySpendingLimit() == null) {
            // No company or no limit set, so no limit to exceed
            log.debug("No company or no spending limit set, skipping check");
            return;
        }

        final var currentSpending = calculateCurrentMonthSpending(company);
        final var limit = company.getMonthlySpendingLimit();

        log.debug("Checking monthly spending limit: current={}, limit={}, comparison={}", 
                currentSpending, limit, currentSpending.compareTo(limit));

        if (currentSpending.compareTo(limit) >= 0) {
            log.warn("Company {} has exceeded its monthly spending limit of {}", company.getId(), limit);
            throw new MonthlyLimitExceededException(
                    "Monthly spending limit exceeded. Please contact your administrator to increase your limit.",
                    company.getId(),
                    company.getMonthlySpendingLimit().doubleValue(),
                    currentSpending.doubleValue()
            );
        } else {
            log.debug("Company {} has not exceeded its monthly spending limit of {}", company.getId(), limit);
        }
    }

    @Override
    public CompletableFuture<MessageResponse> process(Conversation conversation, Message recent, ChatContext chatContext) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(recent, "Message cannot be null");
        requireNonNull(chatContext, "ChatContext cannot be null");

        log.debug("Asynchronously processing message for conversation: {}, message: {}", conversation.getId(), recent.getId());
        log.debug("Using chat context - Purpose: {}, Project Overview: {}, History Size: {}",
                chatContext.getPurpose(), chatContext.getProjectOverview(), chatContext.getLastMessages().size());

        // Check if the assistant is muted in the conversation
        if (recent.getAssistant() != null && conversation.isAssistantMuted(recent.getAssistant())) {
            log.debug("Assistant {} is muted in conversation {}, skipping message processing", 
                    recent.getAssistant().getId(), conversation.getId());
            return CompletableFuture.completedFuture(MessageResponse.builder()
                    .content("This assistant is currently muted in this conversation.")
                    .build());
        }

        final var start = now();

        return CompletableFuture.supplyAsync(() -> {
            try {
                final var messages = new ArrayList<org.springframework.ai.chat.messages.Message>();

                // Add system message with context
                messages.add(new SystemMessage(String.format(
                        "Conversation Purpose: %s\nProject Overview: %s",
                        chatContext.getPurpose(),
                        chatContext.getProjectOverview()
                )));

                // Add conversation history
                for (final var historyMessage : chatContext.getLastMessages()) {
                    if (historyMessage.equals(recent)) {
                        continue; // Skip the current message
                    }
                    if (historyMessage.isAssistant()) {
                        messages.add(new AssistantMessage(historyMessage.getContent()));
                    } else {
                        messages.add(new UserMessage(historyMessage.getContent()));
                    }
                }

                messages.add(new UserMessage(recent.getContent()));

                log.debug("Conversation history size: {}", messages.size());
                final var prompt = new Prompt(messages.toArray(new org.springframework.ai.chat.messages.Message[0]));

                log.debug("Sending prompt to OpenAI: {}", prompt);

                final var currentSettings = systemSettingsService.getCurrentSettings();

                final var company = Optional.ofNullable(conversation)
                        .map(Conversation::getUser)
                        .map(User::getCompany)
                        .orElse(null);

                // Check if the company has exceeded its monthly spending limit
                checkMonthlySpendingLimit(company);

                final var llmModel = Optional.ofNullable(company)
                        .map(Company::getLlmModel)
                        .filter(Strings::isNotBlank)
                        .orElse(currentSettings.getLlmModel());

                final var model = GptModel.fromId(llmModel);
                final var chatModel = OpenAiChatModel.builder()
                        .openAiApi(OpenAiApi.builder()
                                .apiKey(System.getenv("OPENAI_API_KEY"))
                                .build())
                        .defaultOptions(OpenAiChatOptions.builder()
                                .model(model.getId())
                                .temperature(model.getTemperature())
                                .build())
                        .build();
                final var aiResponse = chatModel.call(prompt);
                final var assistantMessage = aiResponse.getResult().getOutput();
                final var response = assistantMessage.getText();

                final var end = now();
                final var duration = Duration.between(start, end).toMillis();

                final var metadata = aiResponse.getMetadata();
                final var usage = metadata.getUsage();

                final var metrics = Metrics.builder()
                        .duration(duration)
                        .inputTokens(usage.getPromptTokens())
                        .outputTokens(usage.getCompletionTokens())
                        .provider("OpenAI")
                        .model(llmModel)
                        .build();

                recent.addMetrics(metrics);
                messageRepository.save(recent);

                log.debug("Received response from OpenAI: {}", response);

                return MessageResponse.builder()
                        .content(response)
                        .metrics(metrics)
                        .build();

            } catch (IllegalArgumentException e) {
                log.error("Invalid argument while processing message: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Invalid input for message processing", e);
            } catch (Exception e) {
                log.error("Error processing message: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to process message: " + e.getMessage(), e);
            }
        });
    }

}

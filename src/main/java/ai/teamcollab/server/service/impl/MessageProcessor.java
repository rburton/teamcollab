package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.exception.MonthlyLimitExceededException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;

/**
 * Responsible for processing chat messages.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor {
    private final MessageRepository messageRepository;
    private final AiModelFactory aiModelFactory;
    private final PromptBuilder promptBuilder;
    private final MetricsRepository metricsRepository;

    /**
     * Processes a message and returns a response.
     *
     * @param conversation the conversation
     * @param recent the recent message to process
     * @param chatContext the chat context
     * @return a CompletableFuture containing the message response
     */
    public CompletableFuture<MessageResponse> process(Conversation conversation, Message recent, ChatContext chatContext) {
        requireNonNull(conversation, "Conversation cannot be null");
        requireNonNull(recent, "Message cannot be null");
        requireNonNull(chatContext, "ChatContext cannot be null");

        log.debug("Asynchronously processing message for conversation: {}, message: {}", conversation.getId(), recent.getId());
        log.debug("Using chat context - Purpose: {}, Project Overview: {}, History Size: {}",
                chatContext.getPurpose(), chatContext.getProjectOverview(), chatContext.getLastMessages().size());

        final var start = now();

        final var company = Optional.ofNullable(conversation)
                .map(Conversation::getUser)
                .map(User::getCompany)
                .orElse(null);

        // Check if the company has exceeded its monthly spending limit
        checkMonthlySpendingLimit(company);

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build the prompt
                final var prompt = promptBuilder.buildMessagePrompt(chatContext, recent);
                log.debug("Sending prompt to OpenAI: {}", prompt);

                // Get the AI model
                final var chatModel = aiModelFactory.createModel(conversation);
                final var modelId = aiModelFactory.getModelId(conversation);
                
                // Call the AI model
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
                        .model(modelId)
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

}
package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.MetricCache;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.exception.MonthlyLimitExceededException;
import ai.teamcollab.server.repository.MessageRepository;
import ai.teamcollab.server.repository.MetricCacheRepository;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.domain.ChatContext;
import ai.teamcollab.server.service.domain.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MetricCacheRepository metricCacheRepository;

    /**
     * Processes a message and returns a response.
     *
     * @param conversation the conversation
     * @param recent       the recent message to process
     * @param chatContext  the chat context
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

        final var company = Optional.of(conversation)
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
                final var llmModel = aiModelFactory.getActiveLlmModel(conversation);

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
                        .llmModel(llmModel)
                        .build();

                recent.addMetrics(metrics);
                messageRepository.save(recent);

                // Update the metric cache for this conversation, provider, and model
                updateMetricCache(conversation, metrics);

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

    /**
     * Updates the metric cache for a conversation and LLM model. If a cache entry doesn't exist for the given
     * combination, a new one is created.
     *
     * @param conversation the conversation
     * @param metrics      the metrics to add to the cache
     */
    private void updateMetricCache(Conversation conversation, Metrics metrics) {
        if (conversation == null || metrics == null || metrics.getLlmModel() == null) {
            log.warn("Cannot update metric cache: conversation, metrics, or llmModel is null");
            return;
        }

        final var llmModel = metrics.getLlmModel();

        log.debug("Updating metric cache for conversation {}, llmModel {}",
                conversation.getId(), llmModel.getModelId());

        // Try to find an existing cache entry
        final var metricCacheOpt = metricCacheRepository.findByConversationAndLlmModel(
                conversation.getId(), llmModel.getId());

        if (metricCacheOpt.isPresent()) {
            // Update existing cache using SQL to avoid race conditions
            final var metricCache = metricCacheOpt.get();
            final var rowsUpdated = metricCacheRepository.incrementMetricsById(
                    metricCache.getId(),
                    metrics.getDuration(),
                    metrics.getInputTokens(),
                    metrics.getOutputTokens()
            );
            log.debug("Updated existing metric cache: {}, rows affected: {}", metricCache.getId(), rowsUpdated);
        } else {
            // Create new cache
            final var now = LocalDateTime.now();
            final var metricCache = MetricCache.builder()
                    .conversation(conversation)
                    .llmModel(llmModel)
                    .totalDuration(metrics.getDuration())
                    .messageCount(1)
                    .totalInputTokens(metrics.getInputTokens())
                    .totalOutputTokens(metrics.getOutputTokens())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            metricCacheRepository.save(metricCache);
            log.debug("Created new metric cache: {}", metricCache.getId());
        }
    }

}

package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.MetricCache;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.MetricCacheRepository;
import ai.teamcollab.server.repository.PointInTimeSummaryRepository;
import ai.teamcollab.server.repository.UserRepository;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Slf4j
@Controller
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsViewController {

    private final MetricsService metricsService;
    private final MessageSource messageSource;
    private final ConversationService conversationService;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PointInTimeSummaryRepository pointInTimeSummaryRepository;
    private final MetricCacheRepository metricCacheRepository;


    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            final var pageable = PageRequest.of(page, size);
            final var metricsPage = metricsService.getMetricsPaginated(pageable);
            final var statistics = metricsService.getMetricsStatistics();

            // Add company and user counts to statistics
            statistics.put("totalCompanies", companyRepository.count());
            statistics.put("totalUsers", userRepository.count());

            model.addAttribute("metrics", metricsPage.getContent());
            model.addAttribute("statistics", statistics);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", metricsPage.getTotalPages());
            model.addAttribute("totalItems", metricsPage.getTotalElements());
            model.addAttribute("pageSize", size);

            return "metrics/index";
        } catch (Exception e) {
            log.error("Error fetching metrics", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("metrics.error.fetch", new Object[]{e.getMessage()}, LocaleContextHolder.getLocale()));
            return "redirect:/";
        }
    }

    @GetMapping("/conversation/{id}")
    public String showConversationMetrics(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            var conversation = conversationService.findConversationById(id);

            // Count point-in-time summaries for this conversation
            long summaryCount = pointInTimeSummaryRepository.countByConversation(conversation);

            model.addAttribute("conversation", conversation);
            model.addAttribute("messageCount", conversation.getMessageCacheCounter());
            model.addAttribute("summaryCount", summaryCount);
            model.addAttribute("user", conversation.getUser());
            model.addAttribute("assistants", conversation.getAssistants());

            // Get metrics from MetricCache
            var metricCaches = metricCacheRepository.findByConversationId(id);

            log.debug("Found {} metric cache entries for conversation {}", metricCaches.size(), id);

            // Aggregate metrics from MetricCache
            var totalDuration = metricCaches.stream()
                    .mapToLong(MetricCache::getTotalDuration)
                    .sum();
            var totalInputTokens = metricCaches.stream()
                    .mapToInt(MetricCache::getTotalInputTokens)
                    .sum();
            var totalOutputTokens = metricCaches.stream()
                    .mapToInt(MetricCache::getTotalOutputTokens)
                    .sum();

            // Calculate costs based on each model's tokens
            var inputTokensCost = metricCaches.stream()
                    .map(cache -> calculateInputTokensCost(cache.getLlmModel(), cache.getTotalInputTokens()))
                    .reduce(ZERO, BigDecimal::add);
            var outputTokensCost = metricCaches.stream()
                    .map(cache -> calculateOutputTokensCost(cache.getLlmModel(), cache.getTotalOutputTokens()))
                    .reduce(ZERO, BigDecimal::add);

            var totalCost = inputTokensCost.add(outputTokensCost);

            model.addAttribute("totalDuration", totalDuration);
            model.addAttribute("totalInputTokens", totalInputTokens);
            model.addAttribute("totalOutputTokens", totalOutputTokens);
            model.addAttribute("inputTokensCost", inputTokensCost.setScale(4, HALF_UP).toString());
            model.addAttribute("outputTokensCost", outputTokensCost.setScale(4, HALF_UP).toString());
            model.addAttribute("totalCost", totalCost.setScale(4, HALF_UP).toString());

            return "metrics/conversation";
        } catch (Exception e) {
            log.error("Error fetching conversation metrics for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("metrics.error.conversation.fetch",
                            new Object[]{id, e.getMessage()}, LocaleContextHolder.getLocale()));
            return "redirect:/metrics";
        }
    }

    @GetMapping("/company/{companyId}/costs")
    public String showCompanyCosts(@PathVariable Long companyId,
                                   @AuthenticationPrincipal LoginUserDetails currentUser,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Verify user and company access
            if (Objects.isNull(currentUser) || Objects.isNull(currentUser.getCompanyId()) ||
                    !currentUser.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                log.warn("Unauthorized access attempt for company {}", companyId);
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("metrics.error.unauthorized",
                                null, LocaleContextHolder.getLocale()));
                return "redirect:/metrics";
            }

            // Verify user has access to the company
            if (!currentUser.getCompanyId().equals(companyId)) {
                log.warn("User {} attempted to access costs for company {}", currentUser.getId(), companyId);
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("metrics.error.unauthorized",
                                null, LocaleContextHolder.getLocale()));
                return "redirect:/metrics";
            }

            var costs = metricsService.getCompanyCosts(companyId);

            // Handle null values in costs map
            var dailyCost = costs.getOrDefault("daily", ZERO).setScale(4, HALF_UP);
            var weeklyCost = costs.getOrDefault("weekly", ZERO).setScale(4, HALF_UP);
            var monthlyCost = costs.getOrDefault("monthly", ZERO).setScale(4, HALF_UP);

            model.addAttribute("dailyCost", dailyCost.toString());
            model.addAttribute("weeklyCost", weeklyCost.toString());
            model.addAttribute("monthlyCost", monthlyCost.toString());
            model.addAttribute("companyId", companyId);
            model.addAttribute("companyName", currentUser.getCompanyName());

            return "metrics/company-costs";
        } catch (Exception e) {
            log.error("Error fetching company costs for company ID: {}", companyId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("metrics.error.company.costs.fetch",
                            new Object[]{companyId, e.getMessage()}, LocaleContextHolder.getLocale()));
            return "redirect:/metrics";
        }
    }

    // Package-private for testing
    BigDecimal calculateInputTokensCost(LlmModel model, int tokens) {
        return model.calculate(tokens, 0);
    }

    // Package-private for testing
    BigDecimal calculateOutputTokensCost(LlmModel model, int tokens) {
        return model.calculate(0, tokens);
    }

}

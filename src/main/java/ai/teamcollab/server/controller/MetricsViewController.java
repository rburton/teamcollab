package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.GptModel;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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


    @GetMapping
    public String index(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Metrics> topMetrics = metricsService.getTopMetrics();
            Map<String, Object> statistics = metricsService.getMetricsStatistics();

            model.addAttribute("metrics", topMetrics);
            model.addAttribute("statistics", statistics);

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
            var messages = conversationService.findMessagesByConversation(id);

            model.addAttribute("conversation", conversation);
            model.addAttribute("messageCount", messages.size());
            model.addAttribute("user", conversation.getUser());
            model.addAttribute("personas", conversation.getPersonas());

            // Aggregate metrics
            var totalDuration = messages.stream()
                    .map(Message::getMetrics)
                    .filter(Objects::nonNull)
                    .mapToLong(Metrics::getDuration)
                    .sum();
            var totalInputTokens = messages.stream()
                    .map(Message::getMetrics)
                    .filter(Objects::nonNull)
                    .mapToInt(Metrics::getInputTokens)
                    .sum();
            var totalOutputTokens = messages.stream()
                    .map(Message::getMetrics)
                    .filter(Objects::nonNull)
                    .mapToInt(Metrics::getOutputTokens)
                    .sum();

            var inputTokensCost = messages.stream()
                    .map(Message::getMetrics)
                    .filter(Objects::nonNull)
                    .map(message -> calculateInputTokensCost(message.getModel(), message.getInputTokens()))
                    .reduce(ZERO, BigDecimal::add);
            var outputTokensCost = messages.stream()
                    .map(Message::getMetrics)
                    .filter(Objects::nonNull)
                    .map(message -> calculateOutputTokensCost(message.getModel(), message.getOutputTokens()))
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String showCompanyCosts(@PathVariable Long companyId, 
                                 @AuthenticationPrincipal User currentUser,
                                 Model model, 
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verify user has access to the company
            if (!currentUser.getCompany().getId().equals(companyId)) {
                log.warn("User {} attempted to access costs for company {}", currentUser.getId(), companyId);
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("metrics.error.unauthorized",
                                null, LocaleContextHolder.getLocale()));
                return "redirect:/metrics";
            }

            var costs = metricsService.getCompanyCosts(companyId);

            model.addAttribute("dailyCost", costs.get("daily").setScale(4, HALF_UP).toString());
            model.addAttribute("weeklyCost", costs.get("weekly").setScale(4, HALF_UP).toString());
            model.addAttribute("monthlyCost", costs.get("monthly").setScale(4, HALF_UP).toString());
            model.addAttribute("companyId", companyId);
            model.addAttribute("companyName", currentUser.getCompany().getName());

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
    BigDecimal calculateInputTokensCost(String model, int tokens) {
        return GptModel.fromId(model).calculate(tokens, 0);
    }

    // Package-private for testing
    BigDecimal calculateOutputTokensCost(String model, int tokens) {
        return GptModel.fromId(model).calculate(0, tokens);
    }

}

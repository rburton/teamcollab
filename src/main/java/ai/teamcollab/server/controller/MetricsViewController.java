package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.service.MetricsService;
import ai.teamcollab.server.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsViewController {

    // GPT-3.5-turbo pricing (per 1K tokens)
    private static final BigDecimal INPUT_TOKEN_PRICE = new BigDecimal("0.0010");   // $0.0010 per 1K input tokens
    private static final BigDecimal OUTPUT_TOKEN_PRICE = new BigDecimal("0.0020");  // $0.0020 per 1K output tokens
    private static final BigDecimal TOKEN_DIVISOR = new BigDecimal("1000");         // Price is per 1K tokens

    private final MetricsService metricsService;
    private final MessageSource messageSource;
    private final ConversationService conversationService;

    // Package-private for testing
    BigDecimal calculateInputTokensCost(int tokens) {
        return new BigDecimal(tokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(INPUT_TOKEN_PRICE);
    }

    // Package-private for testing
    BigDecimal calculateOutputTokensCost(int tokens) {
        return new BigDecimal(tokens)
                .divide(TOKEN_DIVISOR, 6, RoundingMode.HALF_UP)
                .multiply(OUTPUT_TOKEN_PRICE);
    }

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
                .filter(java.util.Objects::nonNull)
                .mapToLong(Metrics::getDuration)
                .sum();
            var totalInputTokens = messages.stream()
                .map(Message::getMetrics)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Metrics::getInputTokens)
                .sum();
            var totalOutputTokens = messages.stream()
                .map(Message::getMetrics)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Metrics::getOutputTokens)
                .sum();

            // Calculate costs
            var inputTokensCost = calculateInputTokensCost(totalInputTokens);
            var outputTokensCost = calculateOutputTokensCost(totalOutputTokens);
            var totalCost = inputTokensCost.add(outputTokensCost);

            model.addAttribute("totalDuration", totalDuration);
            model.addAttribute("totalInputTokens", totalInputTokens);
            model.addAttribute("totalOutputTokens", totalOutputTokens);
            model.addAttribute("inputTokensCost", inputTokensCost.setScale(4, RoundingMode.HALF_UP).toString());
            model.addAttribute("outputTokensCost", outputTokensCost.setScale(4, RoundingMode.HALF_UP).toString());
            model.addAttribute("totalCost", totalCost.setScale(4, RoundingMode.HALF_UP).toString());

            return "metrics/conversation";
        } catch (Exception e) {
            log.error("Error fetching conversation metrics for id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                messageSource.getMessage("metrics.error.conversation.fetch", 
                    new Object[]{id, e.getMessage()}, LocaleContextHolder.getLocale()));
            return "redirect:/metrics";
        }
    }
}

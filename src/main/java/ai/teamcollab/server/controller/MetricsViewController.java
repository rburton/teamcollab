package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class MetricsViewController {

    private final MetricsService metricsService;
    private final MessageSource messageSource;

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
}

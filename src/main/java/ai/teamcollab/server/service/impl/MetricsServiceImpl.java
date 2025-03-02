package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MetricsRepository metricsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Metrics> getTopMetrics() {
        log.debug("Fetching top 10 metrics");
        return metricsRepository.findTop10ByOrderByDurationDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMetricsStatistics() {
        log.debug("Calculating metrics statistics");
        
        List<Metrics> allMetrics = metricsRepository.findAll();
        Map<String, Object> statistics = new HashMap<>();
        
        if (allMetrics.isEmpty()) {
            return statistics;
        }

        // Calculate average duration
        double avgDuration = allMetrics.stream()
                .mapToLong(Metrics::getDuration)
                .average()
                .orElse(0.0);

        // Calculate total tokens
        int totalInputTokens = allMetrics.stream()
                .mapToInt(Metrics::getInputTokens)
                .sum();
        
        int totalOutputTokens = allMetrics.stream()
                .mapToInt(Metrics::getOutputTokens)
                .sum();

        // Find most used model
        String mostUsedModel = allMetrics.stream()
                .map(Metrics::getModel)
                .collect(java.util.stream.Collectors.groupingBy(
                        model -> model,
                        java.util.stream.Collectors.counting()
                ))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        statistics.put("averageDuration", avgDuration);
        statistics.put("totalInputTokens", totalInputTokens);
        statistics.put("totalOutputTokens", totalOutputTokens);
        statistics.put("mostUsedModel", mostUsedModel);

        return statistics;
    }
}
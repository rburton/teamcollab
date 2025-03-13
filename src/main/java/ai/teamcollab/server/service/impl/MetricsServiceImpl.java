package ai.teamcollab.server.service.impl;

import ai.teamcollab.server.domain.MetricCache;
import ai.teamcollab.server.domain.Metrics;
import ai.teamcollab.server.repository.MetricCacheRepository;
import ai.teamcollab.server.repository.MetricsRepository;
import ai.teamcollab.server.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private final MetricsRepository metricsRepository;
    private final MetricCacheRepository metricCacheRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Metrics> getTopMetrics() {
        log.debug("Fetching top 10 metrics");
        return metricsRepository.findTop10ByOrderByDurationDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Metrics> getMetricsPaginated(Pageable pageable) {
        log.debug("Fetching paginated metrics with pageable: {}", pageable);
        return metricsRepository.findAllOrderByMessageCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMetricsStatistics() {
        log.debug("Calculating metrics statistics using MetricCache");

        List<MetricCache> allMetricCaches = metricCacheRepository.findAll();
        Map<String, Object> statistics = new HashMap<>();

        if (allMetricCaches.isEmpty()) {
            return statistics;
        }

        // Calculate average duration
        double avgDuration = allMetricCaches.stream()
                .mapToLong(MetricCache::getTotalDuration)
                .sum() / (double) allMetricCaches.stream()
                .mapToInt(MetricCache::getMessageCount)
                .sum();

        // Calculate total tokens
        int totalInputTokens = allMetricCaches.stream()
                .mapToInt(MetricCache::getTotalInputTokens)
                .sum();

        int totalOutputTokens = allMetricCaches.stream()
                .mapToInt(MetricCache::getTotalOutputTokens)
                .sum();

        // Find most used model
        String mostUsedModel = allMetricCaches.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        MetricCache::getModel,
                        java.util.stream.Collectors.summingInt(MetricCache::getMessageCount)
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

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getCompanyCosts(Long companyId) {
        log.debug("Calculating company costs for company ID: {}", companyId);

        Map<String, BigDecimal> costs = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Calculate daily costs (last 24 hours)
        LocalDateTime dayStart = now.minusHours(24);
        costs.put("daily", getCompanyCostsByDateRange(companyId, dayStart, now));

        // Calculate weekly costs (last 7 days)
        LocalDateTime weekStart = now.minusDays(7);
        costs.put("weekly", getCompanyCostsByDateRange(companyId, weekStart, now));

        // Calculate monthly costs (last 30 days)
        LocalDateTime monthStart = now.minusDays(30);
        costs.put("monthly", getCompanyCostsByDateRange(companyId, monthStart, now));

        return costs;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCompanyCostsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculating company costs for company ID: {} between {} and {}", companyId, startDate, endDate);

        List<Metrics> metrics = metricsRepository.findByCompanyAndDateRange(companyId, startDate, endDate);

        return metrics.stream()
                .map(Metrics::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

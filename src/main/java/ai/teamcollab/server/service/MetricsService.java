package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Metrics;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface MetricsService {
    /**
     * Retrieves the top 10 metrics ordered by duration.
     * @return List of top 10 metrics
     */
    List<Metrics> getTopMetrics();

    /**
     * Retrieves metrics statistics including:
     * - Average duration
     * - Total input tokens
     * - Total output tokens
     * - Most used model
     * @return Map of metric statistics
     */
    java.util.Map<String, Object> getMetricsStatistics();

    /**
     * Retrieves company costs grouped by time period
     * @param companyId the ID of the company
     * @return Map containing costs by day, week, and month
     */
    java.util.Map<String, BigDecimal> getCompanyCosts(Long companyId);

    /**
     * Retrieves company costs for a specific date range
     * @param companyId the ID of the company
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return total cost for the specified period
     */
    BigDecimal getCompanyCostsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
}

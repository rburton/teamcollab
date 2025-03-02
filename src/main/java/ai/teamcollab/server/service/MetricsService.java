package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Metrics;
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
}
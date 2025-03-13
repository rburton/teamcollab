package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@Entity
@Builder
@Table(name = "metric_cache")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class MetricCache {

    @Id
    @Column(name = "metric_cache_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "llm_model_id", nullable = false)
    private LlmModel llmModel;

    @Column(name = "total_duration", nullable = false)
    private long totalDuration;

    @Column(name = "message_count", nullable = false)
    private int messageCount;

    @Column(name = "total_input_tokens", nullable = false)
    private int totalInputTokens;

    @Column(name = "total_output_tokens", nullable = false)
    private int totalOutputTokens;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Increments the metrics in this cache based on the provided metrics.
     *
     * @param metrics the metrics to add to this cache
     */
    public void incrementMetrics(Metrics metrics) {
        this.totalDuration += metrics.getDuration();
        this.messageCount++;
        this.totalInputTokens += metrics.getInputTokens();
        this.totalOutputTokens += metrics.getOutputTokens();
        this.updatedAt = LocalDateTime.now();
    }
}

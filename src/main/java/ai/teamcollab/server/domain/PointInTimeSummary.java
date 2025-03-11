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

/**
 * Represents a point-in-time summary of a conversation.
 * Contains information about topics discussed, key points, and summaries for each assistant.
 */
@Getter
@Setter
@Entity
@Builder
@Table(name = "point_in_time_summaries")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PointInTimeSummary {

    @Id
    @Column(name = "summary_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Message latestMessage;

    @Column(name = "topics_and_key_points", columnDefinition = "TEXT")
    private String topicsAndKeyPoints;

    @Column(name = "topic_summaries", columnDefinition = "TEXT")
    private String topicSummaries;

    @Column(name = "assistant_summaries", columnDefinition = "TEXT")
    private String assistantSummaries;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Creates a new point-in-time summary with the current timestamp.
     */
    public static PointInTimeSummary create(Conversation conversation, Message latestMessage,
                                           String topicsAndKeyPoints, String topicSummaries,
                                           String assistantSummaries) {
        return PointInTimeSummary.builder()
                .conversation(conversation)
                .latestMessage(latestMessage)
                .topicsAndKeyPoints(topicsAndKeyPoints)
                .topicSummaries(topicSummaries)
                .assistantSummaries(assistantSummaries)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
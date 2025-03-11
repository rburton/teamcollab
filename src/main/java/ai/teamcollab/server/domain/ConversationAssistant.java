package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents the relationship between a conversation and an assistant,
 * including additional attributes like muted status.
 */
@Entity
@Table(name = "conversation_assistant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"conversation", "assistant"})
public class ConversationAssistant {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "assistant_id")
    private Assistant assistant;

    @Column(name = "muted")
    private boolean muted;

    @Enumerated(EnumType.STRING)
    @Column(name = "tone")
    private AssistantTone tone;

    public ConversationAssistant(Conversation conversation, Assistant assistant) {
        this.conversation = conversation;
        this.assistant = assistant;
        this.muted = false;
        this.tone = AssistantTone.FORMAL; // Default tone is formal
    }
}

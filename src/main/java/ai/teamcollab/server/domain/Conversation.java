package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"messages", "assistants"})
public class Conversation {

    @Id
    @Column(name = "conversation_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "purpose")
    @NotBlank(message = "Purpose is required")
    @Size(min = 1, max = 1000, message = "Purpose must be between 1 and 1000 characters")
    private String purpose;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "conversation", cascade = ALL, orphanRemoval = true)
    private Set<Message> messages = new HashSet<>();

    @OneToMany(mappedBy = "conversation", cascade = ALL, orphanRemoval = true)
    private Set<ConversationAssistant> conversationAssistants = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Conversation(String purpose, User createdBy) {
        this.purpose = purpose;
        this.user = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void addAssistant(Assistant assistant) {
        ConversationAssistant conversationAssistant = new ConversationAssistant(this, assistant);
        conversationAssistants.add(conversationAssistant);
    }

    public Set<Assistant> getAssistants() {
        return conversationAssistants.stream()
                .map(ConversationAssistant::getAssistant)
                .collect(Collectors.toSet());
    }

    public void muteAssistant(Assistant assistant) {
        conversationAssistants.stream()
                .filter(ca -> ca.getAssistant().equals(assistant))
                .findFirst()
                .ifPresent(ca -> ca.setMuted(true));
    }

    public void unmuteAssistant(Assistant assistant) {
        conversationAssistants.stream()
                .filter(ca -> ca.getAssistant().equals(assistant))
                .findFirst()
                .ifPresent(ca -> ca.setMuted(false));
    }

    public boolean isAssistantMuted(Assistant assistant) {
        return conversationAssistants.stream()
                .filter(ca -> ca.getAssistant().equals(assistant))
                .findFirst()
                .map(ConversationAssistant::isMuted)
                .orElse(false);
    }
}

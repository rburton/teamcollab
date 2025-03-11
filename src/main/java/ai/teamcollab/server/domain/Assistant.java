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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "assistants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Assistant {

    @Id
    @Column(name = "assistant_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(name = "expertise_areas")
    @NotBlank(message = "At least one area of expertise is required")
    @Size(min = 1, message = "At least one area of expertise is required")
    private String expertise;

    @Column(name = "expertise_prompt")
    @NotBlank(message = "Please describe the role to guide the behavior of the assistant")
    @Size(min = 1, message = "The expertise prompt is too short")
    private String expertisePrompt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToMany(mappedBy = "assistant")
    private Set<ConversationAssistant> conversationAssistants = new HashSet<>();

    public Assistant(String name, String expertise) {
        this.name = name;
        this.expertise = expertise;
    }

    public void addToConversation(Conversation conversation) {
        conversation.addAssistant(this);
    }

    public void removeFromConversation(Conversation conversation) {
        conversationAssistants.removeIf(ca -> ca.getConversation().equals(conversation));
    }

    public Set<Conversation> getConversations() {
        return conversationAssistants.stream()
                .map(ConversationAssistant::getConversation)
                .collect(Collectors.toSet());
    }

    public void muteInConversation(Conversation conversation) {
        conversationAssistants.stream()
                .filter(ca -> ca.getConversation().equals(conversation))
                .findFirst()
                .ifPresent(ca -> ca.setMuted(true));
    }

    public void unmuteInConversation(Conversation conversation) {
        conversationAssistants.stream()
                .filter(ca -> ca.getConversation().equals(conversation))
                .findFirst()
                .ifPresent(ca -> ca.setMuted(false));
    }

    public boolean isMutedInConversation(Conversation conversation) {
        return conversationAssistants.stream()
                .filter(ca -> ca.getConversation().equals(conversation))
                .findFirst()
                .map(ConversationAssistant::isMuted)
                .orElse(false);
    }
}

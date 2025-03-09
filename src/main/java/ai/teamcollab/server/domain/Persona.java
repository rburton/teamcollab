package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "personas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Persona {

    @Id
    @Column(name = "persona_id")
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
    @NotBlank(message = "Please describe the role to guide the behavior of the Persona")
    @Size(min = 1, message = "The expertise prompt is too short")
    private String expertisePrompt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Lazy
    @ManyToMany(mappedBy = "personas")
    private Set<Conversation> conversations = new HashSet<>();

    public Persona(String name, String expertise) {
        this.name = name;
        this.expertise = expertise;
    }

    public void addToConversation(Conversation conversation) {
        conversation.addPersona(this);
    }

    public void removeFromConversation(Conversation conversation) {
        this.conversations.remove(conversation);
    }
}

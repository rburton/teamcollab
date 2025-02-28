package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Persona {

    @Id
    @Column(name = "persona_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Column(name = "expertise_areas")
    @NotBlank(message = "At least one area of expertise is required")
    @Size(min = 1, message = "At least one area of expertise is required")
    private String expertises;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Lazy
    @ManyToMany(mappedBy = "personas")
    private Set<Conversation> conversations = new HashSet<>();

    public Persona(String name, String expertises) {
        this.name = name;
        this.expertises = expertises;
    }

    public void addToConversation(Conversation conversation) {
        conversation.addPersona(this);
    }

    public void removeFromConversation(Conversation conversation) {
        this.conversations.remove(conversation);
    }
}

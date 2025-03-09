package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"messages", "personas"})
public class Conversation {

    @Id
    @Column(name = "conversation_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "purpose")
    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 1000, message = "Purpose must be between 10 and 1000 characters")
    private String purpose;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "conversation", cascade = ALL, orphanRemoval = true)
    private Set<Message> messages = new HashSet<>();

    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "conversation_persona",
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    @Fetch(FetchMode.JOIN)
    private Set<Persona> personas = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Conversation(String purpose, User createdBy) {
        this.purpose = purpose;
        this.user = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public void addPersona(Persona persona) {
        personas.add(persona);
        persona.getConversations().add(this);
    }
}

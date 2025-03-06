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

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"conversations"})
public class Project {

    @Id
    @Column(name = "project_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Column(name = "overview")
    @NotBlank(message = "Overview is required")
    @Size(min = 3, max = 255, message = "Overview must be between 3 and 255 characters")
    private String overview;

    @OneToMany(mappedBy = "project", cascade = ALL, orphanRemoval = true)
    private Set<Conversation> conversations = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}

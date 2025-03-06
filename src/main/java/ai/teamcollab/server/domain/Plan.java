package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plans")
@ToString(exclude = {"planDetails", "subscriptions"})
@EqualsAndHashCode(exclude = {"planDetails", "subscriptions"})
public class Plan {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @NotBlank(message = "Plan name is required")
    @Size(min = 3, max = 255, message = "Plan name must be between 3 and 255 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "plan", cascade = ALL, orphanRemoval = true)
    private List<PlanDetail> planDetails = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "plan")
    private List<Subscription> subscriptions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

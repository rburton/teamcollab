package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "subscriptions")
@ToString(exclude = {"company", "plan", "payments"})
@EqualsAndHashCode(exclude = {"company", "plan", "payments"})
public class Subscription {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @NotNull(message = "Plan is required")
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Pattern(regexp = "^sub_[a-zA-Z0-9]+$", message = "Invalid Stripe subscription ID format")
    @Column(name = "stripe_subscription_id", nullable = true, unique = true)
    private String stripeSubscriptionId;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be today or in the future")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder.Default
    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

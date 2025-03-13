package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Objects.isNull;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "companies")
@EqualsAndHashCode(of = "id")
public class Company implements Serializable {

    @Id
    @Column(name = "company_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name")
    private String name;

    @Column(name = "stripe_customer_id", unique = true)
    private String stripeCustomerId;

    @OneToMany(mappedBy = "company", cascade = ALL, orphanRemoval = true)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "llm_model_id")
    private LlmModel llmModel;

    @Positive(message = "Monthly price must be positive")
    @DecimalMin(value = "0.01", message = "Monthly price must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Monthly price must have at most 10 digits and 2 decimal places")
    @Column(name = "monthly_spending_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlySpendingLimit;

    public void addUser(User user) {
        this.users.add(user);
    }

    public boolean owns(Assistant assistant) {
        return isNull(assistant.getCompany()) || assistant.getCompany().equals(this);
    }

    public boolean doesntOwns(Assistant assistant) {
        return !owns(assistant);
    }

    public void addSubscription(PlanDetail planDetail) {
        final var subscription = Subscription.builder()
                .plan(planDetail.getPlan())
                .startDate(LocalDate.now())
                .build();
        this.subscriptions.add(subscription);
        subscription.setCompany(this);
        this.setMonthlySpendingLimit(planDetail.getMonthlySpendingLimit());
    }

}

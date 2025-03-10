package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Objects.isNull;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "llm_model")
    private String llmModel;

    @Column(name = "monthly_spending_limit")
    private Double monthlySpendingLimit;

    public void addUser(User user) {
        this.users.add(user);
    }

    public boolean owns(Assistant assistant) {
        return isNull(assistant.getCompany()) || assistant.getCompany().equals(this);
    }

    public boolean doesntOwns(Assistant assistant) {
        return !owns(assistant);
    }
}

package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plan_details")
@ToString(exclude = "plan")
@EqualsAndHashCode(exclude = "plan")
public class PlanDetail {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "plan_detail_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @NotNull(message = "Effective date is required")
    @FutureOrPresent(message = "Effective date must be today or in the future")
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @NotNull(message = "Monthly price is required")
    @Positive(message = "Monthly price must be positive")
    @DecimalMin(value = "0.01", message = "Monthly price must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Monthly price must have at most 10 digits and 2 decimal places")
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

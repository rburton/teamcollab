package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static jakarta.persistence.GenerationType.IDENTITY;
import static java.math.RoundingMode.HALF_UP;

@Getter
@Setter
@Entity
@Builder
@Table(name = "metrics")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Metrics {

    @Id
    @Column(name = "metric_id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "duration")
    private long duration;

    @Column(name = "input_tokens")
    private int inputTokens;

    @Column(name = "output_tokens")
    private int outputTokens;

    @Column(name = "provider")
    private String provider;

    @Column(name = "model")
    private String model;

    @Column(name = "additional_info")
    private String additionalInfo;

    @OneToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    public BigDecimal getCost() {
        final var cost = GptModel.fromId(model).calculate(inputTokens, outputTokens);
        return cost.setScale(5, HALF_UP);
    }
}

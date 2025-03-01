package ai.teamcollab.server.service.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Metrics {
    private long duration;
    private int inputTokens;
    private int outputTokens;
    private String provider;
    private String model;
    private String additionalInfo;
}
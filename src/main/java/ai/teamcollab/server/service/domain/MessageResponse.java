package ai.teamcollab.server.service.domain;

import ai.teamcollab.server.domain.Metrics;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String content;
    private Metrics metrics;
}
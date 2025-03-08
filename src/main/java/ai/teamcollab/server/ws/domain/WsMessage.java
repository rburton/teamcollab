package ai.teamcollab.server.ws.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WsMessage {
    private String username;
    private String personaName;
    private String content;
    private LocalDateTime createdAt;
}

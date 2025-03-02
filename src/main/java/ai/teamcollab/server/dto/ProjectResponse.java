package ai.teamcollab.server.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String overview;
    private LocalDateTime createdAt;
    private ConversationResponse conversation;

    @Getter
    @Setter
    @Builder
    public static class ConversationResponse {
        private Long id;
        private String purpose;
        private LocalDateTime createdAt;
    }
}
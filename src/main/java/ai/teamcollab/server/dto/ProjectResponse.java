package ai.teamcollab.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String overview;
    private LocalDateTime createdAt;
    private ConversationResponse conversation;
    private List<ConversationResponse> conversations;

    @Getter
    @Setter
    @Builder
    public static class ConversationResponse {
        private Long id;
        private String purpose;
        private String createdBy;
        private LocalDateTime createdAt;
    }
}
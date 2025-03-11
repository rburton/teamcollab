package ai.teamcollab.server.service.domain;

import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@Builder(toBuilder = true)
public class MessageRow {
    private Long id;
    private String username;
    private String assistantName;
    private String content;
    private Long conversationId;
    private boolean bookmarked;
    private LocalDateTime createdAt;

    public static MessageRow from(Message message) {
        final var username = Optional.ofNullable(message.getUser())
                .map(User::getUsername)
                .orElse(null);
        final var assistantName = Optional.ofNullable(message.getAssistant())
                .map(Assistant::getName)
                .orElse(null);

        return MessageRow.builder()
                .id(message.getId())
                .content(message.getContent())
                .username(username)
                .assistantName(assistantName)
                .bookmarked(false)
                .conversationId(message.getConversation().getId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

package ai.teamcollab.server.service.domain;

import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class MessageRow {
    private String username;
    private String assistantName;
    private String content;
    private LocalDateTime createdAt;

    public static MessageRow from(Message message) {
        final var username = Optional.ofNullable(message.getUser())
                .map(User::getUsername)
                .orElse(null);
        final var assistantName = Optional.ofNullable(message.getAssistant())
                .map(Assistant::getName)
                .orElse(null);

        return MessageRow.builder()
                .content(message.getContent())
                .username(username)
                .assistantName(assistantName)
                .createdAt(message.getCreatedAt())
                .build();
    }
}

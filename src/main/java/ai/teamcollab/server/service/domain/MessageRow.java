package ai.teamcollab.server.service.domain;

import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class MessageRow {
    private String username;
    private String personaName;
    private String content;
    private LocalDateTime createdAt;

    public static MessageRow from(Message message) {
        final var username = Optional.ofNullable(message.getUser())
                .map(User::getUsername)
                .orElse(null);
        final var personaName = Optional.ofNullable(message.getPersona())
                .map(Persona::getName)
                .orElse(null);

        return MessageRow.builder()
                .content(message.getContent())
                .username(username)
                .personaName(personaName)
                .createdAt(message.getCreatedAt())
                .build();
    }
}

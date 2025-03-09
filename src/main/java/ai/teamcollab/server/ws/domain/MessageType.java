package ai.teamcollab.server.ws.domain;

import lombok.Getter;

@Getter
public enum MessageType {
    TURBO("A payload for Turbo streams"),
    CLOSED("When the conversation is closed by the server"),
    MESSAGE("When one or more messages are sent"),
    MESSAGE_PROCESSING("When a message is being processed by AI"),
    MESSAGE_WAITING("When the message is waiting to be processed"),
    THINKING("Update the assistants indicator to thinking"),
    NOTE("When a note was recorded by the facillitor"),
    ACTION_ITEM("When an action item is recorded by the facalitor"),
    ;

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

}

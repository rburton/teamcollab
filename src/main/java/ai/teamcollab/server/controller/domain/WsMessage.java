package ai.teamcollab.server.controller.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {
    @JsonProperty("conversation_id")
    private long conversationId;
    @JsonProperty("content")
    private String content;
    @JsonProperty("sender")
    private String sender;
    @JsonProperty("type")
    private MessageType type;
}

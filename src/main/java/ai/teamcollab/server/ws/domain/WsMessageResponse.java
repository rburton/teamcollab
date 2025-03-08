package ai.teamcollab.server.ws.domain;

import lombok.Builder;
import lombok.Getter;

import static ai.teamcollab.server.ws.domain.MessageType.TURBO;

@Getter
@Builder
public class WsMessageResponse {
    private MessageType messageType;
    private Object payload;

    public static WsMessageResponse turbo(Object payload) {
        return WsMessageResponse.builder()
                .messageType(TURBO)
                .payload(payload)
                .build();
    }

}

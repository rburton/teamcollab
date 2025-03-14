package ai.teamcollab.server.ws;

import ai.teamcollab.server.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    //    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        final var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        final var principal = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        final var user = (User) principal.getPrincipal();
        log.debug("Connected - Session ID: " + user.getUsername());
    }

    //    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        final var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        final var principal = (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        final var user = (User) principal.getPrincipal();
        log.debug("Disconnected - Session ID: " + user.getUsername());
    }

}

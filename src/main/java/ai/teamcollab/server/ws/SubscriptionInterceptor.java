package ai.teamcollab.server.ws;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;
import static org.springframework.messaging.simp.stomp.StompCommand.SUBSCRIBE;

@Component
public class SubscriptionInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final var accessor = StompHeaderAccessor.wrap(message);
        if (SUBSCRIBE.equals(accessor.getCommand())) {
            final var user = (Authentication) accessor.getUser();
            final var destination = accessor.getDestination();
            if (nonNull(destination) && destination.startsWith("/user/")) {
                final var expectedUser = destination.split("/")[2]; // Extract username from /user/{username}/...
                if (!user.getName().equalsIgnoreCase(expectedUser)) {
                    throw new IllegalStateException("Cannot subscribe to another user's channel");
                }
            }
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

    }

}

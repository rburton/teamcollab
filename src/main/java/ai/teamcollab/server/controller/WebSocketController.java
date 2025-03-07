package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.WsMessage;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.domain.MessageRow;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <div class="flex items-start" th:classappend="${message.username} ? ' justify-end'">
 * <p>
 * <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white mr-2"
 * th:text="${#strings.substring(message.username, 0, 1)}">
 * </div>
 * <div class="bg-blue-100 p-3 rounded-lg max-w-[70%]">
 * <p class="text-sm font-semibold text-blue-800"
 * th:text="${message.username + (message.personaName != null ? ' - ' + message.personnName : '')}">
 * </p>
 * <p class="text-gray-800" th:text="${message.content}"></p>
 * <p class="text-xs text-gray-500 mt-1"
 * th:text="${#temporals.format(message.createdAt, 'MMM dd, yyyy, hh:mm a')}">
 * </p>
 * </div>
 * </div>
 */
@Controller
public class WebSocketController {

    public static final String DIRECT_MESSAGE_TOPIC = "/queue/messages";
    public static final String ERROR_EVENT_TOPIC = "/queue/errors";

    private final ConversationService conversationService;

    public WebSocketController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @MessageMapping("/chat.send")
    @SendToUser(DIRECT_MESSAGE_TOPIC)
    public List<MessageRow> sendMessage(@Payload WsMessage message,
                                        UsernamePasswordAuthenticationToken principal,
                                        SimpMessageHeaderAccessor headerAccessor) {
        final var user = (User) principal.getPrincipal();
        final var newMessage = Message.builder()
                .content(message.getContent())
                .build();
        final var savedMessage = conversationService.addToConversation(message.getConversationId(), newMessage, user);
        conversationService.sendMessage(savedMessage.getId(), user.getUsername());
        return List.of(MessageRow.builder()
                .content(savedMessage.getContent())
                .username(principal.getName())
                .createdAt(savedMessage.getCreatedAt())
                .build());
    }

    @MessageMapping("/chat.join")
    @SendToUser(DIRECT_MESSAGE_TOPIC)
    public List<MessageRow> joinChat(@Payload WsMessage message, SimpMessageHeaderAccessor headerAccessor) {
        return conversationService.findMessagesByConversation(message.getConversationId())
                .stream()
                .map(MessageRow::from)
                .toList();
    }

    @MessageExceptionHandler
    @SendToUser(ERROR_EVENT_TOPIC)
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

}

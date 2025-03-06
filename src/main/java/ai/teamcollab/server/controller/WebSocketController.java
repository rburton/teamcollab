package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.WsMessage;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.domain.MessageRow;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * <div th:each="message : ${messages}" class="flex items-start" th:classappend="${message.username} ? ' justify-end'">
 * <p>
 * <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white mr-2"
 * th:text="${#strings.substring(message.username, 0, 1)}">
 * </div>
 *     <div class="bg-blue-100 p-3 rounded-lg max-w-[70%]">
 *         <p class="text-sm font-semibold text-blue-800"
 *            th:text="${message.username + (message.personaName != null ? ' - ' + message.personnName : '')}">
 *         </p>
 *         <p class="text-gray-800" th:text="${message.content}"></p>
 *         <p class="text-xs text-gray-500 mt-1"
 *            th:text="${#temporals.format(message.createdAt, 'MMM dd, yyyy, hh:mm a')}">
 *         </p>
 *     </div>
 * </div>
 */
@Controller
public class WebSocketController {

    private final ConversationService conversationService;

    public WebSocketController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @MessageMapping("/chat.list")
    @SendTo("/topic/public")
    public WsMessage getMessages(@Payload WsMessage message) {
        return message;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public List<MessageRow> sendMessage(@Payload WsMessage message) {
        return List.of(MessageRow.builder()
                .content(message.getContent())
                .build());
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/messages")
    public List<MessageRow> joinChat(@Payload WsMessage message) {
        return conversationService.findMessagesByConversation(message.getConversationId())
                .stream()
                .map(MessageRow::from)
                .toList();
    }

}

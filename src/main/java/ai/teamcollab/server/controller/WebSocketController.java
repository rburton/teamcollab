package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.WsMessage;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.domain.MessageRow;
import ai.teamcollab.server.templates.ThymeleafTemplateRender;
import ai.teamcollab.server.ws.domain.WsMessageResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

import static ai.teamcollab.server.templates.TemplatePath.CONVERSATION_MESSAGE_TEMPLATE;
import static ai.teamcollab.server.templates.TemplatePath.PERSONA_STATUSES_TEMPLATE;
import static ai.teamcollab.server.templates.TemplateVariableName.MESSAGE;
import static ai.teamcollab.server.templates.TemplateVariableName.PERSONAS;
import static ai.teamcollab.server.templates.TemplateVariableName.STATUS;


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

    private final ThymeleafTemplateRender thymeleafTemplateRender;
    private final ConversationService conversationService;

    public WebSocketController(ThymeleafTemplateRender thymeleafTemplateRender, ConversationService conversationService) {
        this.thymeleafTemplateRender = thymeleafTemplateRender;
        this.conversationService = conversationService;
    }

    @MessageMapping("/chat.send")
    @SendToUser(DIRECT_MESSAGE_TOPIC)
    public WsMessageResponse sendMessage(@Payload WsMessage message,
                                         UsernamePasswordAuthenticationToken principal,
                                         SimpMessageHeaderAccessor headerAccessor) {
        final var user = (User) principal.getPrincipal();
        final var newMessage = Message.builder()
                .content(message.getContent())
                .build();
        final var conversation = conversationService.findConversationById(message.getConversationId());

        final var savedMessage = conversationService.addToConversation(conversation.getId(), newMessage, user);
        conversationService.sendMessage(savedMessage.getId(), user.getUsername());
        final var row = MessageRow.builder()
                .content(savedMessage.getContent())
                .username(principal.getName())
                .createdAt(savedMessage.getCreatedAt())
                .build();

        final var html = thymeleafTemplateRender.renderToHtml(CONVERSATION_MESSAGE_TEMPLATE, Map.of(MESSAGE, row));
        final var personas = conversation.getPersonas();
        final var htmlStatus = thymeleafTemplateRender.renderToHtml(PERSONA_STATUSES_TEMPLATE, Map.of(PERSONAS, personas, STATUS, "Thinking"));
        return WsMessageResponse.turbo(List.of(html, htmlStatus));
    }

    @MessageMapping("/chat.join")
    @SendToUser(DIRECT_MESSAGE_TOPIC)
    public WsMessageResponse joinChat(@Payload WsMessage message, SimpMessageHeaderAccessor headerAccessor) {
        final var messages = conversationService.findMessagesByConversation(message.getConversationId())
                .stream()
                .map(MessageRow::from)
                .toList();
        final var elements = messages.stream()
                .map(row -> thymeleafTemplateRender.renderToHtml(CONVERSATION_MESSAGE_TEMPLATE, Map.of(MESSAGE, row)))
                .toList();

        return WsMessageResponse.turbo(elements);
    }

    @MessageExceptionHandler
    @SendToUser(ERROR_EVENT_TOPIC)
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

}

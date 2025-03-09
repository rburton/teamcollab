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
import static ai.teamcollab.server.templates.TemplatePath.ASSISTANT_STATUSES_TEMPLATE;
import static ai.teamcollab.server.templates.TemplateVariableName.MESSAGE;
import static ai.teamcollab.server.templates.TemplateVariableName.ASSISTANTS;
import static ai.teamcollab.server.templates.TemplateVariableName.STATUS;

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
        final var assistants = conversation.getAssistants();
        final var htmlStatus = thymeleafTemplateRender.renderToHtml(ASSISTANT_STATUSES_TEMPLATE, Map.of(ASSISTANTS, assistants, STATUS, "Thinking"));
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

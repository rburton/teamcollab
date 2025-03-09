package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.AssistantService;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.domain.MessageRow;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static java.util.Objects.isNull;

@Slf4j
@Controller
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final AssistantService assistantService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ConversationController(@NonNull ConversationService conversationService, @NonNull AssistantService assistantService, SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.assistantService = assistantService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public String index(@NonNull @AuthenticationPrincipal User user, Model model) {
        model.addAttribute("conversations", conversationService.getUserConversations(user.getId()));
        if (!model.containsAttribute("conversation")) {
            model.addAttribute("conversation", new Conversation());
        }
        return "conversations/index";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("conversation") Conversation conversation,
                         BindingResult bindingResult,
                         @NonNull @AuthenticationPrincipal User user,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.conversation", bindingResult);
            redirectAttributes.addFlashAttribute("conversation", conversation);
            return "redirect:/conversations";
        }

        try {
            conversationService.createConversation(conversation, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Conversation created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("conversation", conversation);
        }

        return "redirect:/conversations";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, @NonNull @AuthenticationPrincipal User user, @NonNull Model model) {
        final var conversation = conversationService.findConversationByIdWithAssistant(id);
        if (!user.equals(conversation.getUser())) {
            throw new IllegalArgumentException("Access denied");
        }

        model.addAttribute("conversation", conversation);
        model.addAttribute("assistants", conversation.getAssistants());
        return "conversations/show";
    }

    @PostMapping("/{conversationId}/messages")
    public String postMessage(@PathVariable Long conversationId,
                              @Valid @ModelAttribute("newMessage") Message message,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal User user,
                              RedirectAttributes redirectAttributes,
                              Model mode,
                              @RequestHeader("Accept") String accept) {

        if (isNull(user)) {
            throw new IllegalArgumentException("User not authenticated");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newMessage", bindingResult);
            redirectAttributes.addFlashAttribute("newMessage", message);
            return "redirect:/conversations/" + conversationId;
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            bindingResult.rejectValue("content", "NotEmpty", "Message content cannot be empty");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newMessage", bindingResult);
            redirectAttributes.addFlashAttribute("newMessage", message);
            return "redirect:/conversations/" + conversationId;
        }

        try {
            final var savedMessage = conversationService.addToConversation(conversationId, message, user);
            this.messagingTemplate.convertAndSend("/topic/public", "{}");

            //            conversationService.sendMessage(savedMessage);
            redirectAttributes.addFlashAttribute("successMessage", "Message posted successfully!");
            mode.addAttribute("message", MessageRow.from(message));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("newMessage", MessageRow.from(message));
        }

        if (accept.contains("turbo")) {
            return "/conversations/message.xhtml";
        }
        return "redirect:/conversations/" + conversationId;

    }

    @PostMapping("/{conversationId}/assistant/{assistantId}")
    public String addAssistantToConversation(
            @PathVariable Long conversationId,
            @PathVariable Long assistantId,
            @AuthenticationPrincipal User user,
            Model model) {

        log.debug("Adding assistant {} to conversation {}", assistantId, conversationId);

        try {
            var assistant = assistantService.findById(assistantId)
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found"));

            assistantService.addToConversation(assistant.getId(), conversationId);

            var updatedAssistant = assistantService.findById(assistantId)
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found after update"));
            model.addAttribute("assistant", updatedAssistant);
            return "conversations/assistant";
        } catch (IllegalArgumentException e) {
            log.error("Bad request while adding assistant to conversation", e);
            return "conversations/assistant";
        } catch (Exception e) {
            log.error("Error adding assistant to conversation", e);
            return "conversations/assistant";
        }
    }

}

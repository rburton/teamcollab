package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("conversations", conversationService.getUserConversations(user.getId()));
        if (!model.containsAttribute("conversation")) {
            model.addAttribute("conversation", new Conversation());
        }
        return "conversations/index";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("conversation") Conversation conversation,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal User user,
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
    public String show(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            throw new IllegalArgumentException("User not authenticated");
        }

        Conversation conversation = conversationService.getConversationById(id);
        if (!user.equals(conversation.getUser())) {
            throw new IllegalArgumentException("Access denied");
        }

        model.addAttribute("conversation", conversation);
        return "conversations/show";
    }

}

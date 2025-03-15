package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.AssistantView;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.Message;
import ai.teamcollab.server.service.AssistantService;
import ai.teamcollab.server.service.BookmarkService;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.domain.MessageRow;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private final BookmarkService bookmarkService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ConversationController(@NonNull ConversationService conversationService,
                                  @NonNull AssistantService assistantService,
                                  @NonNull BookmarkService bookmarkService,
                                  @NonNull SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.assistantService = assistantService;
        this.bookmarkService = bookmarkService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping
    public String index(@NonNull @AuthenticationPrincipal LoginUserDetails user, Model model) {
        model.addAttribute("conversations", conversationService.getUserConversations(user.getId()));
        if (!model.containsAttribute("conversation")) {
            model.addAttribute("conversation", new Conversation());
        }
        return "conversations/index";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("conversation") Conversation conversation,
                         BindingResult bindingResult,
                         @NonNull @AuthenticationPrincipal LoginUserDetails user,
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
    public String show(@PathVariable Long id, @NonNull @AuthenticationPrincipal LoginUserDetails user, @NonNull Model model) {
        final var conversation = conversationService.findConversationByIdWithAssistant(id);

        // Check if the user can access the conversation
        // User can access if they own the conversation or if it's not private
        if (!conversationService.canUserAccessConversation(id, user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        model.addAttribute("conversation", conversation);
        model.addAttribute("conversationId", conversation.getId());
        model.addAttribute("assistants", AssistantView.from(conversation.getConversationAssistants()));
        // Add a flag to indicate if the user is the owner of the conversation
        model.addAttribute("isOwner", user.getId().equals(conversation.getUser().getId()));

        return "conversations/show";
    }

    @PostMapping("/{conversationId}/messages/{messageId}/bookmark")
    public ResponseEntity<?> bookmarkMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @NonNull @AuthenticationPrincipal LoginUserDetails user) {

        try {
            bookmarkService.bookmarkMessage(user.getId(), messageId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{conversationId}/messages/{messageId}/unbookmark")
    public ResponseEntity<?> unbookmarkMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @NonNull @AuthenticationPrincipal LoginUserDetails user) {

        try {
            bookmarkService.unbookmarkMessage(user.getId(), messageId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{conversationId}/messages")
    public String postMessage(@PathVariable Long conversationId,
                              @Valid @ModelAttribute("newMessage") Message message,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal LoginUserDetails user,
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
            final var savedMessage = conversationService.addToConversation(conversationId, message, user.getId());
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

    /**
     * Resets a conversation by soft deleting all messages and generating a point-in-time summary.
     *
     * @param conversationId the ID of the conversation to reset
     * @param user the authenticated user
     * @param redirectAttributes for flash messages
     * @param accept the Accept header to determine the response format
     * @return the appropriate view
     */
    @PostMapping("/{conversationId}/reset")
    public String resetConversation(
            @PathVariable Long conversationId,
            @NonNull @AuthenticationPrincipal LoginUserDetails user,
            RedirectAttributes redirectAttributes,
            @RequestHeader("Accept") String accept) {

        log.debug("Resetting conversation {} for user {}", conversationId, user.getId());

        try {
            // Reset the conversation and get the summary
            final var summaryFuture = conversationService.resetConversation(conversationId, user.getId());

            // Wait for the summary to be generated
            final var summary = summaryFuture.join();

            redirectAttributes.addFlashAttribute("successMessage", "Conversation reset successfully!");
            redirectAttributes.addFlashAttribute("summary", summary);

            if (accept.contains("turbo")) {
                return "redirect:/conversations/" + conversationId;
            }

            return "redirect:/conversations/" + conversationId;
        } catch (IllegalArgumentException e) {
            log.error("Bad request while resetting conversation", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/conversations/" + conversationId;
        } catch (Exception e) {
            log.error("Error resetting conversation", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while resetting the conversation");
            return "redirect:/conversations/" + conversationId;
        }
    }

    /**
     * Toggles the privacy status of a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param user the authenticated user
     * @param redirectAttributes for flash messages
     * @param accept the Accept header to determine the response format
     * @return the appropriate view
     */
    @PostMapping("/{conversationId}/toggle-privacy")
    public String togglePrivacy(
            @PathVariable Long conversationId,
            @NonNull @AuthenticationPrincipal LoginUserDetails user,
            RedirectAttributes redirectAttributes,
            @RequestHeader("Accept") String accept) {

        log.debug("Toggling privacy for conversation {} by user {}", conversationId, user.getId());

        try {
            // Get the current conversation
            final var conversation = conversationService.findConversationById(conversationId);

            // Toggle the privacy status
            final var isCurrentlyPrivate = conversation.isPrivate();
            final var updatedConversation = conversationService.setConversationPrivacy(
                    conversationId, 
                    user.getId(), 
                    !isCurrentlyPrivate
            );

            final var statusMessage = updatedConversation.isPrivate() 
                    ? "Conversation marked as private. Only you can view it." 
                    : "Conversation marked as public. All users can view it.";

            redirectAttributes.addFlashAttribute("successMessage", statusMessage);

            if (accept.contains("turbo")) {
                return "redirect:/conversations/" + conversationId;
            }

            return "redirect:/conversations/" + conversationId;
        } catch (IllegalArgumentException e) {
            log.error("Bad request while toggling conversation privacy", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/conversations/" + conversationId;
        } catch (Exception e) {
            log.error("Error toggling conversation privacy", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while changing the conversation privacy");
            return "redirect:/conversations/" + conversationId;
        }
    }
}

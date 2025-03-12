package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.AssistantView;
import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.service.AssistantService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/assistants")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUserDetails loginUserDetails, Model model) {
        model.addAttribute("assistants", assistantService.findByCompany(loginUserDetails.getCompanyId()));
        if (!model.containsAttribute("assistant")) {
            model.addAttribute("assistant", new Assistant());
        }
        return "assistants/index";
    }

    @ResponseBody
    @GetMapping("/all")
    public List<Assistant> getAllAssistants(@AuthenticationPrincipal LoginUserDetails loginUserDetails) {
        return assistantService.findByCompany(loginUserDetails.getCompanyId());
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("assistant") Assistant assistant, BindingResult bindingResult, @AuthenticationPrincipal LoginUserDetails loginUserDetails, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.assistant", bindingResult);
            redirectAttributes.addFlashAttribute("assistant", assistant);
            return "redirect:/assistants";
        }

        try {
            final var company = loginUserDetails.getUser().getCompany();
            assistantService.createAssistant(assistant.getName(), assistant.getExpertise(), assistant.getExpertisePrompt(), company);
            redirectAttributes.addFlashAttribute("successMessage", "Assistant created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("assistant", assistant);
        }

        return "redirect:/assistants";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, @AuthenticationPrincipal LoginUserDetails loginUserDetails, Model model) {
        final var assistant = assistantService.findById(id).orElseThrow(() -> new IllegalArgumentException("Assistant not found"));
        final var company = loginUserDetails.getUser().getCompany();
        if (company.doesntOwns(assistant)) {
            throw new IllegalArgumentException("Access denied");
        }
        model.addAttribute("assistant", assistant);
        return "assistants/show";
    }

    @PostMapping("/{id}/expertise")
    public String addExpertise(@PathVariable Long id, @RequestParam String expertise, @AuthenticationPrincipal LoginUserDetails loginUserDetails, RedirectAttributes redirectAttributes) {
        try {
            final var assistant = assistantService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found"));

            final var company = loginUserDetails.getUser().getCompany();
            if (company.doesntOwns(assistant)) {
                throw new IllegalArgumentException("Access denied");
            }

            assistant.setExpertise(expertise);
            assistantService.updateAssistant(assistant.getId(), assistant.getName(), assistant.getExpertise());
            redirectAttributes.addFlashAttribute("successMessage", "Expertise added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/assistants/" + id;
    }


    @PostMapping(value = "/{id}/conversations/{conversationId}")
    public String addToConversation(@PathVariable Long id, @PathVariable Long conversationId, @AuthenticationPrincipal LoginUserDetails loginUserDetails, Model model) {
        try {
            final var assistant = assistantService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found"));
            model.addAttribute("assistantId", id);

            final var company = loginUserDetails.getUser().getCompany();
            if (company.doesntOwns(assistant)) {
                throw new IllegalArgumentException("Access denied");
            }

            assistantService.addToConversation(id, conversationId);
            model.addAttribute("conversationId", conversationId);
            model.addAttribute("assistant", AssistantView.from(assistant));
        } catch (IllegalArgumentException e) {
            log.error("Failed to add to conversation", e);
        }

        return "/assistants/added.xhtml";
    }

    @DeleteMapping("/{id}/conversations/{conversationId}")
    public String removeFromConversation(@PathVariable Long id, @PathVariable Long conversationId, @AuthenticationPrincipal LoginUserDetails loginUserDetails, RedirectAttributes redirectAttributes) {
        try {
            Assistant assistant = assistantService.findById(id).orElseThrow(() -> new IllegalArgumentException("Assistant not found"));

            final var company = loginUserDetails.getUser().getCompany();
            if (company.doesntOwns(assistant)) {
                throw new IllegalArgumentException("Access denied");
            }

            assistantService.removeFromConversation(id, conversationId);
            redirectAttributes.addFlashAttribute("successMessage", "Removed from conversation successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/assistants/" + id;
    }

    @GetMapping("/conversations/{conversationId}")
    public String getAssistantNotInConversation(@PathVariable Long conversationId, @AuthenticationPrincipal LoginUserDetails loginUserDetails, Model model) {
        try {
            log.debug("Getting assistants not in conversation {} for company {}", conversationId, loginUserDetails.getCompanyId());
            final var assistants = assistantService.findAssistantsNotInConversation(loginUserDetails.getCompanyId(), conversationId);
            model.addAttribute("assistants", assistants.stream().map(AssistantView::from).toList());
            model.addAttribute("conversationId", conversationId);
            return "assistants/assistants";
        } catch (IllegalArgumentException e) {
            log.error("Error getting assistants not in conversation: {}", e.getMessage());
            return "assistants/assistants";
        } catch (Exception e) {
            log.error("Unexpected error getting assistants not in conversation: {}", e.getMessage());
            return "assistants/assistants";
        }
    }
}

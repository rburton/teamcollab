package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.AssistantTone;
import ai.teamcollab.server.service.AssistantToneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/system/assistant-tones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AssistantToneController {

    private final AssistantToneService assistantToneService;

    @GetMapping
    public String listTones(Model model) {
        log.debug("Showing assistant tones list page");
        List<AssistantTone> tones = assistantToneService.findAll();
        model.addAttribute("tones", tones);
        return "system/assistant-tones/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        log.debug("Showing create assistant tone form");
        model.addAttribute("tone", new AssistantTone());
        return "system/assistant-tones/create";
    }

    @PostMapping("/create")
    public String createTone(@Valid @ModelAttribute("tone") AssistantTone tone, BindingResult result, Model model) {
        log.debug("Creating new assistant tone: {}", tone.getName());

        if (result.hasErrors()) {
            return "system/assistant-tones/create";
        }

        try {
            assistantToneService.createTone(tone.getName(), tone.getDisplayName(), tone.getPrompt());
            model.addAttribute("success", "Assistant tone created successfully");
        } catch (Exception e) {
            log.error("Error creating assistant tone", e);
            model.addAttribute("error", "Failed to create assistant tone: " + e.getMessage());
            return "system/assistant-tones/create";
        }

        return "redirect:/system/assistant-tones";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit assistant tone form for ID: {}", id);
        
        AssistantTone tone = assistantToneService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid assistant tone ID: " + id));
        
        model.addAttribute("tone", tone);
        return "system/assistant-tones/edit";
    }

    @PostMapping("/{id}/update")
    public String updateTone(@PathVariable Long id, @Valid @ModelAttribute("tone") AssistantTone tone, 
                             BindingResult result, Model model) {
        log.debug("Updating assistant tone with ID: {}", id);

        if (result.hasErrors()) {
            return "system/assistant-tones/edit";
        }

        try {
            assistantToneService.updateTone(id, tone.getDisplayName(), tone.getPrompt());
            model.addAttribute("success", "Assistant tone updated successfully");
        } catch (Exception e) {
            log.error("Error updating assistant tone", e);
            model.addAttribute("error", "Failed to update assistant tone: " + e.getMessage());
            return "system/assistant-tones/edit";
        }

        return "redirect:/system/assistant-tones";
    }

    @PostMapping("/{id}/delete")
    public String deleteTone(@PathVariable Long id, Model model) {
        log.debug("Deleting assistant tone with ID: {}", id);

        try {
            assistantToneService.deleteTone(id);
            model.addAttribute("success", "Assistant tone deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting assistant tone", e);
            model.addAttribute("error", "Failed to delete assistant tone: " + e.getMessage());
        }

        return "redirect:/system/assistant-tones";
    }
}
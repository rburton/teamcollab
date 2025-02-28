package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.PersonaService;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    private final PersonaService personaService;
    private final CompanyService companyService;

    public PersonaController(PersonaService personaService, CompanyService companyService) {
        this.personaService = personaService;
        this.companyService = companyService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        final var company = user.getCompany();
        model.addAttribute("personas", personaService.findByCompany(company.getId()));
        if (!model.containsAttribute("persona")) {
            model.addAttribute("persona", new Persona());
        }
        return "personas/index";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("persona") Persona persona, BindingResult bindingResult, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.persona", bindingResult);
            redirectAttributes.addFlashAttribute("persona", persona);
            return "redirect:/personas";
        }

        try {
            final var company = user.getCompany();
            personaService.createPersona(persona.getName(), persona.getExpertises(), company);
            redirectAttributes.addFlashAttribute("successMessage", "Persona created successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("persona", persona);
        }

        return "redirect:/personas";
    }

    @GetMapping("/{id}")
    public String show(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
        Persona persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));
        final var company = user.getCompany();
        if (company.doesntOwns(persona)) {
            throw new IllegalArgumentException("Access denied");
        }
        model.addAttribute("persona", persona);
        return "personas/show";
    }

    @PostMapping("/{id}/expertise")
    public String addExpertise(@PathVariable Long id, @RequestParam String expertise, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));

            final var company = user.getCompany();
            if (company.doesntOwns(persona)) {
                throw new IllegalArgumentException("Access denied");
            }

            persona.setExpertises(expertise);
            personaService.updatePersona(persona.getId(), persona.getName(), persona.getExpertises());
            redirectAttributes.addFlashAttribute("successMessage", "Expertise added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/personas/" + id;
    }

    @PostMapping("/{id}/conversations/{conversationId}")
    public String addToConversation(@PathVariable Long id, @PathVariable Long conversationId, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));

            final var company = user.getCompany();
            if (company.doesntOwns(persona)) {
                throw new IllegalArgumentException("Access denied");
            }

            personaService.addToConversation(id, conversationId);
            redirectAttributes.addFlashAttribute("successMessage", "Added to conversation successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/personas/" + id;
    }

    @DeleteMapping("/{id}/conversations/{conversationId}")
    public String removeFromConversation(@PathVariable Long id, @PathVariable Long conversationId, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        try {
            Persona persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));

            final var company = user.getCompany();
            if (company.doesntOwns(persona)) {
                throw new IllegalArgumentException("Access denied");
            }

            personaService.removeFromConversation(id, conversationId);
            redirectAttributes.addFlashAttribute("successMessage", "Removed from conversation successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/personas/" + id;
    }
}
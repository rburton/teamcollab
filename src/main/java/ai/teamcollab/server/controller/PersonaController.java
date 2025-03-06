package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.PersonaService;
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
@RequestMapping("/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
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

    @ResponseBody
    @GetMapping("/all")
    public List<Persona> getAllPersonas(@AuthenticationPrincipal User user) {
        final var company = user.getCompany();
        return personaService.findByCompany(company.getId());
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
        final var persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));
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
            final var persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));

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


    @PostMapping(value = "/{id}/conversations/{conversationId}")
    public String addToConversation(@PathVariable Long id, @PathVariable Long conversationId, @AuthenticationPrincipal User user, Model model) {
        try {
            final var persona = personaService.findById(id).orElseThrow(() -> new IllegalArgumentException("Persona not found"));
            model.addAttribute("personaId", id);

            final var company = user.getCompany();
            if (company.doesntOwns(persona)) {
                throw new IllegalArgumentException("Access denied");
            }

            personaService.addToConversation(id, conversationId);
        } catch (IllegalArgumentException e) {
        }

        return "/personas/added.xhtml";
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

    @GetMapping("/conversations/{conversationId}")
    public String getPersonasNotInConversation(@PathVariable Long conversationId, @AuthenticationPrincipal User user, Model model) {
        try {
            log.debug("Getting personas not in conversation {} for company {}", conversationId, user.getCompany().getId());
            final var company = user.getCompany();
            final var personas = personaService.findPersonasNotInConversation(company.getId(), conversationId);
            model.addAttribute("personas", personas);
            model.addAttribute("conversationId", conversationId);
            return "personas/personas";
        } catch (IllegalArgumentException e) {
            log.error("Error getting personas not in conversation: {}", e.getMessage());
            return "personas/personas";
        } catch (Exception e) {
            log.error("Unexpected error getting personas not in conversation: {}", e.getMessage());
            return "personas/personas";
        }
    }
}

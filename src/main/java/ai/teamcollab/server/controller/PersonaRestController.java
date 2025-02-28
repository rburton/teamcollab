package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.PersonaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping(value = "/api/personas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonaRestController {
    private static final Logger logger = LoggerFactory.getLogger(PersonaRestController.class);

    private final PersonaService personaService;
    private final CompanyService companyService;

    public PersonaRestController(PersonaService personaService, CompanyService companyService) {
        this.personaService = personaService;
        this.companyService = companyService;
    }

    @GetMapping("/all")
    public List<Persona> getAllPersonas(@AuthenticationPrincipal User user) {
        final var company = user.getCompany();
        return personaService.findByCompany(company.getId());
    }

    @PostMapping("/add-to-conversation")
    public ResponseEntity<Void> addPersonaToConversation(
            @RequestParam Long conversationId,
            @RequestParam Long personaId,
            @AuthenticationPrincipal User user) {
        try {
            // Verify the persona belongs to the user's company
            var persona = personaService.findById(personaId)
                    .orElseThrow(() -> new IllegalArgumentException("Persona not found"));
            if (!persona.getCompany().getId().equals(user.getCompany().getId())) {
                return ResponseEntity.status(403).build();
            }

            personaService.addToConversation(personaId, conversationId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error adding persona to conversation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

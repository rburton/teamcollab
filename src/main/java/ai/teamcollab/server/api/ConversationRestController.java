package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.AddPersonaRequest;
import ai.teamcollab.server.api.domain.PersonaAddedResponse;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.PersonaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConversationRestController {
    private static final Logger logger = LoggerFactory.getLogger(ConversationRestController.class);

    private final PersonaService personaService;

    public ConversationRestController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @PostMapping("/{conversationId}/persona")
    public ResponseEntity<PersonaAddedResponse> addPersonaToConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody AddPersonaRequest request,
            @AuthenticationPrincipal User user) {
        logger.debug("Adding persona {} to conversation {}", request.getPersonaId(), conversationId);

        try {
            // Verify the persona belongs to the user's company
            var persona = personaService.findById(request.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona not found"));

            personaService.addToConversation(persona.getId(), conversationId);

            // Fetch and return the updated persona
            var updatedPersona = personaService.findById(request.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona not found after update"));

            return ResponseEntity.ok(new PersonaAddedResponse(updatedPersona.getId(), updatedPersona.getName(), updatedPersona.getExpertises()));
        } catch (IllegalArgumentException e) {
            logger.error("Bad request while adding persona to conversation", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error adding persona to conversation", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

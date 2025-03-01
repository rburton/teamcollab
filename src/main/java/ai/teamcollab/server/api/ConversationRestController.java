package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.AddPersonaRequest;
import ai.teamcollab.server.api.domain.PersonaResponse;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.PersonaService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ai.teamcollab.server.api.domain.PersonaResponse.fromPersona;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.internalServerError;

@Log4j2
@Validated
@RestController
@RequestMapping(value = "/api/conversations", produces = APPLICATION_JSON_VALUE)
public class ConversationRestController {

    private final PersonaService personaService;

    public ConversationRestController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @PostMapping("/{conversationId}/persona")
    public ResponseEntity<PersonaResponse> addPersonaToConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody AddPersonaRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("Adding persona {} to conversation {}", request.getPersonaId(), conversationId);

        try {
            var persona = personaService.findById(request.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona not found"));

            personaService.addToConversation(persona.getId(), conversationId);

            var updatedPersona = personaService.findById(request.getPersonaId())
                    .orElseThrow(() -> new IllegalArgumentException("Persona not found after update"));

            return ResponseEntity.ok()
                    .contentType(APPLICATION_JSON)
                    .body(fromPersona(updatedPersona));
        } catch (IllegalArgumentException e) {
            log.error("Bad request while adding persona to conversation", e);
            return badRequest().build();
        } catch (Exception e) {
            log.error("Error adding persona to conversation", e);
            return internalServerError().build();
        }
    }

}

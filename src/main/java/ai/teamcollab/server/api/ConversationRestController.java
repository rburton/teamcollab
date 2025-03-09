package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.AddAssistantRequest;
import ai.teamcollab.server.api.domain.AssistantResponse;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.AssistantService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ai.teamcollab.server.api.domain.AssistantResponse.fromAssistant;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.internalServerError;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/api/conversations", produces = APPLICATION_JSON_VALUE)
public class ConversationRestController {

    private final AssistantService assistantService;

    public ConversationRestController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/{conversationId}/assistant")
    public ResponseEntity<AssistantResponse> addAssistantToConversation(
            @PathVariable Long conversationId,
            @Valid @RequestBody AddAssistantRequest request,
            @AuthenticationPrincipal User user) {

        log.debug("Adding assistant {} to conversation {}", request.getAssistantId(), conversationId);

        try {
            var assistant = assistantService.findById(request.getAssistantId())
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found"));

            assistantService.addToConversation(assistant.getId(), conversationId);

            var updatedAssistant = assistantService.findById(request.getAssistantId())
                    .orElseThrow(() -> new IllegalArgumentException("Assistant not found after update"));

            return ResponseEntity.ok()
                    .contentType(APPLICATION_JSON)
                    .body(fromAssistant(updatedAssistant));
        } catch (IllegalArgumentException e) {
            log.error("Bad request while adding assistant to conversation", e);
            return badRequest().build();
        } catch (Exception e) {
            log.error("Error adding assistant to conversation", e);
            return internalServerError().build();
        }
    }

}

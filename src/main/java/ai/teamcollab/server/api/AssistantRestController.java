package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.AssistantResponse;
import ai.teamcollab.server.domain.AssistantTone;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.AssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/assistants", produces = MediaType.APPLICATION_JSON_VALUE)
public class AssistantRestController {

    private final AssistantService assistantService;

    public AssistantRestController(AssistantService assistantService, CompanyService companyService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/all")
    public List<AssistantResponse> getAllAssistants(@AuthenticationPrincipal User user) {
        final var company = user.getCompany();
        return assistantService.findByCompany(company.getId())
                .stream()
                .map(AssistantResponse::fromAssistant)
                .toList();
    }

    @PostMapping("/{assistantId}/conversations/{conversationId}/mute")
    public ResponseEntity<Void> muteAssistantInConversation(
            @PathVariable Long assistantId,
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user) {
        log.debug("Muting assistant {} in conversation {}", assistantId, conversationId);
        assistantService.muteAssistantInConversation(assistantId, conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{assistantId}/conversations/{conversationId}/unmute")
    public ResponseEntity<Void> unmuteAssistantInConversation(
            @PathVariable Long assistantId,
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user) {
        log.debug("Unmuting assistant {} in conversation {}", assistantId, conversationId);
        assistantService.unmuteAssistantInConversation(assistantId, conversationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{assistantId}/conversations/{conversationId}/muted")
    public ResponseEntity<Boolean> isAssistantMutedInConversation(
            @PathVariable Long assistantId,
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user) {
        log.debug("Checking if assistant {} is muted in conversation {}", assistantId, conversationId);
        boolean muted = assistantService.isAssistantMutedInConversation(assistantId, conversationId);
        return ResponseEntity.ok(muted);
    }

    @PostMapping("/{assistantId}/conversations/{conversationId}/tone")
    public ResponseEntity<Void> setAssistantToneInConversation(
            @PathVariable Long assistantId,
            @PathVariable Long conversationId,
            @RequestParam("tone") String toneName,
            @AuthenticationPrincipal User user) {
        log.debug("Setting tone {} for assistant {} in conversation {}", toneName, assistantId, conversationId);
        try {
            assistantService.setAssistantToneInConversation(assistantId, conversationId, toneName);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid tone name: {}", toneName, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{assistantId}/conversations/{conversationId}/tone")
    public ResponseEntity<String> getAssistantToneInConversation(
            @PathVariable Long assistantId,
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User user) {
        log.debug("Getting tone for assistant {} in conversation {}", assistantId, conversationId);
        AssistantTone tone = assistantService.getAssistantToneInConversation(assistantId, conversationId);
        return ResponseEntity.ok(tone != null ? tone.getName() : "FORMAL");
    }
}

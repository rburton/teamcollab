package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.AssistantResponse;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.AssistantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}

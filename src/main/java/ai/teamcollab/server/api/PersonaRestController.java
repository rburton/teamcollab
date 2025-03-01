package ai.teamcollab.server.api;

import ai.teamcollab.server.api.domain.PersonaResponse;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.PersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/personas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonaRestController {

    private final PersonaService personaService;

    public PersonaRestController(PersonaService personaService, CompanyService companyService) {
        this.personaService = personaService;
    }

    @GetMapping("/all")
    public List<PersonaResponse> getAllPersonas(@AuthenticationPrincipal User user) {
        final var company = user.getCompany();
        return personaService.findByCompany(company.getId())
                .stream()
                .map(PersonaResponse::fromPersona)
                .toList();
    }

}

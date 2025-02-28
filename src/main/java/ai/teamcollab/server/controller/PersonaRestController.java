package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.PersonaService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/personas", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonaRestController {

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
}
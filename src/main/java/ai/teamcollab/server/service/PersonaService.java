package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.repository.ConversationRepository;
import ai.teamcollab.server.repository.PersonaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final ConversationRepository conversationRepository;

    public PersonaService(PersonaRepository personaRepository, ConversationRepository conversationRepository) {
        this.personaRepository = personaRepository;
        this.conversationRepository = conversationRepository;
    }

    public Persona createPersona(String name, String expertise, Company company) {
        log.debug("Creating new persona: {} with company ID: {}", name, company.getId());
        Persona persona = new Persona(name, expertise);
        persona.setCompany(company);
        return personaRepository.save(persona);
    }

    public Optional<Persona> findById(Long id) {
        return personaRepository.findById(id);
    }

    public List<Persona> findByCompany(Long companyId) {
        return personaRepository.findByCompanyIdOrCompanyIdIsNull(companyId);
    }

    public Persona updatePersona(Long id, String name, String expertiseAreas) {
        log.debug("Updating persona with ID: {}", id);

        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + id));

        persona.setName(name);
        persona.setExpertises(expertiseAreas);
        return personaRepository.save(persona);
    }

    @Transactional
    public Persona addToConversation(Long personaId, Long conversationId) {
        log.debug("Adding persona {} to conversation {}", personaId, conversationId);

        final var persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + personaId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));
        persona.addToConversation(conversation);
        return personaRepository.save(persona);
    }

    public void removeFromConversation(Long personaId, Long conversationId) {
        log.debug("Removing persona {} from conversation {}", personaId, conversationId);

        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona not found with ID: " + personaId));


        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));
        persona.removeFromConversation(conversation);
        personaRepository.save(persona);
    }

    public void deletePersona(Long id) {
        log.debug("Deleting persona with ID: {}", id);
        personaRepository.deleteById(id);
    }

}
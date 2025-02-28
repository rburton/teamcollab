package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Persona;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.PersonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaRestControllerTest {

    @Mock
    private PersonaService personaService;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private PersonaRestController controller;

    private User user;
    private Company company;
    private Persona persona;
    private Long conversationId;
    private Long personaId;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);

        user = new User();
        user.setId(1L);
        user.setCompany(company);

        persona = new Persona();
        persona.setId(2L);
        persona.setCompany(company);

        conversationId = 3L;
        personaId = persona.getId();
    }

    @Test
    void addPersonaToConversation_Success() {
        // Given
        when(personaService.findById(personaId)).thenReturn(Optional.of(persona));
        doNothing().when(personaService).addToConversation(personaId, conversationId);

        // When
        ResponseEntity<Void> response = controller.addPersonaToConversation(conversationId, personaId, user);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(personaService).addToConversation(personaId, conversationId);
    }

    @Test
    void addPersonaToConversation_PersonaNotFound() {
        // Given
        when(personaService.findById(personaId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = controller.addPersonaToConversation(conversationId, personaId, user);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verify(personaService, never()).addToConversation(anyLong(), anyLong());
    }

    @Test
    void addPersonaToConversation_DifferentCompany() {
        // Given
        Company otherCompany = new Company();
        otherCompany.setId(2L);
        persona.setCompany(otherCompany);
        
        when(personaService.findById(personaId)).thenReturn(Optional.of(persona));

        // When
        ResponseEntity<Void> response = controller.addPersonaToConversation(conversationId, personaId, user);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        verify(personaService, never()).addToConversation(anyLong(), anyLong());
    }

    @Test
    void addPersonaToConversation_ServiceException() {
        // Given
        when(personaService.findById(personaId)).thenReturn(Optional.of(persona));
        doThrow(new RuntimeException("Service error")).when(personaService).addToConversation(personaId, conversationId);

        // When
        ResponseEntity<Void> response = controller.addPersonaToConversation(conversationId, personaId, user);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
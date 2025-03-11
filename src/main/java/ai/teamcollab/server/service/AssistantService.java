package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Assistant;
import ai.teamcollab.server.domain.AssistantTone;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.repository.AssistantRepository;
import ai.teamcollab.server.repository.ConversationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class AssistantService {

    private final AssistantRepository assistantRepository;
    private final ConversationRepository conversationRepository;

    public AssistantService(AssistantRepository assistantRepository, ConversationRepository conversationRepository) {
        this.assistantRepository = assistantRepository;
        this.conversationRepository = conversationRepository;
    }

    public Assistant createAssistant(String name, String expertise, String description, Company company) {
        log.debug("Creating new assistant: {} with company ID: {}", name, company.getId());
        Assistant assistant = Assistant.builder()
                .name(name)
                .expertise(expertise)
                .expertisePrompt(description)
                .build();
        assistant.setCompany(company);
        return assistantRepository.save(assistant);
    }

    public Optional<Assistant> findById(Long id) {
        return assistantRepository.findById(id);
    }

    public List<Assistant> findByCompany(Long companyId) {
        return assistantRepository.findByCompanyIdOrCompanyIdIsNull(companyId);
    }

    public Assistant updateAssistant(Long id, String name, String expertiseAreas) {
        log.debug("Updating assistant with ID: {}", id);

        Assistant assistant = assistantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + id));

        assistant.setName(name);
        assistant.setExpertise(expertiseAreas);
        return assistantRepository.save(assistant);
    }

    @Transactional
    public Assistant addToConversation(Long assistantId, Long conversationId) {
        log.debug("Adding assistant {} to conversation {}", assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));
        assistant.addToConversation(conversation);
        return assistantRepository.save(assistant);
    }

    public void removeFromConversation(Long assistantId, Long conversationId) {
        log.debug("Removing assistant {} from conversation {}", assistantId, conversationId);

        Assistant assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));


        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));
        assistant.removeFromConversation(conversation);
        assistantRepository.save(assistant);
    }

    @Transactional
    public void muteAssistantInConversation(Long assistantId, Long conversationId) {
        log.debug("Muting assistant {} in conversation {}", assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        assistant.muteInConversation(conversation);
        assistantRepository.save(assistant);
    }

    @Transactional
    public void unmuteAssistantInConversation(Long assistantId, Long conversationId) {
        log.debug("Unmuting assistant {} in conversation {}", assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        assistant.unmuteInConversation(conversation);
        assistantRepository.save(assistant);
    }

    public boolean isAssistantMutedInConversation(Long assistantId, Long conversationId) {
        log.debug("Checking if assistant {} is muted in conversation {}", assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        return assistant.isMutedInConversation(conversation);
    }

    public List<Assistant> findAssistantsNotInConversation(Long companyId, Long conversationId) {
        log.debug("Finding assistants not in conversation {} for company {}", conversationId, companyId);
        return assistantRepository.findAssistantsNotInConversation(companyId, conversationId);
    }

    @Transactional
    public void setAssistantToneInConversation(Long assistantId, Long conversationId, AssistantTone tone) {
        log.debug("Setting tone {} for assistant {} in conversation {}", tone, assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        assistant.setToneInConversation(conversation, tone);
        assistantRepository.save(assistant);
    }

    public AssistantTone getAssistantToneInConversation(Long assistantId, Long conversationId) {
        log.debug("Getting tone for assistant {} in conversation {}", assistantId, conversationId);

        final var assistant = assistantRepository.findById(assistantId)
                .orElseThrow(() -> new IllegalArgumentException("Assistant not found with ID: " + assistantId));

        final var conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found with ID: " + conversationId));

        return assistant.getToneInConversation(conversation);
    }
}

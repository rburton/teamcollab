package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.AssistantTone;
import ai.teamcollab.server.repository.AssistantToneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing AssistantTone entities.
 */
@Slf4j
@Service
@Transactional
public class AssistantToneService {

    private final AssistantToneRepository assistantToneRepository;

    public AssistantToneService(AssistantToneRepository assistantToneRepository) {
        this.assistantToneRepository = assistantToneRepository;
    }

    /**
     * Find all assistant tones.
     *
     * @return a list of all assistant tones
     */
    public List<AssistantTone> findAll() {
        return assistantToneRepository.findAll();
    }

    /**
     * Find an assistant tone by its ID.
     *
     * @param id the ID of the tone
     * @return an Optional containing the tone if found, or empty if not found
     */
    public Optional<AssistantTone> findById(Long id) {
        return assistantToneRepository.findById(id);
    }

    /**
     * Find an assistant tone by its name (case-insensitive).
     *
     * @param name the name of the tone
     * @return an Optional containing the tone if found, or empty if not found
     */
    public Optional<AssistantTone> findByName(String name) {
        return assistantToneRepository.findByNameIgnoreCase(name);
    }

    /**
     * Find an assistant tone by its display name (case-insensitive).
     *
     * @param displayName the display name of the tone
     * @return an Optional containing the tone if found, or empty if not found
     */
    public Optional<AssistantTone> findByDisplayName(String displayName) {
        return assistantToneRepository.findByDisplayNameIgnoreCase(displayName);
    }

    /**
     * Create a new assistant tone.
     *
     * @param name        the name of the tone
     * @param displayName the display name of the tone
     * @param prompt      the prompt for the tone
     * @return the created tone
     */
    public AssistantTone createTone(String name, String displayName, String prompt) {
        log.debug("Creating new assistant tone: {}", name);

        final var tone = AssistantTone.builder()
                .name(name)
                .displayName(displayName)
                .prompt(prompt)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();

        return assistantToneRepository.save(tone);
    }

    /**
     * Update an existing assistant tone.
     *
     * @param id          the ID of the tone to update
     * @param displayName the new display name
     * @param prompt      the new prompt
     * @return the updated tone
     * @throws IllegalArgumentException if no tone is found with the given ID
     */
    public AssistantTone updateTone(Long id, String displayName, String prompt) {
        log.debug("Updating assistant tone with ID: {}", id);

        final var tone = assistantToneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No tone found with ID: " + id));

        tone.setDisplayName(displayName);
        tone.setPrompt(prompt);
        tone.setUpdatedTime(LocalDateTime.now());

        return assistantToneRepository.save(tone);
    }

    /**
     * Delete an assistant tone (soft delete).
     *
     * @param id the ID of the tone to delete
     * @throws IllegalArgumentException if no tone is found with the given ID
     */
    public void deleteTone(Long id) {
        log.debug("Deleting assistant tone with ID: {}", id);

        final var tone = assistantToneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No tone found with ID: " + id));

        tone.setDeletedTime(LocalDateTime.now());
        assistantToneRepository.save(tone);
    }

    /**
     * Get an assistant tone by name, creating it if it doesn't exist. This is a compatibility method to ease transition
     * from enum to entity.
     *
     * @param name the name of the tone
     * @return the found or created tone
     */
    public AssistantTone getOrCreateByName(String name) {
        return findByName(name).orElseGet(() -> {
            String displayName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            return createTone(name, displayName, "");
        });
    }

    /**
     * Find an assistant tone by name, throwing an exception if not found. This is a compatibility method to ease
     * transition from enum to entity.
     *
     * @param name the name of the tone
     * @return the found tone
     * @throws IllegalArgumentException if no tone is found with the given name
     */
    public AssistantTone findByNameOrThrow(String name) {
        return findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("No tone found with name: " + name));
    }
}
package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.AssistantTone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AssistantToneRepositoryTest {

    @Autowired
    private AssistantToneRepository assistantToneRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void findByNameIgnoreCaseShouldCacheResults() {
        // Given
        String toneName = "TestTone";
        AssistantTone tone = AssistantTone.builder()
                .name(toneName)
                .displayName("Test Tone")
                .prompt("This is a test tone")
                .createdTime(LocalDateTime.now())
                .build();

        assistantToneRepository.save(tone);

        // Clear the cache to ensure a fresh start
        cacheManager.getCache("assistantTonesByName").clear();

        // When
        // First call should hit the database
        Optional<AssistantTone> firstResult = assistantToneRepository.findByNameIgnoreCase(toneName);

        // Second call should hit the cache
        Optional<AssistantTone> secondResult = assistantToneRepository.findByNameIgnoreCase(toneName);

        // Then
        assertThat(firstResult).isPresent();
        assertThat(secondResult).isPresent();
        assertThat(firstResult.get()).isEqualTo(secondResult.get());

        // Verify the value is in the cache
        assertThat(cacheManager.getCache("assistantTonesByName").get(toneName.toLowerCase())).isNotNull();
    }

    @Test
    void saveShouldUpdateCache() {
        // Given
        String toneName = "UpdatedTone";
        AssistantTone tone = AssistantTone.builder()
                .name(toneName)
                .displayName("Updated Tone")
                .prompt("This is an updated tone")
                .createdTime(LocalDateTime.now())
                .build();

        // Save the initial entity
        AssistantTone savedTone = assistantToneRepository.save(tone);

        // When
        // Load the entity to ensure it's in the cache
        assistantToneRepository.findByNameIgnoreCase(toneName);

        // Update the entity
        savedTone.setPrompt("Updated prompt");
        assistantToneRepository.save(savedTone);

        // Then
        // Get from cache should return the updated entity
        Optional<AssistantTone> cachedTone = assistantToneRepository.findByNameIgnoreCase(toneName);

        assertThat(cachedTone).isPresent();
        assertThat(cachedTone.get().getPrompt()).isEqualTo("Updated prompt");
    }
}

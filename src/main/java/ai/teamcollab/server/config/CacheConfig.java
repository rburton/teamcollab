package ai.teamcollab.server.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Spring Cache.
 * Enables caching and defines the cache manager.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Defines the cache manager bean.
     * Uses a simple in-memory cache implementation based on ConcurrentHashMap.
     * 
     * @return the cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            // AssistantTone caches
            "assistantTones", "assistantTonesByName", "assistantTonesByDisplayName",
            // Assistant caches
            "assistants", "assistantsByCompany", "assistantsNotInConversation",
            // Company caches
            "companies",
            // LlmModel caches
            "llmModels", "llmModelsByModelId", "llmModelsByProvider", "llmModelsByNameAndProvider",
            // LlmProvider caches
            "llmProviders", "llmProvidersByName",
            // Role caches
            "roles", "rolesByName",
            // SystemSettings caches
            "systemSettings", "currentSystemSettings",
            // User caches
            "users", "usersByUsername", "usersByEmail", "usersByCompany", 
            "enabledUserCountByCompany", "disabledUserCountByCompany"
        );
    }
}

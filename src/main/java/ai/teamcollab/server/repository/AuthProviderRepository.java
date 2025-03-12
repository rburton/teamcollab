package ai.teamcollab.server.repository;

import ai.teamcollab.server.domain.AuthProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing AuthProvider entities.
 */
@Repository
public interface AuthProviderRepository extends CrudRepository<AuthProvider, Long> {
    
    /**
     * Find an AuthProvider by provider name and provider user ID.
     *
     * @param providerName the name of the provider
     * @param providerUserId the user ID from the provider
     * @return an Optional containing the AuthProvider if found, or empty if not found
     */
    Optional<AuthProvider> findByProviderNameAndProviderUserId(String providerName, String providerUserId);
    
    /**
     * Find an AuthProvider by provider name and provider email.
     *
     * @param providerName the name of the provider
     * @param providerEmail the email from the provider
     * @return an Optional containing the AuthProvider if found, or empty if not found
     */
    Optional<AuthProvider> findByProviderNameAndProviderEmail(String providerName, String providerEmail);
}
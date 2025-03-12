package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.AuthProvider;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.AuthProviderRepository;
import ai.teamcollab.server.repository.UserRepository;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

import static java.util.UUID.randomUUID;

/**
 * Service for handling OAuth2 authentication.
 */
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthProviderRepository authProviderRepository;
    private final CompanyService companyService;
    private final UserService userService;

    public OAuth2UserService(UserRepository userRepository,
                             AuthProviderRepository authProviderRepository, CompanyService companyService, UserService userService) {
        this.userRepository = userRepository;
        this.authProviderRepository = authProviderRepository;
        this.companyService = companyService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final var oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        final var providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase();
        final var attributes = oAuth2User.getAttributes();

        // Extract user details from OAuth2 provider
        final var email = (String) attributes.get("email");
        final var providerId = (String) attributes.get("sub");
        final var pictureUrl = (String) attributes.get("picture");

        // Check if user exists by provider and provider ID
        final var existingAuthProvider = authProviderRepository
                .findByProviderNameAndProviderUserId(providerName, providerId);

        User user;

        if (existingAuthProvider.isPresent()) {
            // User exists, get the user from the auth provider
            final var authProvider = existingAuthProvider.get();
            user = authProvider.getUser();

            if (user == null) {
                // If user is not set in the auth provider, we have an inconsistent state
                // Try to find a user with the same email as the auth provider
                final var providerEmail = authProvider.getProviderEmail();
                if (providerEmail != null) {
                    final var userByEmail = userRepository.findByEmail(providerEmail);
                    if (userByEmail.isPresent()) {
                        user = userByEmail.get();

                        // Update the auth provider with the user
                        authProvider.setUser(user);
                        authProviderRepository.save(authProvider);
                    } else {
                        // Create a new user if we can't find one
                        user = createNewUser(providerEmail);

                        // Update the auth provider with the user
                        authProvider.setUser(user);
                        authProviderRepository.save(authProvider);
                    }
                } else {
                    // If we don't have an email, we can't create a user
                    throw new RuntimeException("Auth provider has no user and no email: " + authProvider.getId());
                }
            }
        } else {
            // User doesn't exist, check if user exists by email
            final var userByEmail = userRepository.findByEmail(email);

            if (userByEmail.isPresent()) {
                // User exists by email, link the auth provider
                user = userByEmail.get();
            } else {
                // Create new user
                user = createNewUser(email);
            }

            // Create and link auth provider
            final var authProvider = createAuthProvider(providerName, providerId, email, pictureUrl);
            authProvider.setUser(user);
            authProviderRepository.save(authProvider);

            user.addAuthProvider(authProvider);
            user = userRepository.save(user);
        }

        final var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email");
    }

    private User createNewUser(String email) {

        final var company = Company.builder()
                .name("Company Name").build();

        final var savedCompany = companyService.createCompany(company);
        final var user = User.builder()
                .email(email)
                .username(email)
                .password(randomUUID().toString())
                .enabled(true)
                .build();
        user.setCompany(savedCompany);

        final var savedUser = userService.registerNewUser(user, "USER");

        companyService.addUserToCompany(savedCompany, savedUser);
        return savedUser;
    }

    private AuthProvider createAuthProvider(String providerName, String providerId, String email, String pictureUrl) {
        final var now = LocalDateTime.now();

        final var authProvider = new AuthProvider();
        authProvider.setProviderName(providerName);
        authProvider.setProviderUserId(providerId);
        authProvider.setProviderEmail(email);
        authProvider.setProviderPictureUrl(pictureUrl);
        authProvider.setCreatedAt(now);
        authProvider.setUpdatedAt(now);

        return authProviderRepository.save(authProvider);
    }
}

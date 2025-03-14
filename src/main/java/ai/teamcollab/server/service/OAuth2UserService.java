package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.AuthProvider;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.LoginUserDetails;
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
import java.util.Map;

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

    private LoginUserDetails processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        final var providerName = extractProviderName(oAuth2UserRequest);
        final var userInfo = extractUserInfo(oAuth2User.getAttributes());

        final var user = findOrCreateUser(providerName, userInfo);

        setupAuthentication(oAuth2User, user);

        return new LoginUserDetails(user);
    }

    private String extractProviderName(OAuth2UserRequest oAuth2UserRequest) {
        return oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase();
    }

    private UserInfo extractUserInfo(Map<String, Object> attributes) {
        return new UserInfo(
                (String) attributes.get("email"),
                (String) attributes.get("sub"),
                (String) attributes.get("picture")
        );
    }

    private User findOrCreateUser(String providerName, UserInfo userInfo) {
        final var existingAuthProvider = authProviderRepository
                .findByProviderNameAndProviderUserId(providerName, userInfo.providerId());

        if (existingAuthProvider.isPresent()) {
            return handleExistingAuthProvider(existingAuthProvider.get());
        } else {
            return handleNewAuthProvider(providerName, userInfo);
        }
    }

    private User handleExistingAuthProvider(AuthProvider authProvider) {
        var user = authProvider.getUser();

        if (user == null) {
            user = findOrCreateUserByEmail(authProvider.getProviderEmail());
            linkUserToAuthProvider(user, authProvider);
        }

        return user;
    }

    private User findOrCreateUserByEmail(String email) {
        if (email == null) {
            throw new RuntimeException("Cannot create user without email");
        }

        return userRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(email));
    }

    private void linkUserToAuthProvider(User user, AuthProvider authProvider) {
        authProvider.setUser(user);
        authProviderRepository.save(authProvider);
    }

    private User handleNewAuthProvider(String providerName, UserInfo userInfo) {
        final var user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> createNewUser(userInfo.email()));

        AuthProvider authProvider = createAuthProvider(
                providerName,
                userInfo.providerId(),
                userInfo.email(),
                userInfo.pictureUrl()
        );

        linkUserToAuthProvider(user, authProvider);

        user.addAuthProvider(authProvider);
        return userRepository.save(user);
    }

    private void setupAuthentication(OAuth2User oAuth2User, User user) {
        final var defaultOAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oAuth2User.getAttributes(),
                "email");

        final var loginUserDetails = new LoginUserDetails(user);
        final var authentication = new UsernamePasswordAuthenticationToken(
                loginUserDetails,
                null,
                loginUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
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

        final var savedUser = userService.registerNewUser(user, "USER", "ADMIN");

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

    /**
     * Record to hold user information extracted from OAuth2 provider.
     */
    private record UserInfo(String email, String providerId, String pictureUrl) {
    }
}

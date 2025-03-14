package ai.teamcollab.server.config;

import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import static ai.teamcollab.server.domain.Audit.AuditActionType.LOGIN;
import static ai.teamcollab.server.domain.Audit.AuditActionType.LOGOUT;

/**
 * Handlers for authentication events (login success, login failure, logout).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationEventHandlers {

    private final AuditService auditService;

    /**
     * Handle successful authentication events (login).
     *
     * @param event the authentication success event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        final var authentication = event.getAuthentication();
        final var principal = authentication.getPrincipal();

        if (principal instanceof LoginUserDetails loginUserDetails) {
            final var user = loginUserDetails.getUser();
            log.info("User logged in: {}", user.getUsername());

            auditService.createAuditEvent(
                    LOGIN,
                    user,
                    "User logged in successfully",
                    "User",
                    user.getId()
            );
        } else {
            log.info("Authentication success for principal: {}", principal);
        }
    }

    /**
     * Handle authentication failure events (login failure).
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        final var username = event.getAuthentication().getName();
        log.warn("Failed login attempt for user: {}", username);

        // We can't create an audit event with a user here since the authentication failed
        // But we could log this information in a separate table if needed
    }

    /**
     * Handle logout success events.
     *
     * @param event the logout success event
     */
    @EventListener
    public void onLogoutSuccess(LogoutSuccessEvent event) {
        final var authentication = event.getAuthentication();

        if (authentication != null) {
            final var principal = authentication.getPrincipal();

            if (principal instanceof LoginUserDetails loginUserDetails) {
                final var user = loginUserDetails.getUser();
                log.info("User logged out: {}", user.getUsername());

                auditService.createAuditEvent(
                        LOGOUT,
                        user,
                        "User logged out successfully",
                        "User",
                        user.getId()
                );
            } else {
                log.info("Logout success for principal: {}", principal);
            }
        }
    }
}
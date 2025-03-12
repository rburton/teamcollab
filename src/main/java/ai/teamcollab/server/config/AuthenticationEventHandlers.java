package ai.teamcollab.server.config;

import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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
        Authentication authentication = event.getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof LoginUserDetails loginUserDetails) {
            User user = loginUserDetails.getUser();
            log.info("User logged in: {}", user.getUsername());
            
            auditService.createAuditEvent(
                Audit.AuditActionType.LOGIN,
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
        String username = event.getAuthentication().getName();
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
        Authentication authentication = event.getAuthentication();
        
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof LoginUserDetails loginUserDetails) {
                User user = loginUserDetails.getUser();
                log.info("User logged out: {}", user.getUsername());
                
                auditService.createAuditEvent(
                    Audit.AuditActionType.LOGOUT,
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
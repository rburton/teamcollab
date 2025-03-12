package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.AuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling audit events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    /**
     * Create an audit event.
     *
     * @param actionType the type of action
     * @param user the user who performed the action
     * @param details additional details about the action
     * @param entityType the type of entity affected by the action
     * @param entityId the ID of the entity affected by the action
     * @return the created audit event
     */
    @Transactional
    public Audit createAuditEvent(Audit.AuditActionType actionType, User user, String details, 
                                 String entityType, Long entityId) {
        String ipAddress = getClientIpAddress();
        Company company = user.getCompany();

        Audit audit = Audit.builder()
                .actionType(actionType)
                .user(user)
                .username(user.getUsername())
                .ipAddress(ipAddress)
                .details(details)
                .timestamp(LocalDateTime.now())
                .entityType(entityType)
                .entityId(entityId)
                .company(company)
                .build();

        log.info("Creating audit event: {} for user: {} ({})", actionType, user.getUsername(), user.getId());
        return auditRepository.save(audit);
    }

    /**
     * Create an audit event for the currently authenticated user.
     *
     * @param actionType the type of action
     * @param details additional details about the action
     * @param entityType the type of entity affected by the action
     * @param entityId the ID of the entity affected by the action
     * @return the created audit event
     */
    @Transactional
    public Audit createAuditEvent(Audit.AuditActionType actionType, String details, 
                                 String entityType, Long entityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return createAuditEvent(actionType, user, details, entityType, entityId);
        }
        log.warn("Attempted to create audit event without authenticated user: {}", actionType);
        return null;
    }

    /**
     * Get the client IP address from the current request.
     *
     * @return the client IP address or "unknown" if it cannot be determined
     */
    private String getClientIpAddress() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(this::extractIpAddress)
                .orElse("unknown");
    }

    /**
     * Extract the IP address from the request.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Find all audit events for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of audit events
     */
    @Transactional(readOnly = true)
    public List<Audit> findAuditsByUser(Long userId) {
        return auditRepository.findByUserId(userId);
    }

    /**
     * Find all audit events for a specific company.
     *
     * @param companyId the ID of the company
     * @return a list of audit events
     */
    @Transactional(readOnly = true)
    public List<Audit> findAuditsByCompany(Long companyId) {
        return auditRepository.findByCompanyId(companyId);
    }

    /**
     * Find all audit events of a specific action type.
     *
     * @param actionType the type of action
     * @return a list of audit events
     */
    @Transactional(readOnly = true)
    public List<Audit> findAuditsByActionType(Audit.AuditActionType actionType) {
        return auditRepository.findByActionType(actionType);
    }

    /**
     * Find all audit events of a specific action type with pagination.
     *
     * @param actionType the type of action
     * @param pageable the pagination information
     * @return a page of audit events
     */
    @Transactional(readOnly = true)
    public Page<Audit> findAuditsByActionType(Audit.AuditActionType actionType, Pageable pageable) {
        return auditRepository.findByActionType(actionType, pageable);
    }

    /**
     * Find the most recent audit events, limited by count.
     *
     * @param limit the maximum number of events to return
     * @return a list of audit events
     */
    @Transactional(readOnly = true)
    public List<Audit> findMostRecentAudits(int limit) {
        return auditRepository.findMostRecentAudits(limit);
    }

    /**
     * Find all audit events with pagination.
     *
     * @param pageable the pagination information
     * @return a page of audit events
     */
    @Transactional(readOnly = true)
    public Page<Audit> findAllAudits(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    /**
     * Find the most recent audit events with pagination.
     *
     * @param pageable the pagination information
     * @return a page of audit events
     */
    @Transactional(readOnly = true)
    public Page<Audit> findMostRecentAudits(Pageable pageable) {
        return auditRepository.findMostRecentAudits(pageable);
    }
}

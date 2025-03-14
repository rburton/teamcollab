package ai.teamcollab.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an audit event in the system.
 * This tracks various user actions for security and compliance purposes.
 */
@Entity
@Table(name = "audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The type of action that was performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionType actionType;

    /**
     * The user who performed the action.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The username of the user who performed the action.
     * This is stored separately to maintain the audit record even if the user is deleted.
     */
    @Column(nullable = false)
    private String username;

    /**
     * The IP address from which the action was performed.
     */
    private String ipAddress;

    /**
     * Additional details about the action (e.g., changed values, affected entity IDs).
     */
    @Column(length = 1000)
    private String details;

    /**
     * The timestamp when the action occurred.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * The entity type that was affected by the action (e.g., "User", "Project").
     */
    private String entityType;

    /**
     * The ID of the entity that was affected by the action.
     */
    private Long entityId;

    /**
     * The company context in which the action occurred.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    /**
     * Enum representing the types of actions that can be audited.
     */
    public enum AuditActionType {
        LOGIN,
        LOGOUT,
        USER_CREATED,
        PASSWORD_CHANGED,
        ROLES_CHANGED,
        ACCOUNT_ENABLED,
        ACCOUNT_DISABLED,
        PROJECT_CREATED,
        CONVERSATION_CREATED,
        CONVERSATION_RESET,
        EMAIL_CHANGED,
        SUBSCRIPTION_CREATED,
        SUBSCRIPTION_CHANGED
    }
}

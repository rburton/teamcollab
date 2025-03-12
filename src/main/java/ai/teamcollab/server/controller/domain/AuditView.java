package ai.teamcollab.server.controller.domain;

import ai.teamcollab.server.domain.Audit;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class AuditView {

    private Audit.AuditActionType auditActionType;
    private String actionType;
    private String username;
    private String entityType;
    private String ipAddress;
    private String details;
    private LocalDateTime timestamp;

    public static AuditView from(Audit audit) {
        return AuditView.builder()
                .auditActionType(audit.getActionType())
                .details(audit.getDetails())
                .username(audit.getUsername())
                .entityType(audit.getEntityType())
                .ipAddress(audit.getIpAddress())
                .timestamp(audit.getTimestamp())
                .build();
    }

    public String getActionType() {
        return Optional.ofNullable(auditActionType)
                .map(Audit.AuditActionType::name)
                .orElse("");
    }

    public String getClassName() {
        return switch (auditActionType) {
            case LOGIN, LOGOUT -> "bg-blue-100 text-blue-800";
            case USER_CREATED, EMAIL_CHANGED, PASSWORD_CHANGED -> "bg-green-100 text-green-800";
            case ROLES_CHANGED -> "bg-purple-100 text-purple-800";
            case ACCOUNT_ENABLED -> "bg-green-100 text-green-800";
            case ACCOUNT_DISABLED -> "bg-red-100 text-red-800";
            case PROJECT_CREATED, CONVERSATION_CREATED -> "bg-yellow-100 text-yellow-800";
            case SUBSCRIPTION_CREATED, SUBSCRIPTION_CHANGED -> "bg-indigo-100 text-indigo-800";
            default -> "bg-gray-100 text-gray-800";
        };
    }
}

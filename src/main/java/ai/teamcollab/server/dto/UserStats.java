package ai.teamcollab.server.dto;

import lombok.Builder;

@Builder
public record UserStats(
    long activeUsers,
    long disabledUsers
) {
    public long totalUsers() {
        return activeUsers + disabledUsers;
    }
}
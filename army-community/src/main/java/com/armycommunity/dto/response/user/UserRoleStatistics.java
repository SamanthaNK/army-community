package com.armycommunity.dto.response.user;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleStatistics {
    private long totalUsers;
    private long regularUsers;
    private long verifiedUsers;
    private long moderators;
    private long admins;
    private long superAdmins;
    private long suspendedUsers;
}

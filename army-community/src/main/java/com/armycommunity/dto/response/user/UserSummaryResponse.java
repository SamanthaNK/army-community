package com.armycommunity.dto.response.user;

import com.armycommunity.model.user.UserRole;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private Long id;
    private String username;
    private String profileImagePath;
    private UserRole userRole;
    private boolean isVerified;
    private boolean isSuspended;
}

package com.armycommunity.dto.response.user;

import com.armycommunity.dto.response.post.PostResponse;
import com.armycommunity.model.user.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String profileImagePath;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole userRole;
    private LocalDateTime verifiedAt;
    private String verificationType;
    private String languagePreference;
    private String timeZone;
    private String oauthProvider;
    private boolean isActive;
    private LocalDateTime lastLoginAt;
    private boolean isVerified;
    private boolean canModerate;
    private boolean isSuspended;
    private LocalDateTime suspendedUntil;
    private String suspensionReason;
    private int followerCount;
    private int followingCount;
    private int postCount;
    private int collectionsCount;
    private List<UserSummaryResponse> recentFollowers;
    private List<PostResponse> recentPosts;
    private boolean isFollowing; // if current user is following this profile
    private boolean isOwnProfile; // if this is current user's own profile
}

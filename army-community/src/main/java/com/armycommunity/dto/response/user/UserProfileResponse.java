package com.armycommunity.dto.response.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String profileImagePath;
    private String bio;
    private String languagePreference;
    private String timeZone;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int followerCount;
    private int followingCount;
    private int postCount;
}

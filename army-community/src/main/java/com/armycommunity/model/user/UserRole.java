package com.armycommunity.model.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("Regular user with basic permissions"),
    VERIFIED("Verified user with enhanced privileges"),
    MODERATOR("Community moderator with content management rights"),
    ADMIN("Administrator with full system access"),
    SUPER_ADMIN("Super administrator with all permissions");

    private final String description;

    // Helper methods for role hierarchy checks
    public boolean hasPermissionLevel(UserRole requiredRole) {
        return this.ordinal() >= requiredRole.ordinal();
    }

    public boolean canModerateContent() {
        return this == MODERATOR || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageUsers() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canCreateVerifiedEvents() {
        return this == VERIFIED || this == MODERATOR || this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageSystem() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}

package com.armycommunity.model.user;

import com.armycommunity.model.post.Comment;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.post.Reaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole = UserRole.USER; // Default role

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_type")
    private String verificationType; // e.g., "MANUAL", "SOCIAL_MEDIA", "OFFICIAL"

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "language_preference", length = 10)
    private String languagePreference = "en"; // Default to English

    @Column(name = "timezone", length = 50)
    private String timezone = "UTC"; // Default to UTC

    @Column(name = "oauth_provider", length = 20)
    private String oauthProvider; // e.g., "google", "facebook", etc.

    @Column(name = "oauth_id", length = 100)
    private String oauthId; // Unique ID from the OAuth provider

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions = new HashSet<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserCollection> collections = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    public boolean hasRole(UserRole role) {
        return this.userRole == role;
    }

    public boolean hasMinimumRole(UserRole minimumRole) {
        return this.userRole.hasPermissionLevel(minimumRole);
    }

    public boolean isVerified() {
        return this.userRole.hasPermissionLevel(UserRole.VERIFIED);
    }

    public boolean canModerate() {
        return this.userRole.canModerateContent();
    }

    public boolean isSuspended() {
        return !this.isActive || (this.suspendedUntil != null && LocalDateTime.now().isBefore(this.suspendedUntil));
    }

    public boolean canCreateVerifiedEvents() {
        return this.userRole.canCreateVerifiedEvents();
    }
}

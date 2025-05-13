package com.armycommunity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 50)
    private String role = "USER";
    // Add any other fields you need for the user profile

    @Column(name = "language_preference", length = 50)
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
}

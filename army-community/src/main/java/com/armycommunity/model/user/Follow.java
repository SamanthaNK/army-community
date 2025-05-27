package com.armycommunity.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing a follow relationship between users.
 * This is a many-to-many relationship where one user can follow another.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "follows")
@IdClass(Follow.FollowId.class)
public class Follow {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Composite key class for Follow entity.
     * Contains follower and following IDs to form a unique identifier for the follow relationship.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowId implements Serializable {
        private Long follower;
        private Long following;
    }
}

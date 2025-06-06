package com.armycommunity.model.post;

import com.armycommunity.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing a reaction (like, dislike, love, etc.) to a post.
 * This is a many-to-many relationship between users and posts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reactions")
@IdClass(Reaction.ReactionId.class)
public class Reaction {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "reaction_type", nullable = false, length = 20)
    private String reactionType = "LIKE"; // Default to "LIKE"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Composite key class for Reaction entity.
     * Contains user and post IDs to form a unique identifier for the reaction.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionId implements Serializable {
        private Long user;
        private Long post;
    }
}

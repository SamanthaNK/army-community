package com.armycommunity.model.post;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entity representing the relationship between posts and tags.
 * Manages the many-to-many relationship between posts and tags.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post_tags")
@IdClass(PostTag.PostTagId.class)
public class PostTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    /**
     * Composite key class for PostTag entity.
     * Contains post and tag IDs to form a unique identifier for the relationship.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostTagId implements Serializable {
        private Long post;
        private Long tag;
    }
}

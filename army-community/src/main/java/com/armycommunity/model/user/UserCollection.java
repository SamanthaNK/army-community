package com.armycommunity.model.user;

import com.armycommunity.model.album.Album;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a user's collection of albums.
 * This is a many-to-many relationship between users and albums.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_collections")
@IdClass(UserCollection.UserCollectionId.class)
public class UserCollection {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Composite key class for UserCollection entity.
     * Contains user and album IDs to form a unique identifier for the collection.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCollectionId implements Serializable {
        private Long user;
        private Long album;
    }
}

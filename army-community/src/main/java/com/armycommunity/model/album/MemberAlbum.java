package com.armycommunity.model.album;

import com.armycommunity.model.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entity representing the relationship between BTS members and albums.
 * Manages the many-to-many relationship between members and albums, including member roles in each album.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member_albums")
@IdClass(MemberAlbum.MemberAlbumId.class)
public class MemberAlbum {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Id
    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "role", nullable = false)
    private String role;

    /**
     * Composite key class for MemberAlbum entity.
     * Contains member and album IDs to form a unique identifier for the relationship.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberAlbumId implements Serializable {
        private Long member;
        private Long album;
    }
}

package com.armycommunity.model.song;

import com.armycommunity.model.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entity representing the relationship between BTS members and songs.
 * Manages the many-to-many relationship between members and songs, including member roles in each song.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "song_members")
@IdClass(SongMember.SongMemberId.class)
public class SongMember {

    @Id
    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * Composite key class for SongMember entity.
     * Contains song and member IDs to form a unique identifier for the relationship.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongMemberId implements Serializable {
        private Long song;
        private Long member;
    }
}

package com.armycommunity.model.song;

import com.armycommunity.model.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongMemberId implements Serializable {
        private Long song;
        private Long member;
    }
}

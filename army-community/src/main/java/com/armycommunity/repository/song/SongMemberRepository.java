package com.armycommunity.repository.song;

import com.armycommunity.model.song.SongMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SongMemberRepository extends JpaRepository<SongMember, SongMember.SongMemberId> {
    void deleteBySongId(Long songId);

    void deleteByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM SongMember sm WHERE sm.song.id = :songId")
    void deleteAllBySongId(Long songId);

    @Modifying
    @Query("DELETE FROM SongMember sm WHERE sm.member.id = :memberId")
    void deleteAllByMemberId(Long memberId);
}

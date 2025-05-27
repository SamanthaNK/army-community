package com.armycommunity.repository.song;

import com.armycommunity.model.song.SongMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing SongMember entities.
 */
@Repository
public interface SongMemberRepository extends JpaRepository<SongMember, SongMember.SongMemberId> {
    void deleteBySongId(Long songId);

    void deleteByMemberId(Long memberId);
}

package com.armycommunity.repository.member;

import com.armycommunity.model.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByStageName(String stageName);

    @Query("SELECT m FROM Member m JOIN MemberLineAssignment mla ON m.id = mla.member.id " +
            "WHERE mla.lineType = ?1")
    List<Member> findByLineType(String lineType);

    @Query("SELECT m FROM Member m JOIN SongMember sm ON m.id = sm.member.id " +
            "WHERE sm.song.id = ?1")
    List<Member> findBySongId(Long songId);
}
package com.armycommunity.repository.member;

import com.armycommunity.model.member.MemberLineAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing MemberLineAssignment entities.
 */
@Repository
public interface MemberLineAssignmentRepository extends JpaRepository<MemberLineAssignment, Long> {
    List<MemberLineAssignment> findByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM MemberLineAssignment mla WHERE mla.member.id = :memberId")
    void deleteByMemberId(Long memberId);
}

package com.armycommunity.repository.album;

import com.armycommunity.model.album.MemberAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository interface for managing MemberAlbum entities.
 */
@Repository
public interface MemberAlbumRepository extends JpaRepository<MemberAlbum, MemberAlbum.MemberAlbumId> {
    List<MemberAlbum> findByAlbumId(Long albumId);

    List<MemberAlbum> findByMemberId(Long memberId);

    boolean existsByMemberIdAndAlbumId(Long memberId, Long albumId);

    @Modifying
    @Query("DELETE FROM MemberAlbum ma WHERE ma.album.id = :albumId")
    void deleteByAlbumId(Long albumId);

    @Modifying
    @Query("DELETE FROM MemberAlbum ma WHERE ma.member.id = :memberId")
    void deleteByMemberId(Long memberId);

    @Query("SELECT COUNT(ma) FROM MemberAlbum ma WHERE ma.album.id = :albumId")
    int countByAlbumId(Long albumId);
}

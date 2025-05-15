package com.armycommunity.repository.song;

import com.armycommunity.model.song.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findByAlbumId(Long albumId);

    List<Song> findByTitleContainingIgnoreCase(String title);

    List<Song> findByIsTitleTrue();

    List<Song> findByLanguage(String language);

    @Query("SELECT s FROM Song s WHERE s.title LIKE %?1% OR s.koreanTitle LIKE %?1% OR s.lyrics LIKE %?1%")
    Page<Song> searchSongs(String keyword, Pageable pageable);

    @Query("SELECT s FROM Song s JOIN SongMember sm ON s.id = sm.song.id WHERE sm.member.id = ?1")
    List<Song> findSongsByMemberId(Long memberId);
}

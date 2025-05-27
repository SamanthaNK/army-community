package com.armycommunity.repository.song;

import com.armycommunity.model.song.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Song entities.
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    @Query("SELECT s FROM Song s WHERE s.album.id = :albumId ORDER BY s.trackNumber")
    List<Song> findByAlbumId(@Param("albumId") Long albumId);

    List<Song> findByTitleContainingIgnoreCase(String title);

    List<Song> findByIsTitleTrue();

    List<Song> findByLanguage(String language);

    @Query("SELECT s FROM Song s WHERE s.title LIKE %?1% OR s.koreanTitle LIKE %?1% OR s.lyrics LIKE %?1%")
    Page<Song> searchSongs(String query, Pageable pageable);

    @Query("SELECT s FROM Song s JOIN SongMember sm ON s.id = sm.song.id WHERE sm.member.id = ?1")
    List<Song> findSongsByMemberId(Long memberId);

    @Query("SELECT s FROM Song s WHERE LOWER(s.title) = LOWER(:title) AND s.album.id = :albumId")
    boolean existsByTitleAndAlbumId(@Param("title") String title, @Param("albumId") Long albumId);

    @Query("SELECT s FROM Song s WHERE LOWER(s.title) = LOWER(:title) AND s.album.id = :albumId AND s.id != :id")
    boolean existsByTitleAndAlbumIdAndIdNot(@Param("title") String title, @Param("albumId") Long albumId, @Param("id") Long id);

    @Query("SELECT DISTINCT s FROM Song s WHERE LOWER(s.artist) LIKE LOWER(CONCAT('%', :artist, '%')) " +
            "ORDER BY s.releaseDate DESC")
    List<Song> findByArtistContainingIgnoreCase(@Param("artist") String artist);

    @Query("SELECT s FROM Song s WHERE s.releaseType = :releaseType ORDER BY s.releaseDate DESC")
    List<Song> findByReleaseType(@Param("releaseType") String releaseType);

    @Query("SELECT DISTINCT s FROM Song s " +
            "JOIN s.songMembers sm " +
            "WHERE LOWER(s.artist) NOT IN ('bts', '방탄소년단') " +
            "ORDER BY s.releaseDate DESC")
    List<Song> findFeaturingSongs();

    @Query("SELECT DISTINCT s FROM Song s " +
            "LEFT JOIN FETCH s.songMembers sm " +
            "WHERE LOWER(s.title) = LOWER(:title) AND LOWER(s.artist) = LOWER(:artist)")
    Optional<Song> findByTitleAndArtist(@Param("title") String title, @Param("artist") String artist);

}

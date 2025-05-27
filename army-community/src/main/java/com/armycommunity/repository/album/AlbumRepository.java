package com.armycommunity.repository.album;

import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Album entities.
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByAlbumType(AlbumType albumType);

    List<Album> findByArtist(String artist);

    List<Album> findByEraId(Long eraId);

    List<Album> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<Album> findByTitleAndArtist(String title, String artist);

    @Query("SELECT a FROM Album a WHERE a.title LIKE %?1% OR a.koreanTitle LIKE %?1%")
    List<Album> searchAlbums(String query);
}

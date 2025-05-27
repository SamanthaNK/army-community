package com.armycommunity.service.album;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;

import java.time.LocalDate;
import java.util.List;

public interface AlbumService {
    AlbumDetailResponse createAlbum(AlbumRequest request);

    AlbumDetailResponse getAlbumById(Long albumId);

    AlbumDetailResponse updateAlbum(Long albumId, AlbumRequest request);

    void deleteAlbum(Long albumId);

    List<AlbumSummaryResponse> getAllAlbums();

    List<AlbumSummaryResponse> getAlbumsByType(AlbumType albumType);

    List<AlbumSummaryResponse> getAlbumsByArtist(String artist);

    List<AlbumSummaryResponse> getAlbumsByEra(Long eraId);

    List<AlbumSummaryResponse> getAlbumsByReleaseDate(LocalDate startDate, LocalDate endDate);

    List<AlbumSummaryResponse> searchAlbums(String query);

    Album findOrCreateAlbum(AlbumRequest request);
}

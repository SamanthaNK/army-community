package com.armycommunity.service.album;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface AlbumService {
    AlbumDetailResponse createAlbum(AlbumRequest request, MultipartFile coverImage);

    AlbumDetailResponse getAlbumById(Long albumId);

    AlbumDetailResponse updateAlbum(Long albumId, AlbumRequest request, MultipartFile coverImage);

    void deleteAlbum(Long albumId);

    Page<AlbumSummaryResponse> getAllAlbums(Pageable pageable);

    List<AlbumSummaryResponse> getAlbumsByType(AlbumType albumType);

    List<AlbumSummaryResponse> getAlbumsByArtist(String artist);

    List<AlbumSummaryResponse> getAlbumsByEra(Long eraId);

    List<AlbumSummaryResponse> getAlbumsByReleaseDate(LocalDate startDate, LocalDate endDate);

    Page<AlbumSummaryResponse> searchAlbums(String keyword, Pageable pageable);

    Album findOrCreateAlbum(AlbumRequest request);
}

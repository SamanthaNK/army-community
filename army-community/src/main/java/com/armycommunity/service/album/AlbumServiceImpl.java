package com.armycommunity.service.album;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.AlbumMapper;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import com.armycommunity.model.album.Era;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import com.armycommunity.repository.song.SongRepository;
import com.armycommunity.repository.user.UserCollectionRepository;
import com.armycommunity.service.filestorage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final EraRepository eraRepository;
    private final SongRepository songRepository;
    private final UserCollectionRepository userCollectionRepository;
    private final AlbumMapper albumMapper;
    private final FileStorageService fileStorageService;

    @Override
    public AlbumDetailResponse createAlbum(AlbumRequest request, MultipartFile coverImage) {
        return null;
    }

    @Override
    public AlbumDetailResponse getAlbumById(Long albumId) {
        // TODO: implement
        return null;
    }

    @Override
    public AlbumDetailResponse updateAlbum(Long albumId, AlbumRequest request, MultipartFile coverImage) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteAlbum(Long albumId) {
        // TODO: implement
    }

    @Override
    public Page<AlbumSummaryResponse> getAllAlbums(Pageable pageable) {
        // TODO: implement
        return null;
    }

    @Override
    public List<AlbumSummaryResponse> getAlbumsByType(AlbumType albumType) {
        // TODO: implement
        return null;
    }

    @Override
    public List<AlbumSummaryResponse> getAlbumsByArtist(String artist) {
        // TODO: implement
        return null;
    }

    @Override
    public List<AlbumSummaryResponse> getAlbumsByEra(Long eraId) {
        // TODO: implement
        return null;
    }

    @Override
    public List<AlbumSummaryResponse> getAlbumsByReleaseDate(LocalDate startDate, LocalDate endDate) {
        // TODO: implement
        return null;
    }

    @Override
    public Page<AlbumSummaryResponse> searchAlbums(String keyword, Pageable pageable) {
        // TODO: implement
        return null;
    }

    @Override
    public Album findOrCreateAlbum(AlbumRequest request) {
        // TODO: implement
        return null;
    }
}

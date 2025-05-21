package com.armycommunity.service.album;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.AlbumMapper;
import com.armycommunity.mapper.SongMapper;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import com.armycommunity.model.album.Era;
import com.armycommunity.model.song.Song;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import com.armycommunity.repository.song.SongRepository;
import com.armycommunity.repository.user.UserCollectionRepository;
import com.armycommunity.service.filestorage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AlbumService interface for managing BTS albums
 */
@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final EraRepository eraRepository;
    private final SongRepository songRepository;
    private final UserCollectionRepository userCollectionRepository;
    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;

    /**
     * Creates a new album with optional cover image
     *
     * @param request The album request containing album details
     * @param coverImage Optional cover image file
     * @return The created album detailed response
     */
    @Override
    @Transactional
    public AlbumDetailResponse createAlbum(AlbumRequest request, MultipartFile coverImage) {
        // Create and map the album entity
        Album album = albumMapper.toEntity(request);

        // Set era if provided
        if (request.getEraId() != null) {
            Era era = eraRepository.findById(request.getEraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + request.getEraId()));
            album.setEra(era);
        }

        // Save the cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String imagePath = fileStorageService.storeFile(coverImage, "albums");
                album.setCoverImagePath(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store album cover image", e);
            }
        }

        // Save and enhance the response
        Album savedAlbum = albumRepository.save(album);
        return enrichAlbumDetailResponse(savedAlbum);
    }

    /**
     * Retrieves an album by its ID with detailed information
     *
     * @param albumId The ID of the album to retrieve
     * @return The detailed album response
     */
    @Override
    @Transactional(readOnly = true)
    public AlbumDetailResponse getAlbumById(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));
        return enrichAlbumDetailResponse(album);
    }

    /**
     * Updates an existing album with optional new cover image
     *
     * @param albumId The ID of the album to update
     * @param request The updated album request
     * @param coverImage Optional new cover image file
     * @return The updated album detailed response
     */
    @Override
    @Transactional
    public AlbumDetailResponse updateAlbum(Long albumId, AlbumRequest request, MultipartFile coverImage) {
        // Find the album to update
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));

        // Update the album entity
        albumMapper.updateEntity(request, album);

        // Update the era if provided and different
        if (request.getEraId() != null &&
                (album.getEra() == null || !album.getEra().getId().equals(request.getEraId()))) {
            Era era = eraRepository.findById(request.getEraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + request.getEraId()));
            album.setEra(era);
        }

        // Update the cover image if provided
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                // Delete old image if exists
                if (album.getCoverImagePath() != null) {
                    fileStorageService.deleteFile(album.getCoverImagePath());
                }
                String imagePath = fileStorageService.storeFile(coverImage, "albums");
                album.setCoverImagePath(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store cover image", e);
            }
        }

        // Save and enhance the response
        Album updatedAlbum = albumRepository.save(album);
        return enrichAlbumDetailResponse(updatedAlbum);
    }

    /**
     * Deletes an album by its ID
     *
     * @param albumId The ID of the album to delete
     */
    @Override
    @Transactional
    public void deleteAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));

        // Delete cover image if exists
        if (album.getCoverImagePath() != null) {
            try {
                fileStorageService.deleteFile(album.getCoverImagePath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete cover image", e);
            }
        }

        albumRepository.delete(album);
    }

    /**
     * Retrieves all albums with pagination
     *
     * @param pageable Pagination information
     * @return A page of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlbumSummaryResponse> getAllAlbums(Pageable pageable) {
        return albumRepository.findAll(pageable)
                .map(this::enrichAlbumSummaryResponse);
    }

    /**
     * Retrieves albums by their type
     *
     * @param albumType The type of albums to retrieve
     * @return A list of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByType(AlbumType albumType) {
        return albumRepository.findByAlbumType(albumType).stream()
                .map(this::enrichAlbumSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves albums by the artist
     *
     * @param artist The artist name (e.g., "BTS", "RM")
     * @return A list of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByArtist(String artist) {
        return albumRepository.findByArtist(artist).stream()
                .map(this::enrichAlbumSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves albums by era
     *
     * @param eraId The ID of the era
     * @return A list of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByEra(Long eraId) {
        // Check if the era exists
        if (!eraRepository.existsById(eraId)) {
            throw new ResourceNotFoundException("Era not found with id: " + eraId);
        }

        return albumRepository.findByEraId(eraId).stream()
                .map(this::enrichAlbumSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves albums released between two dates
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return A list of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByReleaseDate(LocalDate startDate, LocalDate endDate) {
        return albumRepository.findByReleaseDateBetween(startDate, endDate).stream()
                .map(this::enrichAlbumSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Searches for albums based on a keyword
     *
     * @param keyword The keyword to search for
     * @param pageable The pagination information
     * @return A page of album summary responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlbumSummaryResponse> searchAlbums(String keyword, Pageable pageable) {
        return albumRepository.searchAlbums(keyword, pageable)
                .map(this::enrichAlbumSummaryResponse);
    }

    /**
     * Finds an existing album or creates a new one based on the request
     *
     * @param request The album request
     * @return The found or created album entity
     */
    @Override
    @Transactional
    public Album findOrCreateAlbum(AlbumRequest request) {
        // Try to find an existing album
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtist(request.getTitle(), request.getArtist());

        if (existingAlbum.isPresent()) {
            return existingAlbum.get();
        }

        // Create a new album if not found
        Album album = albumMapper.toEntity(request);

        // Set the era if provided
        if (request.getEraId() != null) {
            Era era = eraRepository.findById(request.getEraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Era not found with id: " + request.getEraId()));
            album.setEra(era);
        }

        return albumRepository.save(album);
    }

    // Helper methods
    /**
     * Enhances an album summary response with additional information
     *
     * @param album The album entity
     * @return The enhanced album summary response
     */
    private AlbumSummaryResponse enrichAlbumSummaryResponse(Album album) {
        AlbumSummaryResponse response = albumMapper.toSummaryResponse(album);

        // Get song count and total duration
        List<Song> songs = songRepository.findByAlbumId(album.getId());
        response.setSongCount(songs.size());
        response.setTotalDuration(songs.stream().mapToInt(Song::getDuration).sum());

        // Get collection count
        Integer collectionCount = userCollectionRepository.countByAlbumId(album.getId());
        response.setCollectionCount(collectionCount);

        return response;
    }

    /**
     * Enhances an album detail response with additional information
     *
     * @param album The album entity
     * @return The enhanced album detail response
     */
    private AlbumDetailResponse enrichAlbumDetailResponse(Album album) {
        AlbumDetailResponse response = albumMapper.toDetailResponse(album);

        // Get songs
        List<Song> songs = songRepository.findByAlbumId(album.getId());
        List<SongSummaryResponse> songResponses = songs.stream()
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
        response.setSongs(songResponses);

        // Get collection count
        Integer collectionCount = userCollectionRepository.countByAlbumId(album.getId());
        response.setCollectionCount(collectionCount);

        return response;
    }
}

package com.armycommunity.service.album;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.exception.DuplicateResourceException;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.AlbumMapper;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import com.armycommunity.model.album.Era;
import com.armycommunity.model.album.MemberAlbum;
import com.armycommunity.model.member.Member;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import com.armycommunity.repository.album.MemberAlbumRepository;
import com.armycommunity.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of AlbumService interface for managing BTS albums
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final EraRepository eraRepository;
    private final MemberRepository memberRepository;
    private final MemberAlbumRepository memberAlbumRepository;
    private final AlbumMapper albumMapper;

    @Override
    @Transactional
    public AlbumDetailResponse createAlbum(AlbumRequest request) {
        log.info("Creating new album with title: {}", request.getTitle());

        // Check for duplicate album
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtist(
                request.getTitle(), request.getArtist());

        if (existingAlbum.isPresent()) {
            throw new DuplicateResourceException(
                    "Album with title '" + request.getTitle() + "' by " + request.getArtist() + " already exists");
        }

        // Create album entity
        Album album = albumMapper.toEntity(request);

        // Set album type
        try {
            album.setAlbumType(AlbumType.valueOf(request.getAlbumType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid album type: " + request.getAlbumType());
        }

        // Set era if provided
        if (request.getEraId() != null) {
            Era era = eraRepository.findById(request.getEraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Era not found with ID: " + request.getEraId()));
            album.setEra(era);
            log.debug("Associated album with era: {}", era.getName());
        }

        // Save album first to get ID
        Album savedAlbum = albumRepository.save(album);
        log.info("Album created successfully with ID: {}", savedAlbum.getId());

        // Handle member associations for solo/sub-unit albums
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            Set<MemberAlbum> memberAlbums = new HashSet<>();

            for (Long memberId : request.getMemberIds()) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

                MemberAlbum memberAlbum = new MemberAlbum();
                memberAlbum.setMember(member);
                memberAlbum.setAlbum(savedAlbum);
                memberAlbums.add(memberAlbum);

                log.debug("Associated member {} with album {}", member.getStageName(), savedAlbum.getTitle());
            }

            memberAlbumRepository.saveAll(memberAlbums);
            savedAlbum.setMemberAlbums(memberAlbums);
        }

        AlbumDetailResponse response = albumMapper.toDetailResponse(savedAlbum);
        log.info("Album creation completed for: {}", savedAlbum.getTitle());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumDetailResponse getAlbumById(Long albumId) {
        log.debug("Fetching album with ID: {}", albumId);
        return albumRepository.findById(albumId)
                .map(albumMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + albumId));
    }

    @Override
    @Transactional
    public AlbumDetailResponse updateAlbum(Long albumId, AlbumRequest request) {
        log.info("Updating album with ID: {}", albumId);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with ID: " + albumId));

        String originalTitle = album.getTitle();

        // Check for duplicate if title or artist changed
        if (!album.getTitle().equals(request.getTitle()) ||
                !album.getArtist().equals(request.getArtist())) {

            Optional<Album> existingAlbum = albumRepository.findByTitleAndArtist(
                    request.getTitle(), request.getArtist());

            if (existingAlbum.isPresent() && !existingAlbum.get().getId().equals(albumId)) {
                throw new DuplicateResourceException(
                        "Album with title '" + request.getTitle() + "' by " + request.getArtist() + " already exists");
            }
        }

        // Update album fields
        albumMapper.updateAlbumFromRequest(request, album);

        // Update the album type
        if (request.getAlbumType() != null) {
            try {
                album.setAlbumType(AlbumType.valueOf(request.getAlbumType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid album type: " + request.getAlbumType());
            }
        }

        // Update era if provided
        if (request.getEraId() != null) {
            Era era = eraRepository.findById(request.getEraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Era not found with ID: " + request.getEraId()));
            album.setEra(era);
            log.debug("Updated album era to: {}", era.getName());
        }

        // Update member associations
        if (request.getMemberIds() != null) {
            // Remove existing associations
            memberAlbumRepository.deleteByAlbumId(albumId);

            // Add new associations
            if (!request.getMemberIds().isEmpty()) {
                Set<MemberAlbum> memberAlbums = new HashSet<>();

                for (Long memberId : request.getMemberIds()) {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

                    MemberAlbum memberAlbum = new MemberAlbum();
                    memberAlbum.setMember(member);
                    memberAlbum.setAlbum(album);
                    memberAlbums.add(memberAlbum);
                }

                memberAlbumRepository.saveAll(memberAlbums);
                album.setMemberAlbums(memberAlbums);
                log.debug("Updated member associations for album: {}", album.getTitle());
            }
        }

        Album updatedAlbum = albumRepository.save(album);
        log.info("Album updated successfully: {} -> {}", originalTitle, updatedAlbum.getTitle());

        return albumMapper.toDetailResponse(updatedAlbum);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId) {
        log.info("Deleting album with ID: {}", albumId);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResourceNotFoundException("Album not found with ID: " + albumId));

        String albumTitle = album.getTitle();

        // Delete member associations first
        memberAlbumRepository.deleteByAlbumId(albumId);
        log.debug("Deleted member associations for album: {}", albumTitle);

        // Delete the album
        albumRepository.delete(album);
        log.info("Album deleted successfully: {}", albumTitle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAllAlbums() {
        log.debug("Fetching all albums");
        List<Album> albums = albumRepository.findAll();
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByType(AlbumType albumType) {
        log.debug("Fetching albums of type: {}", albumType);
        List<Album> albums = albumRepository.findByAlbumType(albumType);
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByArtist(String artist) {
        log.debug("Fetching albums by artist: {}", artist);
        List<Album> albums = albumRepository.findByArtist(artist);
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByEra(Long eraId) {
        log.debug("Fetching albums for era with ID: {}", eraId);

        // Check if the era exists
        if (!eraRepository.existsById(eraId)) {
            throw new ResourceNotFoundException("Era not found with id: " + eraId);
        }

        List<Album> albums = albumRepository.findByEraId(eraId);
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> getAlbumsByReleaseDate(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching albums released between {} and {}", startDate, endDate);
        List<Album> albums = albumRepository.findByReleaseDateBetween(startDate, endDate);
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumSummaryResponse> searchAlbums(String query) {
        log.debug("Searching albums with query: {}", query);
        List<Album> albums = albumRepository.searchAlbums(query);
        return albumMapper.toSummaryResponseList(albums);
    }

    @Override
    @Transactional
    public Album findOrCreateAlbum(AlbumRequest request) {
        log.debug("Finding or creating album: {} by {}", request.getTitle(), request.getArtist());

        // Try to find an existing album
        Optional<Album> existingAlbum = albumRepository.findByTitleAndArtist(request.getTitle(), request.getArtist());

        if (existingAlbum.isPresent()) {
            return existingAlbum.get();
        }

        // Create a new album if not found
        Album album = albumMapper.toEntity(request);

        return albumRepository.save(album);
    }
}

package com.armycommunity.service.song;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.exception.DuplicateResourceException;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.SongMapper;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.song.Song;
import com.armycommunity.model.song.SongMember;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.member.MemberRepository;
import com.armycommunity.repository.song.SongMemberRepository;
import com.armycommunity.repository.song.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final MemberRepository memberRepository;
    private final SongMemberRepository songMemberRepository;
    private final SongMapper songMapper;

    @Override
    @Transactional
    public SongDetailResponse createSong(SongRequest request) {
        log.info("Creating new song with title: {}", request.getTitle());

        // Check for duplicate song in the same album
        if (request.getAlbumId() != null) {
            boolean exists = songRepository.existsByTitleAndAlbumId(request.getTitle(), request.getAlbumId());
            if (exists) {
                throw new DuplicateResourceException("Song with title '" + request.getTitle() + "' already exists in this album");
            }
        }

        Song song = songMapper.toEntity(request);

        // Set album if provided
        if (request.getAlbumId() != null) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found with ID: " + request.getAlbumId()));
            song.setAlbum(album);
        }

        Song savedSong = songRepository.save(song);
        log.info("Successfully created song with ID: {}", savedSong.getId());

        // Associate members with the song
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            associateMembersWithSong(savedSong, request.getMemberIds());
        }

        return songMapper.toDetailResponse(savedSong);
    }

    @Override
    @Transactional(readOnly = true)
    public SongDetailResponse getSongById(Long songId) {
        log.debug("Retrieving song with ID: {}", songId);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with ID: " + songId));

        log.debug("Successfully retrieved song: {}", song.getTitle());
        return songMapper.toDetailResponse(song);
    }

    @Override
    @Transactional
    public SongDetailResponse updateSong(Long songId, SongRequest request) {
        log.info("Updating song with ID: {}", songId);

        Song existingSong = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with ID: " + songId));

        // Check for duplicate title in same album (excluding current song)
        if (request.getAlbumId() != null && !request.getTitle().equals(existingSong.getTitle())) {
            boolean exists = songRepository.existsByTitleAndAlbumIdAndIdNot(
                    request.getTitle(), request.getAlbumId(), songId);
            if (exists) {
                throw new DuplicateResourceException("Song with title '" + request.getTitle() + "' already exists in this album");
            }
        }

        songMapper.updateSongFromRequest(request, existingSong);

        // Update album association if changed
        if (request.getAlbumId() != null &&
                (existingSong.getAlbum() == null || !existingSong.getAlbum().getId().equals(request.getAlbumId()))) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found with ID: " + request.getAlbumId()));
            existingSong.setAlbum(album);
            log.debug("Updated song album association to: {}", album.getTitle());
        }

        Song updatedSong = songRepository.save(existingSong);
        log.info("Successfully updated song with ID: {}", songId);

        // Update member associations
        if (request.getMemberIds() != null) {
            updateSongMemberAssociations(updatedSong, request.getMemberIds());
        }

        return songMapper.toDetailResponse(updatedSong);
    }

    @Override
    @Transactional
    public void deleteSong(Long songId) {
        log.info("Deleting song with ID: {}", songId);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with ID: " + songId));

        // Remove member associations first
        songMemberRepository.deleteBySongId(songId);

        songRepository.delete(song);
        log.info("Successfully deleted song: {}", song.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByAlbum(Long albumId) {
        log.debug("Retrieving songs for album with ID: {}", albumId);

        if (!albumRepository.existsById(albumId)) {
            throw new ResourceNotFoundException("Album not found with id: " + albumId);
        }

        List<Song> songs = songRepository.findByAlbumId(albumId);
        log.debug("Found {} songs for album ID: {}", songs.size(), albumId);

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getTitleTracks() {
        log.debug("Retrieving all title tracks");

        List<Song> titleTracks = songRepository.findByIsTitleTrue();
        log.debug("Found {} title tracks", titleTracks.size());

        return songMapper.toSummaryResponseList(titleTracks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByMember(Long memberId) {
        log.debug("Retrieving songs for member ID: {}", memberId);

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with ID: " + memberId);
        }

        List<Song> songs = songRepository.findSongsByMemberId(memberId);
        log.debug("Found {} songs for member ID: {}", songs.size(), memberId);

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByLanguage(String language) {
        log.debug("Retrieving songs by language: {}", language);

        List<Song> songs = songRepository.findByLanguage(language);
        log.debug("Found {} songs in language: {}", songs.size(), language);

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SongSummaryResponse> searchSongs(String query, Pageable pageable) {
        log.debug("Searching songs with query: {}", query);

        Page<Song> songPage = songRepository.searchSongs(query, pageable);
        log.debug("Found {} songs matching query: {}", songPage.getTotalElements(), query);
        return songPage.map(songMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByArtist(String artist) {
        log.debug("Retrieving songs by artist: {}", artist);

        List<Song> songs = songRepository.findByArtistContainingIgnoreCase(artist);
        log.debug("Found {} songs by artist: {}", songs.size(), artist);

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByReleaseType(String releaseType) {
        log.debug("Retrieving songs by release type: {}", releaseType);

        List<Song> songs = songRepository.findByReleaseType(releaseType);
        log.debug("Found {} songs with release type: {}", songs.size(), releaseType);

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getFeaturingSongs() {
        log.debug("Retrieving songs where BTS members feature");

        // Songs where BTS members participate but BTS is not the main artist
        List<Song> songs = songRepository.findFeaturingSongs();
        log.debug("Found {} featuring songs", songs.size());

        return songMapper.toSummaryResponseList(songs);
    }

    @Override
    @Transactional
    public Song findOrCreateSong(SongRequest request) {
        log.debug("Finding or creating song with title: {}", request.getTitle());

        Song existingSong = songRepository.findByTitleAndArtist(request.getTitle(), request.getArtist())
                .orElse(null);

        if (existingSong != null) {
            log.debug("Found existing song: {}", existingSong.getTitle());
            return existingSong;
        }

        // Create new song
        log.debug("Creating new song as it doesn't exist");
        SongDetailResponse response = createSong(request);
        return songRepository.findById(response.getId()).orElseThrow();
    }

    // Helper methods

    private void associateMembersWithSong(Song song, List<Long> memberIds) {
        log.debug("Associating {} members with song: {}", memberIds.size(), song.getTitle());

        try {
            Set<SongMember> songMembers = new HashSet<>();

            for (Long memberId : memberIds) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> {
                            log.error("Member not found with ID: {}", memberId);
                            return new ResourceNotFoundException("Member not found with ID: " + memberId);
                        });

                SongMember songMember = new SongMember();
                songMember.setSong(song);
                songMember.setMember(member);
                songMembers.add(songMember);

                log.debug("Associated member {} with song {}", member.getStageName(), song.getTitle());
            }

            song.setSongMembers(songMembers);
            log.info("Successfully associated {} members with song: {}", memberIds.size(), song.getTitle());

        } catch (Exception e) {
            log.error("Error associating members with song: {}", song.getTitle(), e);
            throw e;
        }
    }

    private void updateSongMemberAssociations(Song song, List<Long> memberIds) {
        log.debug("Updating member associations for song: {}", song.getTitle());

        try {
            // Remove existing associations
            songMemberRepository.deleteBySongId(song.getId());
            log.debug("Removed existing member associations for song: {}", song.getTitle());

            // Add new associations
            if (!memberIds.isEmpty()) {
                associateMembersWithSong(song, memberIds);
            }

        } catch (Exception e) {
            log.error("Error updating member associations for song: {}", song.getTitle(), e);
            throw e;
        }
    }
}

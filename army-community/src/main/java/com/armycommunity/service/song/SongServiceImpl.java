package com.armycommunity.service.song;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final MemberRepository memberRepository;
    private final SongMemberRepository songMemberRepository;
    private final SongMapper songMapper;

    @Override
    @Transactional
    public SongDetailResponse createSong(SongRequest request) {
        // Find the album if specified
        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + request.getAlbumId()));
        }

        // Create the song entity
        Song song = songMapper.toEntity(request);
        song.setAlbum(album);
        Song savedSong = songRepository.save(song);

        // Associate members with the song if provided
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            for (Long memberId : request.getMemberIds()) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

                SongMember songMember = new SongMember();
                songMember.setSong(savedSong);
                songMember.setMember(member);
                songMemberRepository.save(songMember);
            }
        }

        return getSongById(savedSong.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SongDetailResponse getSongById(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        SongDetailResponse response = songMapper.toDetailResponse(song);

        // Add members who performed on the song
        List<Member> members = memberRepository.findBySongId(songId);
        response.setMembers(members.stream()
                .map(member -> {
                    MemberSummaryResponse memberResponse = new MemberSummaryResponse();
                    memberResponse.setId(member.getId());
                    memberResponse.setStageName(member.getStageName());
                    // Set other required fields from Member to MemberSummaryResponse
                    return memberResponse;
                })
                .collect(Collectors.toList()));

        // Add music videos for the song
        // This will be handled by MusicVideoService, so we'll leave it empty for now
        response.setMusicVideos(new ArrayList<>());

        return response;
    }

    @Override
    @Transactional
    public SongDetailResponse updateSong(Long songId, SongRequest request) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + songId));

        // Update album if changed
        if (request.getAlbumId() != null &&
                (song.getAlbum() == null || !request.getAlbumId().equals(song.getAlbum().getId()))) {
            Album album = albumRepository.findById(request.getAlbumId())
                    .orElseThrow(() -> new ResourceNotFoundException("Album not found with id: " + request.getAlbumId()));
            song.setAlbum(album);
        }

        songMapper.updateEntity(request, song);
        Song updatedSong = songRepository.save(song);

        // Update member associations if provided
        if (request.getMemberIds() != null) {
            // Remove existing associations
            songMemberRepository.deleteBySongId(songId);

            // Add new associations
            for (Long memberId : request.getMemberIds()) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

                SongMember songMember = new SongMember();
                songMember.setSong(updatedSong);
                songMember.setMember(member);
                songMemberRepository.save(songMember);
            }
        }

        return getSongById(updatedSong.getId());
    }

    @Override
    @Transactional
    public void deleteSong(Long songId) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song not found with id: " + songId);
        }

        // Remove member associations
        songMemberRepository.deleteBySongId(songId);

        // Delete the song
        songRepository.deleteById(songId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByAlbum(Long albumId) {
        if (!albumRepository.existsById(albumId)) {
            throw new ResourceNotFoundException("Album not found with id: " + albumId);
        }

        List<Song> songs = songRepository.findByAlbumId(albumId);
        return songs.stream()
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getTitleTracks() {
        List<Song> titleTracks = songRepository.findByIsTitleTrue();
        return titleTracks.stream()
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        List<Song> songs = songRepository.findSongsByMemberId(memberId);
        return songs.stream()
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongSummaryResponse> getSongsByLanguage(String language) {
        List<Song> songs = songRepository.findByLanguage(language);
        return songs.stream()
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SongSummaryResponse> searchSongs(String keyword, Pageable pageable) {
        Page<Song> songs = songRepository.searchSongs(keyword, pageable);
        return songs.map(songMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public Song findOrCreateSong(SongRequest request) {
        // Try to find an existing song with the same title and album
        if (request.getAlbumId() != null && request.getTitle() != null) {
            List<Song> existingSongs = songRepository.findByAlbumId(request.getAlbumId());
            for (Song song : existingSongs) {
                if (song.getTitle().equalsIgnoreCase(request.getTitle())) {
                    return song;
                }
            }
        }

        // If not found, create a new song
        return songRepository.save(songMapper.toEntity(request));
    }
}

package com.armycommunity.service.datainitialization;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.Era;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import com.armycommunity.model.member.MemberLineAssignment;
import com.armycommunity.model.song.Song;
import com.armycommunity.model.song.SongMember;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import com.armycommunity.repository.member.MemberLineAssignmentRepository;
import com.armycommunity.repository.member.MemberRepository;
import com.armycommunity.repository.song.MusicVideoRepository;
import com.armycommunity.repository.song.SongMemberRepository;
import com.armycommunity.repository.song.SongRepository;
import com.armycommunity.service.album.AlbumService;
import com.armycommunity.service.era.EraService;
import com.armycommunity.service.member.MemberService;
import com.armycommunity.service.musicvideo.MusicVideoService;
import com.armycommunity.service.song.SongService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of DataInitializationService for initializing the database with BTS data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationServiceImpl implements DataInitializationService{

    private final MemberService memberService;
    private final EraService eraService;
    private final AlbumService albumService;
    private final SongService songService;
    private final MusicVideoService musicVideoService;

    private final MemberRepository memberRepository;
    private final EraRepository eraRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final MusicVideoRepository musicVideoRepository;
    private final MemberLineAssignmentRepository memberLineAssignmentRepository;
    private final SongMemberRepository songMemberRepository;

    private final ObjectMapper objectMapper;

    /**
     * Initializes BTS members data
     */
    @Override
    @Transactional
    public void initializeMembers() {
        // Check if members are already initialized
        if (memberRepository.count() > 0) {
            log.info("Members already initialized, skipping...");
            return;
        }

        try {
            // Load member data from JSON file
            ClassPathResource resource = new ClassPathResource("data/members.json");
            List<Map<String, Object>> membersData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Process each member
            for (Map<String, Object> memberData : membersData) {
                // Create member request
                MemberRequest request = new MemberRequest();
                request.setStageName((String) memberData.get("stageName"));
                request.setRealName((String) memberData.get("realName"));
                request.setBirthday(LocalDate.parse((String) memberData.get("birthday")));
                request.setPosition((String) memberData.get("position"));

                // Create member
                Member member = memberService.findOrCreateMember(request);

                // Add member lines
                @SuppressWarnings("unchecked")
                Set<MemberLine> lines = (Set<MemberLine>) memberData.get("lines");
                for (MemberLine line : lines) {
                    MemberLineAssignment lineAssignment = new MemberLineAssignment();
                    lineAssignment.setMember(member);
                    lineAssignment.setLineType(line);
                    memberLineAssignmentRepository.save(lineAssignment);
                }
            }

            log.info("Members initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize members", e);
            throw new RuntimeException("Failed to initialize members", e);
        }
    }

    /**
     * Initializes BTS eras data
     */
    @Override
    @Transactional
    public void initializeEras() {
        // Check if eras are already initialized
        if (eraRepository.count() > 0) {
            log.info("Eras already initialized, skipping...");
            return;
        }

        try {
            // Load era data from JSON file
            ClassPathResource resource = new ClassPathResource("data/eras.json");
            List<Map<String, Object>> erasData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Process each era
            for (Map<String, Object> eraData : erasData) {
                // Create era request
                EraRequest request = new EraRequest();
                request.setName((String) eraData.get("name"));
                request.setStartDate(LocalDate.parse((String) eraData.get("startDate")));

                // Parse end date if available
                String endDateStr = (String) eraData.get("endDate");
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    request.setEndDate(LocalDate.parse(endDateStr));
                }

                request.setDescription((String) eraData.get("description"));

                // Create era
                eraService.createEra(request);
            }

            log.info("Eras initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize eras", e);
            throw new RuntimeException("Failed to initialize eras", e);
        }
    }

    /**
     * Initializes BTS albums data
     */
    @Override
    @Transactional
    public void initializeAlbums() {
        // Check if albums are already initialized
        if (albumRepository.count() > 0) {
            log.info("Albums already initialized, skipping...");
            return;
        }

        try {
            // Load album data from JSON file
            ClassPathResource resource = new ClassPathResource("data/albums.json");
            List<Map<String, Object>> albumsData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Process each album
            for (Map<String, Object> albumData : albumsData) {
                // Create album request
                AlbumRequest request = new AlbumRequest();
                request.setTitle((String) albumData.get("title"));
                request.setKoreanTitle((String) albumData.get("koreanTitle"));
                request.setAlbumType(AlbumType.valueOf((String) albumData.get("albumType")));
                request.setReleaseDate(LocalDate.parse((String) albumData.get("releaseDate")));
                request.setArtist((String) albumData.get("artist"));
                request.setDescription((String) albumData.get("description"));

                // Get era ID if available
                String eraName = (String) albumData.get("era");
                if (eraName != null && !eraName.isEmpty()) {
                    Era era = eraRepository.findByName(eraName)
                            .orElse(null);
                    if (era != null) {
                        request.setEraId(era.getId());
                    }
                }

                // Create album
                albumService.createAlbum(request);
            }

            log.info("Albums initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize albums", e);
            throw new RuntimeException("Failed to initialize albums", e);
        }
    }

    /**
     * Initializes BTS songs data
     */
    @Override
    @Transactional
    public void initializeSongs() {
        // Check if songs are already initialized
        if (songRepository.count() > 0) {
            log.info("Songs already initialized, skipping...");
            return;
        }

        try {
            // Load song data from JSON file
            ClassPathResource resource = new ClassPathResource("data/songs.json");
            List<Map<String, Object>> songsData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Process each song
            for (Map<String, Object> songData : songsData) {
                // Find the album
                String albumTitle = (String) songData.get("albumTitle");
                String artist = (String) songData.get("artist");

                Album album = null;
                if (albumTitle != null && !albumTitle.isEmpty()) {
                    album = albumRepository.findByTitleAndArtist(albumTitle, artist)
                            .orElse(null);
                }

                // Create song request
                SongRequest request = new SongRequest();
                request.setTitle((String) songData.get("title"));
                request.setKoreanTitle((String) songData.get("koreanTitle"));
                request.setDuration((Integer) songData.get("duration"));
                request.setTrackNumber((Integer) songData.get("trackNumber"));
                request.setIsTitle((Boolean) songData.get("isTitle"));
                request.setLyrics((String) songData.get("lyrics"));
                request.setLanguage((String) songData.get("language"));

                // Set album ID if available
                if (album != null) {
                    request.setAlbumId(album.getId());
                }

                // Create song
                Song song = songService.findOrCreateSong(request);

                // Add member associations
                @SuppressWarnings("unchecked")
                List<String> memberNames = (List<String>) songData.get("members");
                if (memberNames != null) {
                    for (String stageName : memberNames) {
                        Member member = memberRepository.findByStageName(stageName)
                                .orElse(null);

                        if (member != null) {
                            SongMember songMember = new SongMember();
                            songMember.setSong(song);
                            songMember.setMember(member);
                            songMemberRepository.save(songMember);
                        }
                    }
                }
            }

            log.info("Songs initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize songs", e);
            throw new RuntimeException("Failed to initialize songs", e);
        }
    }

    /**
     * Initializes BTS music videos data
     */
    @Override
    @Transactional
    public void initializeMusicVideos() {
        // Check if music videos are already initialized
        if (musicVideoRepository.count() > 0) {
            log.info("Music videos already initialized, skipping...");
            return;
        }

        try {
            // Load music video data from JSON file
            ClassPathResource resource = new ClassPathResource("data/music_videos.json");
            List<Map<String, Object>> musicVideosData = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // Process each music video
            for (Map<String, Object> mvData : musicVideosData) {
                // Find the song
                String songTitle = (String) mvData.get("songTitle");

                List<Song> songs = songRepository.findByTitleContainingIgnoreCase(songTitle);
                if (songs.isEmpty()) {
                    log.warn("Song not found for music video: {}", songTitle);
                    continue;
                }

                Song song = songs.get(0);

                // Create music video request
                MusicVideoRequest request = new MusicVideoRequest();
                request.setSongId(song.getId());
                request.setTitle((String) mvData.get("title"));
                request.setReleaseDate(LocalDate.parse((String) mvData.get("releaseDate")));
                request.setVideoType((String) mvData.get("videoType"));
                request.setUrl((String) mvData.get("url"));

                // Create music video
                musicVideoService.createMusicVideo(request);
            }

            log.info("Music videos initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize music videos", e);
            throw new RuntimeException("Failed to initialize music videos", e);
        }
    }

    /**
     * Checks if core data is already initialized
     * @return true if data is initialized, false otherwise
     */
    @Override
    public boolean isDataInitialized() {
        long memberCount = memberRepository.count();
        long eraCount = eraRepository.count();
        long albumCount = albumRepository.count();
        long songCount = songRepository.count();

        // Consider data initialized if we have members, eras, albums and songs
        return memberCount > 0 && eraCount > 0 && albumCount > 0 && songCount > 0;
    }
}

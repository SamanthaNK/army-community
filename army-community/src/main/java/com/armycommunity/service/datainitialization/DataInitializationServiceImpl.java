package com.armycommunity.service.datainitialization;

import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.AlbumType;
import com.armycommunity.model.album.Era;
import com.armycommunity.model.album.MemberAlbum;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import com.armycommunity.model.member.MemberLineAssignment;
import com.armycommunity.model.song.MusicVideo;
import com.armycommunity.model.song.Song;
import com.armycommunity.model.song.SongMember;
import com.armycommunity.repository.album.AlbumRepository;
import com.armycommunity.repository.album.EraRepository;
import com.armycommunity.repository.album.MemberAlbumRepository;
import com.armycommunity.repository.member.MemberLineAssignmentRepository;
import com.armycommunity.repository.member.MemberRepository;
import com.armycommunity.repository.song.MusicVideoRepository;
import com.armycommunity.repository.song.SongMemberRepository;
import com.armycommunity.repository.song.SongRepository;
import com.armycommunity.service.setting.SettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializationServiceImpl implements CommandLineRunner {

    private final SettingService settingService;

    private final MemberRepository memberRepository;
    private final EraRepository eraRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final MusicVideoRepository musicVideoRepository;
    private final MemberLineAssignmentRepository memberLineAssignmentRepository;
    private final SongMemberRepository songMemberRepository;
    private final MemberAlbumRepository memberAlbumRepository;

    private final ObjectMapper objectMapper;

    private static final String DATA_PATH = "data/";
    private static final String INITIALIZATION_FLAG_KEY = "data_initialized";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (isDataInitialized()) {
            log.info("Data initialization skipped - data already exists");
            return;
        }

        log.info("Starting BTS data initialization...");
        long startTime = System.currentTimeMillis();

        try {
            initializeMembers();
            initializeEras();
            initializeAlbums();
            initializeSongs();
            initializeMusicVideos();

            long endTime = System.currentTimeMillis();
            log.info("Data initialization completed successfully in {} ms", endTime - startTime);

        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize data", e);
        }
    }

    private boolean isDataInitialized() {
        // Check if core data exists
        boolean hasMembers = memberRepository.count() > 0;
        boolean hasEras = eraRepository.count() > 0;
        boolean hasAlbums = albumRepository.count() > 0;

        return hasMembers && hasEras && hasAlbums;
    }

    @Transactional
    public void initializeMembers() {
        if (memberRepository.count() > 0) {
            log.info("Members already initialized, skipping...");
            return;
        }

        log.info("Initializing BTS members...");

        try {
            List<MemberData> memberDataList = loadJsonData("members.json", new TypeReference<List<MemberData>>() {
            });

            for (MemberData memberData : memberDataList) {
                Member member = createMemberFromData(memberData);
                member = memberRepository.save(member);

                // Save line assignments
                for (MemberLine lineType : memberData.getLineTypes()) {
                    MemberLineAssignment assignment = MemberLineAssignment.builder()
                            .member(member)
                            .lineType(lineType)
                            .build();
                    memberLineAssignmentRepository.save(assignment);
                }

                log.debug("Initialized member: {} with {} line assignments",
                        member.getStageName(), memberData.getLineTypes().size());
            }

            long memberCount = memberRepository.count();
            log.info("Successfully initialized {} BTS members", memberCount);

        } catch (Exception e) {
            log.error("Failed to initialize members: {}", e.getMessage(), e);
            throw new RuntimeException("Member initialization failed", e);
        }
    }

    @Transactional
    public void initializeEras() {
        if (eraRepository.count() > 0) {
            log.info("Eras already initialized, skipping...");
            return;
        }

        log.info("Initializing BTS eras...");

        try {
            List<EraData> eraDataList = loadJsonData("eras.json", new TypeReference<List<EraData>>() {});

            for (EraData eraData : eraDataList) {
                Era era = Era.builder()
                        .name(eraData.getName())
                        .startDate(eraData.getStartDate())
                        .endDate(eraData.getEndDate())
                        .description(eraData.getDescription())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                eraRepository.save(era);
                log.debug("Initialized era: {}", era.getName());
            }

            long eraCount = eraRepository.count();
            log.info("Successfully initialized {} BTS eras", eraCount);

        } catch (Exception e) {
            log.error("Failed to initialize eras: {}", e.getMessage(), e);
            throw new RuntimeException("Era initialization failed", e);
        }
    }

    @Transactional
    public void initializeAlbums() {
        if (albumRepository.count() > 0) {
            log.info("Albums already initialized, skipping...");
            return;
        }

        log.info("Initializing BTS albums...");

        try {
            List<AlbumData> albumDataList = loadJsonData("albums.json", new TypeReference<List<AlbumData>>() {});
            Map<String, Era> eraMap = eraRepository.findAll().stream()
                    .collect(Collectors.toMap(Era::getName, era -> era));

            int processedCount = 0;

            for (AlbumData albumData : albumDataList) {
                Era era = eraMap.get(albumData.getEraName());
                if (era == null) {
                    log.warn("Era '{}' not found for album '{}'", albumData.getEraName(), albumData.getTitle());
                    continue;
                }

                Album album = Album.builder()
                        .title(albumData.getTitle())
                        .koreanTitle(albumData.getKoreanTitle())
                        .albumType(albumData.getAlbumType())
                        .releaseDate(albumData.getReleaseDate())
                        .era(era)
                        .artist(albumData.getArtist())
                        .isOfficial(albumData.getIsOfficial())
                        .coverImagePath(albumData.getCoverImagePath())
                        .description(albumData.getDescription())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                album = albumRepository.save(album);

                // Handle member albums if specified
                if (albumData.getMemberIds() != null && !albumData.getMemberIds().isEmpty()) {
                    saveMemberAlbumAssociations(album, albumData.getMemberIds());
                }

                processedCount++;
                log.debug("Initialized album: {} ({})", album.getTitle(), album.getAlbumType());
            }

            log.info("Successfully initialized {} BTS albums", processedCount);

        } catch (Exception e) {
            log.error("Failed to initialize albums: {}", e.getMessage(), e);
            throw new RuntimeException("Album initialization failed", e);
        }
    }

    @Transactional
    public void initializeSongs() {
        if (songRepository.count() > 0) {
            log.info("Songs already initialized, skipping...");
            return;
        }

        log.info("Initializing BTS songs...");

        try {
            List<SongData> songDataList = loadJsonData("songs.json", new TypeReference<List<SongData>>() {});
            Map<String, Album> albumMap = albumRepository.findAll().stream()
                    .collect(Collectors.toMap(Album::getTitle, album -> album));
            Map<String, Member> memberMap = memberRepository.findAll().stream()
                    .collect(Collectors.toMap(Member::getStageName, member -> member));

            int processedCount = 0;

            for (SongData songData : songDataList) {
                Album album = albumMap.get(songData.getAlbumTitle());
                if (album == null) {
                    log.warn("Album '{}' not found for song '{}'", songData.getAlbumTitle(), songData.getTitle());
                    continue;
                }

                Song song = Song.builder()
                        .title(songData.getTitle())
                        .album(album)
                        .koreanTitle(songData.getKoreanTitle())
                        .duration(songData.getDuration())
                        .trackNumber(songData.getTrackNumber())
                        .isTitle(songData.getIsTitle())
                        .doolsetUrl(songData.getDoolsetUrl())
                        .geniusUrl(songData.getGeniusUrl())
                        .language(songData.getLanguage())
                        .featuringArtist(new String[]{songData.getFeaturingArtist()})
                        .releaseDate(songData.getReleaseDate())
                        .releaseType(songData.getReleaseType())
                        .artist(songData.getArtist())
                        .url(songData.getUrl())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                song = songRepository.save(song);

                // Handle song member associations
                if (songData.getMemberNames() != null && !songData.getMemberNames().isEmpty()) {
                    saveSongMemberAssociations(song, songData.getMemberNames(), memberMap);
                }

                processedCount++;
                if (processedCount % 50 == 0) {
                    log.debug("Processed {} songs...", processedCount);
                }
            }

            log.info("Successfully initialized {} BTS songs", processedCount);

        } catch (Exception e) {
            log.error("Failed to initialize songs: {}", e.getMessage(), e);
            throw new RuntimeException("Song initialization failed", e);
        }
    }

    @Transactional
    public void initializeMusicVideos() {
        if (musicVideoRepository.count() > 0) {
            log.info("Music videos already initialized, skipping...");
            return;
        }

        log.info("Initializing BTS music videos...");

        try {
            List<MusicVideoData> videoDataList = loadJsonData("music_videos.json", new TypeReference<List<MusicVideoData>>() {});
            Map<String, Song> songMap = songRepository.findAll().stream()
                    .collect(Collectors.toMap(Song::getTitle, song -> song));

            int processedCount = 0;

            for (MusicVideoData videoData : videoDataList) {
                Song song = songMap.get(videoData.getSongTitle());
                if (song == null) {
                    log.warn("Song '{}' not found for music video '{}'", videoData.getSongTitle(), videoData.getTitle());
                    continue;
                }

                MusicVideo musicVideo = MusicVideo.builder()
                        .song(song)
                        .title(videoData.getTitle())
                        .releaseDate(videoData.getReleaseDate())
                        .videoType(videoData.getVideoType())
                        .url(videoData.getUrl())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                musicVideoRepository.save(musicVideo);
                processedCount++;

                log.debug("Initialized music video: {} for song: {}", videoData.getTitle(), song.getTitle());
            }

            log.info("Successfully initialized {} BTS music videos", processedCount);

        } catch (Exception e) {
            log.error("Failed to initialize music videos: {}", e.getMessage(), e);
            throw new RuntimeException("Music video initialization failed", e);
        }
    }

    private Member createMemberFromData(MemberData memberData) {
        return Member.builder()
                .stageName(memberData.getStageName())
                .realName(memberData.getRealName())
                .birthday(memberData.getBirthday())
                .position(memberData.getPosition())
                .profileImagePath(memberData.getProfileImagePath())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void saveMemberAlbumAssociations(Album album, List<Long> memberIds) {
        List<Member> members = memberRepository.findAllById(memberIds);
        for (Member member : members) {
            MemberAlbum memberAlbum = MemberAlbum.builder()
                    .member(member)
                    .album(album)
                    .build();
            memberAlbumRepository.save(memberAlbum);
        }
    }

    private void saveSongMemberAssociations(Song song, List<String> memberNames, Map<String, Member> memberMap) {
        for (String memberName : memberNames) {
            Member member = memberMap.get(memberName);
            if (member != null) {
                SongMember songMember = SongMember.builder()
                        .song(song)
                        .member(member)
                        .build();
                songMemberRepository.save(songMember);
            } else {
                log.warn("Member '{}' not found for song '{}'", memberName, song.getTitle());
            }
        }
    }

    private <T> T loadJsonData(String filename, TypeReference<T> typeReference) throws IOException {
        String fullPath = DATA_PATH + filename;

        try (InputStream inputStream = new ClassPathResource(fullPath).getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            log.error("Failed to load JSON data from {}: {}", fullPath, e.getMessage());
            throw new IOException("Could not load data from " + fullPath, e);
        }
    }

    // Data classes for JSON deserialization
    @Setter
    @Getter
    private static class MemberData {
        private String stageName;
        private String realName;
        private LocalDate birthday;
        private String position;
        private String profileImagePath;
        private List<MemberLine> lineTypes;

    }

    @Setter
    @Getter
    private static class EraData {
        private String name;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
    }

    @Setter
    @Getter
    private static class AlbumData {
        private String title;
        private String koreanTitle;
        private AlbumType albumType;
        private LocalDate releaseDate;
        private String eraName;
        private String artist;
        private Boolean isOfficial;
        private String coverImagePath;
        private String description;
        private List<Long> memberIds;
    }

    @Setter
    @Getter
    private static class SongData {
        private String title;
        private String koreanTitle;
        private Integer duration;
        private Integer trackNumber;
        private Boolean isTitle;
        private String doolsetUrl;
        private String geniusUrl;
        private String language;
        private String featuringArtist;
        private LocalDate releaseDate;
        private String releaseType;
        private String artist;
        private String url;
        private String albumTitle;
        private List<String> memberNames;
    }

    @Setter
    @Getter
    private static class MusicVideoData {
        private String title;
        private LocalDate releaseDate;
        private String videoType;
        private String url;
        private String songTitle;
    }
}

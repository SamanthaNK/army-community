package com.armycommunity.mapper;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.MusicVideoResponse;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.song.MusicVideo;
import com.armycommunity.model.song.Song;
import com.armycommunity.model.song.SongMember;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SongMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "songMembers", ignore = true)
    @Mapping(target = "musicVideos", ignore = true)
    Song toEntity(SongRequest request);

    @Mapping(target = "album", source = "album", qualifiedByName = "albumToAlbumSummary")
    @Mapping(target = "members", source = "songMembers", qualifiedByName = "songMembersToMemberResponses")
    @Mapping(target = "musicVideos", source = "musicVideos", qualifiedByName = "musicVideosToMusicVideoResponses")
    @Mapping(target = "formattedDuration", source = "duration", qualifiedByName = "formatDuration")
    @Mapping(target = "isBTSOfficial", source = "artist", qualifiedByName = "checkIfBTSOfficial")
    @Mapping(target = "hasFeatures", source = ".", qualifiedByName = "checkIfHasFeatures")
    SongDetailResponse toDetailResponse(Song song);

    @Mapping(target = "isTitle", source = "isTitle", defaultValue = "false")
    SongSummaryResponse toSummaryResponse(Song song);

    List<SongSummaryResponse> toSummaryResponseList(List<Song> songs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "songMembers", ignore = true)
    @Mapping(target = "musicVideos", ignore = true)
    void updateSongFromRequest(SongRequest request, @MappingTarget Song song);

    @Named("albumToAlbumSummary")
    default AlbumSummaryResponse albumToAlbumSummary(Album album) {
        if (album == null) {
            return null;
        }
        return AlbumSummaryResponse.builder()
                .id(album.getId())
                .title(album.getTitle())
                .koreanTitle(album.getKoreanTitle())
                .albumType(album.getAlbumType())
                .releaseDate(album.getReleaseDate())
                .artist(album.getArtist())
                .coverImagePath(album.getCoverImagePath())
                .songCount(album.getSongs() != null ? album.getSongs().size() : 0)
                .collectionCount(album.getCollections() != null ? album.getCollections().size() : 0)
                .build();
    }

    @Named("songMembersToMemberResponses")
    default List<MemberSummaryResponse> songMembersToMemberResponses(Set<SongMember> songMembers) {
        if (songMembers == null || songMembers.isEmpty()) {
            return List.of();
        }
        return songMembers.stream()
                .map(songMember -> {
                    Member member = songMember.getMember();
                    return MemberSummaryResponse.builder()
                            .id(member.getId())
                            .stageName(member.getStageName())
                            .profileImagePath(member.getProfileImagePath())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Named("musicVideosToMusicVideoResponses")
    default List<MusicVideoResponse> musicVideosToMusicVideoResponses(Set<MusicVideo> musicVideos) {
        if (musicVideos == null || musicVideos.isEmpty()) {
            return List.of();
        }
        return musicVideos.stream()
                .map(mv -> MusicVideoResponse.builder()
                        .id(mv.getId())
                        .title(mv.getTitle())
                        .releaseDate(mv.getReleaseDate())
                        .videoType(mv.getVideoType())
                        .url(mv.getUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("formatDuration")
    default String formatDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            return "0:00";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Named("checkIfBTSOfficial")
    default Boolean checkIfBTSOfficial(String artist) {
        if (artist == null) {
            return false;
        }
        String lowerArtist = artist.toLowerCase();
        return lowerArtist.equals("bts") ||
                lowerArtist.equals("방탄소년단") ||
                lowerArtist.startsWith("bts ");
    }

    @Named("checkIfHasFeatures")
    default Boolean checkIfHasFeatures(Song song) {
        if (song == null || song.getSongMembers() == null || song.getSongMembers().isEmpty()) {
            return false;
        }

        String artist = song.getArtist();
        if (artist == null) {
            return false;
        }

        String lowerArtist = artist.toLowerCase();
        boolean isBTSMain = lowerArtist.equals("bts") ||
                lowerArtist.equals("방탄소년단") ||
                lowerArtist.startsWith("bts ");

        // If BTS is not the main artist but BTS members are involved, it's a feature
        return !isBTSMain && !song.getSongMembers().isEmpty();
    }

}

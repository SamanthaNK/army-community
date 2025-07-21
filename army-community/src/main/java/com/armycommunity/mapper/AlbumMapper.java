package com.armycommunity.mapper;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.Era;
import com.armycommunity.model.album.MemberAlbum;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.song.Song;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AlbumMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "memberAlbums", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "era", ignore = true)
    @Mapping(target = "coverImagePath", ignore = true)
    Album toEntity(AlbumRequest albumRequest);

    @Mapping(target = "era", source = "era", qualifiedByName = "eraToEraResponse")
    @Mapping(target = "songs", source = "songs", qualifiedByName = "songsToSongResponses")
    @Mapping(target = "members", source = "memberAlbums", qualifiedByName = "memberAlbumsToMemberResponses")
    @Mapping(target = "collectionCount", expression = "java(album.getCollections().size())")
    AlbumDetailResponse toDetailResponse(Album album);

    @Mapping(target = "songCount", expression = "java(album.getSongs().size())")
    @Mapping(target = "collectionCount", expression = "java(album.getCollections().size())")
    AlbumSummaryResponse toSummaryResponse(Album album);

    List<AlbumSummaryResponse> toSummaryResponseList(List<Album> albums);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "memberAlbums", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "era", ignore = true)
    @Mapping(target = "coverImagePath", ignore = true)
    void updateAlbumFromRequest(AlbumRequest request, @MappingTarget Album album);

    @Named("eraToEraResponse")
    default EraSummaryResponse eraToEraResponse(Era era) {
        if (era == null) {
            return null;
        }
        return EraSummaryResponse.builder()
                .id(era.getId())
                .name(era.getName())
                .startDate(era.getStartDate())
                .endDate(era.getEndDate())
                .description(era.getDescription())
                .build();
    }

    @Named("songsToSongResponses")
    default List<SongSummaryResponse> songsToSongResponses(Set<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return List.of();
        }
        return songs.stream()
                .map(song -> SongSummaryResponse.builder()
                        .id(song.getId())
                        .title(song.getTitle())
                        .koreanTitle(song.getKoreanTitle())
                        .duration(song.getDuration())
                        .trackNumber(song.getTrackNumber())
                        .isTitle(song.getIsTitle())
                        .language(song.getLanguage())
                        .build())
                .collect(Collectors.toList());
    }

    @Named("memberAlbumsToMemberResponses")
    default List<MemberSummaryResponse> memberAlbumsToMemberResponses(Set<MemberAlbum> memberAlbums) {
        if (memberAlbums == null || memberAlbums.isEmpty()) {
            return List.of();
        }
        return memberAlbums.stream()
                .map(memberAlbum -> {
                    Member member = memberAlbum.getMember();
                    return MemberSummaryResponse.builder()
                            .id(member.getId())
                            .stageName(member.getStageName())
                            .profileImagePath(member.getProfileImagePath())
                            .build();
                })
                .collect(Collectors.toList());
    }

}

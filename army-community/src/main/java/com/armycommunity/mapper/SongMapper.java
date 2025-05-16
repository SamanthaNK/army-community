package com.armycommunity.mapper;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.song.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SongMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Song toEntity(SongRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(SongRequest request, @MappingTarget Song song);

    @Mapping(target = "albumId", source = "album.id")
    @Mapping(target = "albumTitle", source = "album.title")
    SongSummaryResponse toSummaryResponse(Song song);

    @Mapping(target = "albumId", source = "album.id")
    @Mapping(target = "albumTitle", source = "album.title")
    @Mapping(target = "albumCoverImagePath", source = "album.coverImagePath")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "musicVideos", ignore = true)
    SongDetailResponse toDetailResponse(Song song);
}

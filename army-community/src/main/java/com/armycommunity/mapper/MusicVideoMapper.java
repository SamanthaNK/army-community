package com.armycommunity.mapper;

import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.response.song.MusicVideoResponse;
import com.armycommunity.model.song.MusicVideo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MusicVideoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MusicVideo toEntity(MusicVideoRequest request);

    @Mapping(target = "songId", source = "song.id")
    @Mapping(target = "songTitle", source = "song.title")
    @Mapping(target = "albumId", source = "song.album.id")
    @Mapping(target = "albumTitle", source = "song.album.title")
    MusicVideoResponse toResponse(MusicVideo musicVideo);

    List<MusicVideoResponse> toResponseList(List<MusicVideo> musicVideos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "song", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMusicVideoFromRequest(MusicVideoRequest request, @MappingTarget MusicVideo musicVideo);


}

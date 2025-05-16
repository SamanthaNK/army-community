package com.armycommunity.mapper;

import com.armycommunity.dto.request.album.AlbumRequest;
import com.armycommunity.dto.response.album.AlbumDetailResponse;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.model.album.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AlbumMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "era", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "coverImagePath", ignore = true)
    Album toEntity(AlbumRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "era", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "coverImagePath", ignore = true)
    void updateEntity(AlbumRequest request, @MappingTarget Album album);

    @Mapping(target = "eraId", source = "era.id")
    @Mapping(target = "eraName", source = "era.name")
    @Mapping(target = "songCount", ignore = true)
    @Mapping(target = "totalDuration", ignore = true)
    @Mapping(target = "collectionCount", ignore = true)
    AlbumSummaryResponse toSummaryResponse(Album album);

    @Mapping(target = "eraId", source = "era.id")
    @Mapping(target = "eraName", source = "era.name")
    @Mapping(target = "songs", ignore = true)
    @Mapping(target = "collectionCount", ignore = true)
    @Mapping(target = "memberCredits", ignore = true)
    AlbumDetailResponse toDetailResponse(Album album);
}

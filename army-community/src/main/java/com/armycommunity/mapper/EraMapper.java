package com.armycommunity.mapper;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.model.album.Album;
import com.armycommunity.model.album.Era;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper interface for Era entity transformations.
 * Handles mapping between Era entities and their corresponding DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "albums", ignore = true)
    Era toEntity(EraRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "albums", ignore = true)
    void updateEraFromRequest(EraRequest request, @MappingTarget Era era);

    @Mapping(target = "albums", source = "albums", qualifiedByName = "albumsToAlbumSummaryResponses")
    @Mapping(target = "albumCount", expression = "java(era.getAlbums().size())")
    @Mapping(target = "isCurrent", expression = "java(isCurrentEra(era))")
    EraDetailResponse toDetailResponse(Era era);

    EraSummaryResponse toSummaryResponse(Era era);

    List<EraSummaryResponse> toSummaryResponseList(List<Era> eras);

    List<EraDetailResponse> toDetailResponseList(List<Era> eras);

    @Named("albumsToAlbumSummaryResponses")
    default List<AlbumSummaryResponse> albumsToAlbumSummaryResponses(Set<Album> albums) {
        if (albums == null || albums.isEmpty()) {
            return List.of();
        }
        return albums.stream()
                .map(album -> AlbumSummaryResponse.builder()
                        .id(album.getId())
                        .title(album.getTitle())
                        .koreanTitle(album.getKoreanTitle())
                        .albumType(album.getAlbumType())
                        .releaseDate(album.getReleaseDate())
                        .artist(album.getArtist())
                        .coverImagePath(album.getCoverImagePath())
                        .songCount(album.getSongs().size())
                        .collectionCount(album.getCollections().size())
                        .build())
                .sorted((a1, a2) -> a1.getReleaseDate().compareTo(a2.getReleaseDate()))
                .collect(Collectors.toList());
    }

    default Boolean isCurrentEra(Era era) {
        if (era == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate startDate = era.getStartDate();
        LocalDate endDate = era.getEndDate();

        // Era is current if the start date is before or equal to now, and the end date is null (ongoing) or after or equal to now
        return (startDate == null || !startDate.isAfter(now)) &&
                (endDate == null || !endDate.isBefore(now));
    }
}

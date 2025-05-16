package com.armycommunity.mapper;

import com.armycommunity.dto.request.album.EraRequest;
import com.armycommunity.dto.response.album.EraDetailResponse;
import com.armycommunity.dto.response.album.EraSummaryResponse;
import com.armycommunity.model.album.Era;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EraMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Era toEntity(EraRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(EraRequest request, @MappingTarget Era era);

    @Mapping(target = "albumCount", ignore = true)
    @Mapping(target = "isCurrent", ignore = true)
    EraSummaryResponse toSummaryResponse(Era era);

    @Mapping(target = "albums", ignore = true)
    @Mapping(target = "isCurrent", ignore = true)
    EraDetailResponse toDetailResponse(Era era);
}

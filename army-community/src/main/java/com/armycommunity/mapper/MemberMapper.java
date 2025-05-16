package com.armycommunity.mapper;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.model.member.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileImagePath", ignore = true)
    Member toEntity(MemberRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "profileImagePath", ignore = true)
    void updateEntity(MemberRequest request, @MappingTarget Member member);

    MemberSummaryResponse toSummaryResponse(Member member);

    @Mapping(target = "lineTypes", ignore = true)
    @Mapping(target = "songCount", ignore = true)
    @Mapping(target = "albums", ignore = true)
    @Mapping(target = "recentSongs", ignore = true)
    MemberDetailResponse toDetailResponse(Member member);
}

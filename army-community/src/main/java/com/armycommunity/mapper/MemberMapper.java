package com.armycommunity.mapper;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import com.armycommunity.model.member.MemberLineAssignment;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profileImagePath", ignore = true)
    Member toEntity(MemberRequest request);

    MemberDetailResponse toDetailResponse(Member member);

    MemberSummaryResponse toSummaryResponse(Member member);

    List<MemberSummaryResponse> toSummaryResponseList(List<Member> members);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "memberLineAssignments", ignore = true)
    @Mapping(target = "songMembers", ignore = true)
    @Mapping(target = "memberAlbums", ignore = true)
    void updateMemberFromRequest(MemberRequest request, @MappingTarget Member member);

    @Named("memberLineAssignmentsToLineTypes")
    default Set<MemberLine> memberLineAssignmentsToLineTypes(Set<MemberLineAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptySet();
        }
        return assignments.stream()
                .map(MemberLineAssignment::getLineType)
                .collect(Collectors.toSet());
    }
}

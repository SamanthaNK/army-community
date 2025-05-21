package com.armycommunity.service.member;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MemberService {
    MemberDetailResponse createMember(MemberRequest request, MultipartFile profileImage);

    MemberDetailResponse getMemberById(Long memberId);

    MemberDetailResponse getMemberByStageName(String stageName);

    MemberDetailResponse updateMember(Long memberId, MemberRequest request, MultipartFile profileImage);

    void deleteMember(Long memberId);

    List<MemberSummaryResponse> getAllMembers();

    List<MemberSummaryResponse> getMembersByLine(MemberLine lineType);

    List<MemberSummaryResponse> getMembersBySong(Long songId);

    Member findOrCreateMember(MemberRequest request);
}

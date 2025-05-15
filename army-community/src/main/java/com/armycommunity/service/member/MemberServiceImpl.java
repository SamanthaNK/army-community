package com.armycommunity.service.member;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.model.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    @Override
    public MemberDetailResponse createMember(MemberRequest request, MultipartFile profileImage) {
        // TODO: implement
        return null;
    }

    @Override
    public MemberDetailResponse getMemberById(Long memberId) {
        // TODO: implement
        return null;
    }

    @Override
    public MemberDetailResponse getMemberByStageName(String stageName) {
        // TODO: implement
        return null;
    }

    @Override
    public MemberDetailResponse updateMember(Long memberId, MemberRequest request, MultipartFile profileImage) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteMember(Long memberId) {
        // TODO: implement
    }

    @Override
    public List<MemberSummaryResponse> getAllMembers() {
        // TODO: implement
        return null;
    }

    @Override
    public List<MemberSummaryResponse> getMembersByLine(String lineType) {
        // TODO: implement
        return null;
    }

    @Override
    public List<MemberSummaryResponse> getMembersBySong(Long songId) {
        // TODO: implement
        return null;
    }

    @Override
    public Member findOrCreateMember(MemberRequest request) {
        // TODO: implement
        return null;
    }
}

package com.armycommunity.service.member;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.exception.DuplicateResourceException;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.MemberMapper;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import com.armycommunity.model.member.MemberLineAssignment;
import com.armycommunity.repository.album.MemberAlbumRepository;
import com.armycommunity.repository.member.MemberLineAssignmentRepository;
import com.armycommunity.repository.member.MemberRepository;
import com.armycommunity.repository.song.SongMemberRepository;
import com.armycommunity.repository.song.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of MemberService interface for managing BTS members
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberLineAssignmentRepository memberLineAssignmentRepository;
    private final SongRepository songRepository;
    private final SongMemberRepository songMemberRepository;
    private final MemberMapper memberMapper;
    private final MemberAlbumRepository memberAlbumRepository;


    @Override
    @Transactional
    public MemberDetailResponse createMember(MemberRequest request) {
        log.info("Creating member with stage name: {}", request.getStageName());

        // Check for duplicate member by stage name
        Optional<Member> existingMember = memberRepository.findByStageName(request.getStageName());
        if (existingMember.isPresent()) {
            throw new DuplicateResourceException(
                    "Member with stage name '" + request.getStageName() + "' already exists");
        }

        Member member = memberMapper.toEntity(request);

        // Handle the profile image path if provided
        if (request.getProfileImagePath() != null) {
            member.setProfileImagePath(request.getProfileImagePath());
        }

        Member savedMember = memberRepository.save(member);
        log.info("Member created with id: {}", savedMember.getId());

        // Handle line type assignments
        if (request.getLineTypes() != null && !request.getLineTypes().isEmpty()) {
            Set<MemberLineAssignment> lineAssignments = new HashSet<>();

            for (MemberLine lineType : request.getLineTypes()) {
                MemberLineAssignment assignment = new MemberLineAssignment();
                assignment.setMember(savedMember);
                assignment.setLineType(lineType);
                lineAssignments.add(assignment);

                log.debug("Associated member {} with line type: {}", savedMember.getStageName(), lineType);
            }

            memberLineAssignmentRepository.saveAll(lineAssignments);
            savedMember.setLineAssignments(lineAssignments);
        }

        return memberMapper.toDetailResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberById(Long memberId) {
        log.debug("Fetching member with ID: {}", memberId);
        return memberRepository.findById(memberId)
                .map(memberMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberByStageName(String stageName) {
        log.debug("Fetching member with stage name: {}", stageName);
        return memberRepository.findByStageName(stageName)
                .map(memberMapper::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with stage name: " + stageName));
    }

    @Override
    @Transactional
    public MemberDetailResponse updateMember(Long memberId, MemberRequest request) {
        log.info("Updating member with ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        // Check for duplicate stage name if changed
        if (!member.getStageName().equals(request.getStageName())) {
            Optional<Member> existingMember = memberRepository.findByStageName(request.getStageName());
            if (existingMember.isPresent() && !existingMember.get().getId().equals(memberId)) {
                throw new DuplicateResourceException(
                        "Member with stage name '" + request.getStageName() + "' already exists");
            }
        }

        memberMapper.updateMemberFromRequest(request, member);

        // Handle the profile image path if provided
        if (request.getProfileImagePath() != null) {
            member.setProfileImagePath(request.getProfileImagePath());
        }

        // Update line type assignments
        if (request.getLineTypes() != null) {
            // Remove existing assignments
            memberLineAssignmentRepository.deleteByMemberId(memberId);

            // Add new assignments
            if (!request.getLineTypes().isEmpty()) {
                Set<MemberLineAssignment> lineAssignments = new HashSet<>();

                for (MemberLine lineType : request.getLineTypes()) {
                    MemberLineAssignment assignment = new MemberLineAssignment();
                    assignment.setMember(member);
                    assignment.setLineType(lineType);
                    lineAssignments.add(assignment);
                }

                memberLineAssignmentRepository.saveAll(lineAssignments);
                member.setLineAssignments(lineAssignments);
                log.debug("Updated line type assignments for member: {}", member.getStageName());
            }
        }

        Member updatedMember = memberRepository.save(member);

        return memberMapper.toDetailResponse(updatedMember);
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        log.info("Deleting member with ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));

        String memberStageName = member.getStageName();

        // Delete line assignments first
        memberLineAssignmentRepository.deleteByMemberId(memberId);
        log.debug("Deleted line assignments for member: {}", memberStageName);

        // Delete song member associations
        songMemberRepository.deleteByMemberId(memberId);
        log.debug("Deleted song associations for member: {}", memberStageName);

        // Delete member album associations
        memberAlbumRepository.deleteByMemberId(memberId);
        log.debug("Deleted album associations for member: {}", memberStageName);

        // Delete the member
        memberRepository.delete(member);
        log.info("Member deleted successfully: {}", memberStageName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getAllMembers() {
        log.debug("Fetching all members");
        List<Member> members = memberRepository.findAll();
        return memberMapper.toSummaryResponseList(members);
    }

    @Override
    @Transactional(readOnly =true)
    public List<MemberSummaryResponse> getMembersByLine(MemberLine lineType) {
        log.debug("Fetching members with line type: {}", lineType);
        List<Member> members = memberRepository.findByLineType(lineType);
        return memberMapper.toSummaryResponseList(members);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getMembersBySong(Long songId) {
        log.debug("Fetching members for song with ID: {}", songId);
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song not found with id: " + songId);
        }

        List<Member> members = memberRepository.findBySongId(songId);
        return memberMapper.toSummaryResponseList(members);
    }

    @Override
    @Transactional
    public Member findOrCreateMember(MemberRequest request) {
        log.debug("Finding or creating member: {}", request.getStageName());

        // Try to find an existing member by stage name
        Optional<Member> existingMember = memberRepository.findByStageName(request.getStageName());

        if (existingMember.isPresent()) {
            log.debug("Found existing member: {}", request.getStageName());
            return existingMember.get();
        }

        // Create a new member if not found
        log.debug("Creating new member: {}", request.getStageName());
        Member member = memberMapper.toEntity(request);

        // Handle the profile image path if provided
        if (request.getProfileImagePath() != null) {
            member.setProfileImagePath(request.getProfileImagePath());
        }

        Member savedMember = memberRepository.save(member);

        // Handle line type assignments for new member
        if (request.getLineTypes() != null && !request.getLineTypes().isEmpty()) {
            Set<MemberLineAssignment> lineAssignments = new HashSet<>();

            for (MemberLine lineType : request.getLineTypes()) {
                MemberLineAssignment assignment = new MemberLineAssignment();
                assignment.setMember(savedMember);
                assignment.setLineType(lineType);
                lineAssignments.add(assignment);
            }

            memberLineAssignmentRepository.saveAll(lineAssignments);
            savedMember.setLineAssignments(lineAssignments);
            log.debug("Added line type assignments for new member: {}", savedMember.getStageName());
        }

        log.info("Member found or created successfully: {}", savedMember.getStageName());
        return savedMember;
    }
}

package com.armycommunity.service.member;

import com.armycommunity.dto.request.member.MemberRequest;
import com.armycommunity.dto.response.member.MemberDetailResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.MemberMapper;
import com.armycommunity.mapper.SongMapper;
import com.armycommunity.model.member.Member;
import com.armycommunity.model.member.MemberLine;
import com.armycommunity.model.member.MemberLineAssignment;
import com.armycommunity.model.song.Song;
import com.armycommunity.repository.member.MemberLineAssignmentRepository;
import com.armycommunity.repository.member.MemberRepository;
import com.armycommunity.repository.song.SongMemberRepository;
import com.armycommunity.repository.song.SongRepository;
import com.armycommunity.service.filestorage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberLineAssignmentRepository memberLineAssignmentRepository;
    private final SongRepository songRepository;
    private final SongMemberRepository songMemberRepository;
    private final MemberMapper memberMapper;
    private final SongMapper songMapper;
    private final FileStorageService fileStorageService;


    @Override
    @Transactional
    public MemberDetailResponse createMember(MemberRequest request, MultipartFile profileImage) {
        Member member = memberMapper.toEntity(request);

        // Save profile image if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imagePath = fileStorageService.storeFile(profileImage, "members");
                member.setProfileImagePath(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store profile image", e);
            }
        }

        Member savedMember = memberRepository.save(member);

        // Save line assignments if provided
        if (request.getLineTypes() != null && !request.getLineTypes().isEmpty()) {
            for (MemberLine lineType : request.getLineTypes()) {
                MemberLineAssignment lineAssignment = new MemberLineAssignment();
                lineAssignment.setMember(savedMember);
                lineAssignment.setLineType(lineType);
                memberLineAssignmentRepository.save(lineAssignment);
            }
        }

        return getMemberById(savedMember.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        MemberDetailResponse response = memberMapper.toDetailResponse(member);

        // Add line types
        List<MemberLineAssignment> lineAssignments = memberLineAssignmentRepository.findByMemberId(memberId);
        response.setLineTypes(lineAssignments.stream()
                .map(MemberLineAssignment::getLineType)
                .collect(Collectors.toSet()));

        // Add song count
        List<Song> songs = songRepository.findSongsByMemberId(memberId);
        response.setSongCount(songs.size());

        // Add recent songs (limit to 5)
        List<SongSummaryResponse> recentSongs = songs.stream()
                .sorted((s1, s2) -> s2.getReleaseDate().compareTo(s1.getReleaseDate()))
                .limit(5)
                .map(songMapper::toSummaryResponse)
                .collect(Collectors.toList());
        response.setRecentSongs(recentSongs);

        // For now, leave albums empty - will be populated if needed
        response.setAlbums(List.of());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberDetailResponse getMemberByStageName(String stageName) {
        Member member = memberRepository.findByStageName(stageName)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with stage name: " + stageName));

        return getMemberById(member.getId());
    }

    @Override
    @Transactional
    public MemberDetailResponse updateMember(Long memberId, MemberRequest request, MultipartFile profileImage) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        memberMapper.updateEntity(request, member);

        // Update profile image if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Delete previous image if exists
                if (member.getProfileImagePath() != null) {
                    fileStorageService.deleteFile(member.getProfileImagePath());
                }

                String imagePath = fileStorageService.storeFile(profileImage, "members");
                member.setProfileImagePath(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store profile image", e);
            }
        }

        Member updatedMember = memberRepository.save(member);

        // Update line assignments if provided
        if (request.getLineTypes() != null) {
            // Remove existing assignments
            memberLineAssignmentRepository.deleteByMemberId(memberId);

            // Add new assignments
            for (MemberLine lineType : request.getLineTypes()) {
                MemberLineAssignment lineAssignment = new MemberLineAssignment();
                lineAssignment.setMember(updatedMember);
                lineAssignment.setLineType(lineType);
                memberLineAssignmentRepository.save(lineAssignment);
            }
        }

        return getMemberById(updatedMember.getId());
    }

    @Override
    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        // Delete profile image if exists
        if (member.getProfileImagePath() != null) {
            try {
                fileStorageService.deleteFile(member.getProfileImagePath());
            } catch (IOException e) {
                // Log error but continue with deletion
                System.err.println("Failed to delete profile image: " + e.getMessage());
            }
        }

        // Delete line assignments
        memberLineAssignmentRepository.deleteByMemberId(memberId);

        // Delete song-member associations
        songMemberRepository.deleteByMemberId(memberId);

        // Delete the member
        memberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(memberMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly =true)
    public List<MemberSummaryResponse> getMembersByLine(MemberLine lineType) {
        List<Member> members = memberRepository.findByLineType(lineType);
        return members.stream()
                .map(memberMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberSummaryResponse> getMembersBySong(Long songId) {
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song not found with id: " + songId);
        }

        List<Member> members = memberRepository.findBySongId(songId);
        return members.stream()
                .map(memberMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Member findOrCreateMember(MemberRequest request) {
        // Try to find by stage name first
        if (request.getStageName() != null) {
            return memberRepository.findByStageName(request.getStageName())
                    .orElseGet(() -> memberRepository.save(memberMapper.toEntity(request)));
        }

        // If stage name is not provided, just create a new member
        return memberRepository.save(memberMapper.toEntity(request));
    }
}

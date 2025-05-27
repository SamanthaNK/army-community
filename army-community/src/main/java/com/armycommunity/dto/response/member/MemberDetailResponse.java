package com.armycommunity.dto.response.member;

import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.member.MemberLine;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponse {
    private Long id;
    private String stageName;
    private String realName;
    private LocalDate birthday;
    private String position;
    private String profileImagePath;
    private Set<MemberLine> lineTypes;
    private Integer songCount;
    private List<AlbumSummaryResponse> albums;  // Solo albums and features
    private List<SongSummaryResponse> recentSongs;
}

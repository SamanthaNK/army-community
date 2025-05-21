package com.armycommunity.dto.response.member;

import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.member.MemberLine;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class MemberDetailResponse {
    private Long id;
    private String stageName;
    private String realName;
    private LocalDate birthday;
    private String position;
    private String profileImagePath;
    private Set<MemberLine> lineTypes;  // RAP_LINE, VOCAL_LINE, etc.
    private int songCount;
    private List<AlbumSummaryResponse> albums;  // Solo albums and features
    private List<SongSummaryResponse> recentSongs;
}

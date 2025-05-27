package com.armycommunity.dto.response.album;

import com.armycommunity.dto.response.member.MemberSummaryResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.album.AlbumType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private AlbumType albumType;
    private LocalDate releaseDate;
    private EraSummaryResponse era;
    private String artist;
    private Boolean isOfficial;
    private String coverImagePath;
    private String description;
    private List<SongSummaryResponse> songs;
    private List<MemberSummaryResponse> members;
    private Integer collectionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.armycommunity.dto.response.song;

import com.armycommunity.dto.response.album.AlbumSummaryResponse;
import com.armycommunity.dto.response.member.MemberSummaryResponse;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongDetailResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private Integer duration;
    private Integer trackNumber;
    private Boolean isTitle;
    private String lyrics;
    private String language;
    private String[] featuringArtist;
    private LocalDate releaseDate;
    private String releaseType;
    private String artist;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AlbumSummaryResponse album;
    private List<MemberSummaryResponse> members;
    private List<MusicVideoResponse> musicVideos;
    private String formattedDuration; // MM:SS format
    private Boolean isBTSOfficial;
    private Boolean hasFeatures;
}

package com.armycommunity.dto.response.song;

import com.armycommunity.dto.response.member.MemberSummaryResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SongDetailResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private Long albumId;
    private String albumTitle;
    private String albumCoverImagePath;
    private Integer trackNumber;
    private Integer duration;
    private boolean isTitle;
    private String language;
    private List<String> featuringArtist;
    private LocalDate releaseDate;
    private String lyrics;
    private String artist;
    private String url;
    private List<MemberSummaryResponse> members;
    private List<MusicVideoResponse> musicVideos;
}

package com.armycommunity.dto.response.song;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SongSummaryResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private Long albumId;
    private String albumTitle;
    private Integer trackNumber;
    private Integer duration;
    private boolean isTitle;
    private String language;
    private List<String> featuringArtist;
    private LocalDate releaseDate;
}

package com.armycommunity.dto.response.album;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AlbumSummaryResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private String albumType;
    private LocalDate releaseDate;
    private Long eraId;
    private String eraName;
    private String artist;
    private String coverImagePath;
    private int songCount;
    private int totalDuration; // in seconds
    private int collectionCount;
}

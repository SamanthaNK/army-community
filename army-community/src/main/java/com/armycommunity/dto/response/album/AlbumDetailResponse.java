package com.armycommunity.dto.response.album;

import com.armycommunity.dto.response.song.SongSummaryResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AlbumDetailResponse {
    // From albums table
    private Long id;
    private String title;
    private String koreanTitle;
    private String albumType;
    private LocalDate releaseDate;
    private String artist;
    private boolean isOfficial;
    private String coverImagePath;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long eraId;
    private String eraName;

    private List<SongSummaryResponse> songs;
    private Map<String, String> memberCredits;  // From member_albums table (member -> role)
    private int songCount;
    private int totalDuration;  // in seconds
    private int collectionCount;  // from user_collections table count

}

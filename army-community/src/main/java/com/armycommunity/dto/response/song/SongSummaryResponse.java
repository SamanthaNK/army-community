package com.armycommunity.dto.response.song;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongSummaryResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private Integer duration;
    private Integer trackNumber;
    private boolean isTitle;
    private String language;
}

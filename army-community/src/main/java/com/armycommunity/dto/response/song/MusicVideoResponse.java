package com.armycommunity.dto.response.song;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicVideoResponse {
    private Long id;
    private Long songId;
    private String songTitle;
    private Long albumId;
    private String albumTitle;
    private String title;
    private LocalDate releaseDate;
    private String videoType;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.armycommunity.dto.response.album;

import com.armycommunity.model.album.AlbumType;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumSummaryResponse {
    private Long id;
    private String title;
    private String koreanTitle;
    private AlbumType albumType;
    private LocalDate releaseDate;
    private String artist;
    private String coverImagePath;
    private int songCount;
    private int collectionCount;
}

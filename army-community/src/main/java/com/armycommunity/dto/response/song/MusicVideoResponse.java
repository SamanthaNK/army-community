package com.armycommunity.dto.response.song;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
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
}

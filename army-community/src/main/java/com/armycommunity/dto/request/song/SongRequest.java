package com.armycommunity.dto.request.song;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SongRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private Long albumId;

    @Size(max = 200, message = "Korean title cannot exceed 200 characters")
    private String koreanTitle;

    @NotNull(message = "Duration is required")
    private Integer duration;

    private Integer trackNumber;
    private Boolean isTitle;
    private String lyrics;
    private String language;
    private List<String> featuringArtist;
    private LocalDate releaseDate;
    private String releaseType;
    private String artist;
    private String url;
    private List<Long> memberIds;
}

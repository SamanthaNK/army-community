package com.armycommunity.dto.request.song;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MusicVideoRequest {
    @NotNull(message = "Song ID is required")
    private Long songId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotBlank(message = "Video type is required")
    private String videoType;

    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL cannot exceed 255 characters")
    private String url;
}

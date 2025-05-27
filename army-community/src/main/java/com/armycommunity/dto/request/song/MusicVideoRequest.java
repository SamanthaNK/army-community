package com.armycommunity.dto.request.song;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicVideoRequest {
    @NotNull(message = "Song ID is required")
    private Long songId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotBlank(message = "Video type is required")
    @Size(max = 50, message = "Video type must not exceed 50 characters")
    private String videoType;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;
}

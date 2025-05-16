package com.armycommunity.dto.request.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AlbumRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Album type is required")
    private String albumType;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Size(max = 200, message = "Korean title cannot exceed 200 characters")
    private String koreanTitle;

    private Long eraId;

    @NotBlank(message = "Artist is required")
    @Size(max = 100, message = "Artist name cannot exceed 100 characters")
    private String artist;

    private Boolean isOfficial;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}

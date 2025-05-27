package com.armycommunity.dto.request.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumRequest {

    @NotBlank(message = "Album title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 200, message = "Korean title cannot exceed 200 characters")
    private String koreanTitle;

    @NotBlank(message = "Album type is required")
    private String albumType;

    @NotNull(message = "Release date is required")
    @Past(message = "Release date must be in the past")
    private LocalDate releaseDate;

    private Long eraId;

    @NotBlank(message = "Artist is required")
    @Size(max = 100, message = "Artist name cannot exceed 100 characters")
    private String artist;

    private Boolean isOfficial = true;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private List<Long> memberIds; // For solo or sub-unit albums
}

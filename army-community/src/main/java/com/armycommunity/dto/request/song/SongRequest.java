package com.armycommunity.dto.request.song;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SongRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    private Long albumId;

    @Size(max = 200, message = "Korean title cannot exceed 200 characters")
    private String koreanTitle;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    private Integer trackNumber;

    @Builder.Default
    private Boolean isTitle = false;

    @Pattern(regexp = "^(https?://.*doolset.*|)$",
            message = "Doolset URL must be a valid Doolset link")
    private String doolsetUrl;

    @Pattern(regexp = "^(https?://genius\\.com/.*|)$",
            message = "Genius URL must be a valid Genius link")
    private String geniusUrl;

    private String language;

    private String[] featuringArtist;

    private LocalDate releaseDate;

    @Pattern(regexp = "ALBUM_TRACK|SOUNDCLOUD|YOUTUBE|FREE_RELEASE|UNOFFICIAL|OTHER",
            message = "Release type must be one of: ALBUM_TRACK, SOUNDCLOUD, YOUTUBE, FREE_RELEASE, UNOFFICIAL, OTHER")
    private String releaseType;

    @NotBlank(message = "Artist is required")
    private String artist;

    private String url;

    // List of member IDs that perform on this song
    private List<Long> memberIds;

    // Helper methods
    public boolean hasLyricsLinks() {
        return hasDoolsetUrl() || hasGeniusUrl();
    }

    public boolean hasDoolsetUrl() {
        return doolsetUrl != null && !doolsetUrl.trim().isEmpty();
    }

    public boolean hasGeniusUrl() {
        return geniusUrl != null && !geniusUrl.trim().isEmpty();
    }

    public boolean hasMembers() {
        return memberIds != null && !memberIds.isEmpty();
    }
}

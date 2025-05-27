package com.armycommunity.dto.request.post;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Event date is required")
    @FutureOrPresent(message = "Event date must be in the present or future")
    private LocalDateTime eventDate;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @NotBlank(message = "Time zone is required")
    @Size(max = 50, message = "Time zone cannot exceed 50 characters")
    private String timeZone;

    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;
}

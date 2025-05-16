package com.armycommunity.dto.response.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String timeZone;
    private String eventType;
    private Long creatorId;
    private String creatorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

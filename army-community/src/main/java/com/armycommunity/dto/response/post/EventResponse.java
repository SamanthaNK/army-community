package com.armycommunity.dto.response.post;

import com.armycommunity.dto.response.user.UserSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String timeZone;
    private String eventType;
    private Long creatorId;
    private UserSummaryResponse createdBy;
    private Boolean isVerified;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;
    private UserSummaryResponse verifier; // Details of who verified
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

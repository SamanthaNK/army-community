package com.armycommunity.dto.response.user;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    Long userId;
    String username;
    private String type;
    private String message;
    private Boolean isRead;
    private Long relatedEntityId;
    private String relatedEntityType;
    private LocalDateTime createdAt;

    public boolean isRecent() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }
}

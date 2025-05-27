package com.armycommunity.dto.response.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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

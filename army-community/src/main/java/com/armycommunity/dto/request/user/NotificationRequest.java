package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Notification type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    private Long relatedEntityId;

    @Size(max = 50, message = "Related entity type must not exceed 50 characters")
    private String relatedEntityType;
}

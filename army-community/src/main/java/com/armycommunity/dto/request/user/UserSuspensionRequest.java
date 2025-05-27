package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuspensionRequest {
    @NotBlank
    private String reason;
    private LocalDateTime suspendUntil; // null for indefinite
}

package com.armycommunity.dto.request.user;

import com.armycommunity.model.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPromotionRequest {
    @NotNull
    private UserRole newRole;

    private String reason;
}

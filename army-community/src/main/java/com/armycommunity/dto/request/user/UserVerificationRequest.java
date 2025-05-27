package com.armycommunity.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationRequest {
    @NotBlank
    private String verificationType;
}

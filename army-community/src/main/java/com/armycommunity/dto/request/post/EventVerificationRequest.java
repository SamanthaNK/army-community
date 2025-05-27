package com.armycommunity.dto.request.post;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventVerificationRequest {
    @NotNull
    private Boolean verified;

    private String verificationNotes;
}

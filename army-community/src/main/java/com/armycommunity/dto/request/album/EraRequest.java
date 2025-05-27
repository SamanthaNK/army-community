package com.armycommunity.dto.request.album;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EraRequest {

    @NotBlank(message = "Era name is required")
    @Size(max = 100, message = "Era name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}

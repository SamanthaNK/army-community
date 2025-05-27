package com.armycommunity.dto.response.album;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EraDetailResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isCurrent;
    private List<AlbumSummaryResponse> albums;
    private Integer albumCount;
}

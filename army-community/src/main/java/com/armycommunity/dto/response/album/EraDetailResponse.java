package com.armycommunity.dto.response.album;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EraDetailResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private boolean isCurrent;
    private List<AlbumSummaryResponse> albums;
}

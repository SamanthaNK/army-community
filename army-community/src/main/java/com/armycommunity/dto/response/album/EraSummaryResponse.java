package com.armycommunity.dto.response.album;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EraSummaryResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private int albumCount;
    private boolean isCurrent;
}

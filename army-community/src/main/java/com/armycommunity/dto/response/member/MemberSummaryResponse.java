package com.armycommunity.dto.response.member;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberSummaryResponse {
    private Long id;
    private String stageName;
    private String realName;
    private LocalDate birthday;
    private String position;
    private String profileImagePath;
}

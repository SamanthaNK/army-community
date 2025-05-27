package com.armycommunity.dto.response.member;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryResponse {
    private Long id;
    private String stageName;
    private String profileImagePath;
}

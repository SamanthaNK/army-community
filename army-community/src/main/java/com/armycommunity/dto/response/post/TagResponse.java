package com.armycommunity.dto.response.post;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
}

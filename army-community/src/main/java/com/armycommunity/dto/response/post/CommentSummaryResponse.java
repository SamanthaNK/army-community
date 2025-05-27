package com.armycommunity.dto.response.post;

import com.armycommunity.dto.response.user.UserSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSummaryResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserSummaryResponse user;
    private int replyCount;
}

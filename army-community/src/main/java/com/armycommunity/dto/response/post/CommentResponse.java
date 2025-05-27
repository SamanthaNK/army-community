package com.armycommunity.dto.response.post;

import com.armycommunity.dto.response.user.UserSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private UserSummaryResponse user;
    private Long postId;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<CommentResponse> replies;
}

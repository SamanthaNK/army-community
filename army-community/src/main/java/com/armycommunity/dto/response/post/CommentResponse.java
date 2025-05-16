package com.armycommunity.dto.response.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CommentResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userProfileImage;
    private Long postId;
    private Long parentCommentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int replyCount;
    private List<CommentResponse> replies;
}

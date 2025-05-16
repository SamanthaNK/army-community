package com.armycommunity.dto.response.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostResponse {
    private Long id;
    private Long userId;
    private String username;
    private String userProfileImage;
    private String content;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long commentCount;
    private Long likeCount;
    private boolean isLikedByCurrentUser;
    private List<String> tags;
}

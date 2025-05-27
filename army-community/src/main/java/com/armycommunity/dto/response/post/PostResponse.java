package com.armycommunity.dto.response.post;

import com.armycommunity.dto.response.user.UserSummaryResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String content;
    private List<String> imagePaths;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryResponse author;

    // Engagement metrics
    private Long likeCount;
    private Long commentCount;
    private Long repostCount;

    // User interaction status
    private Boolean isLikedByUser;
    private Boolean isRepostedByUser;

    // Repost information
    private Boolean isRepost;
    private String repostComment;
    private PostResponse originalPost;

    // Moderation status
    private Boolean isDeleted;
    private Boolean needsModeration;
}

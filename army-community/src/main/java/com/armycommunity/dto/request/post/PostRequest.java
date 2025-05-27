package com.armycommunity.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content cannot exceed 5000 characters")
    private String content;

    @Size(max = 4, message = "Cannot upload more than 5 images")
    private List<MultipartFile> images;

    @Size(max = 10, message = "Cannot have more than 10 tags")
    private List<String> tags;

    // For reposting
    private Long originalPostId;

    @Size(max = 500, message = "Repost comment cannot exceed 500 characters")
    private String repostComment;

    // Validation helper methods
    public boolean isRepost() {
        return originalPostId != null;
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    public boolean hasRepostComment() {
        return repostComment != null && !repostComment.trim().isEmpty();
    }
}

package com.armycommunity.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;
}

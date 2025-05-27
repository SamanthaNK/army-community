package com.armycommunity.service.comment;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.response.post.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long userId, Long postId, CommentRequest request);

    CommentResponse replyToComment(Long parentCommentId, CommentRequest request, Long userId);

    CommentResponse updateComment(Long commentId, Long userId, CommentRequest request);

    void deleteComment(Long commentId, Long userId);

    List<CommentResponse> getPostComments(Long postId);

    List<CommentResponse> getCommentReplies(Long commentId);

    Long countPostComments(Long postId);

    List<CommentResponse> getUserComments(Long userId);
}

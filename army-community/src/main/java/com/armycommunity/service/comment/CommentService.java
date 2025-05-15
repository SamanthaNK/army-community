package com.armycommunity.service.comment;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.response.post.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long userId, Long postId, CommentRequest request);

    CommentResponse replyToComment(Long userId, Long commentId, CommentRequest request);

    CommentResponse updateComment(Long commentId, Long userId, CommentRequest request);

    void deleteComment(Long commentId, Long userId);

    Page<CommentResponse> getPostComments(Long postId, Pageable pageable);

    List<CommentResponse> getCommentReplies(Long commentId);

    Integer countPostComments(Long postId);

    Page<CommentResponse> getUserComments(Long userId, Pageable pageable);
}

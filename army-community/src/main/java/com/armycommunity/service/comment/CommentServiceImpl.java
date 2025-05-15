package com.armycommunity.service.comment;

import com.armycommunity.dto.response.post.CommentResponse;
import com.armycommunity.dto.request.post.CommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
        // TODO: Implement comment creation
        return null;
    }

    @Override
    @Transactional
    public CommentResponse replyToComment(Long userId, Long commentId, CommentRequest request) {
        // TODO: Implement comment creation
        return null;
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        // TODO: Implement comment creation
        return null;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // TODO: Implement comment deletion
    }

    @Override
    public Page<CommentResponse> getPostComments(Long postId, Pageable pageable) {
        // TODO: Implement post comments retrieval
        return null;
    }

    @Override
    public List<CommentResponse> getCommentReplies(Long commentId) {
        // TODO: Implement post comments retrieval
        return null;
    }

    @Override
    public Integer countPostComments(Long postId) {
        // TODO: Implement post comments retrieval
        return null;
    }

    @Override
    public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
        // TODO: Implement post comments retrieval
        return null;
    }
}

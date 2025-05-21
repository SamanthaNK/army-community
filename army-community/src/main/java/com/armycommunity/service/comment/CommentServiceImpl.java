package com.armycommunity.service.comment;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.response.post.CommentResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.CommentMapper;
import com.armycommunity.model.post.Comment;
import com.armycommunity.model.post.Post;
import com.armycommunity.model.user.User;
import com.armycommunity.repository.post.CommentRepository;
import com.armycommunity.repository.post.PostRepository;
import com.armycommunity.repository.user.UserRepository;
import com.armycommunity.service.activitylog.ActivityLogService;
import com.armycommunity.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        Comment comment = commentMapper.toEntity(request, user, post);
        Comment savedComment = commentRepository.save(comment);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_CREATE", "COMMENT", savedComment.getId(), Collections.emptyMap());

        // Send notification to post owner if it's not the same user
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                    post.getUser().getId(),
                    "NEW_COMMENT",
                    user.getUsername() + " commented on your post",
                    postId,
                    "POST"
            );
        }

        return buildCommentResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse replyToComment(Long userId, Long commentId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (parentComment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        Post post = parentComment.getPost();

        Comment reply = commentMapper.toEntity(request, user, post);
        reply.setParentCommentId(commentId);
        Comment savedReply = commentRepository.save(reply);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_REPLY", "COMMENT", savedReply.getId(), Collections.emptyMap());

        // Send notification to parent comment owner if it's not the same user
        if (!parentComment.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                    parentComment.getUser().getId(),
                    "COMMENT_REPLY",
                    user.getUsername() + " replied to your comment",
                    commentId,
                    "COMMENT"
            );
        }

        return buildCommentResponse(savedReply);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this comment");
        }

        if (comment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        commentMapper.updateEntity(request, comment);
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_UPDATE", "COMMENT", updatedComment.getId(), Collections.emptyMap());

        return buildCommentResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }

        comment.setIsDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_DELETE", "COMMENT", commentId, Collections.emptyMap());
    }

    @Override
    public Page<CommentResponse> getPostComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        Page<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseAndParentCommentIdNullOrderByCreatedAtDesc(postId, pageable);
        return new PageImpl<>(
                comments.getContent().stream()
                        .map(this::buildCommentResponse)
                        .collect(Collectors.toList()),
                pageable,
                comments.getTotalElements()
        );
    }

    @Override
    public List<CommentResponse> getCommentReplies(Long commentId) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (parentComment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        List<Comment> replies = commentRepository.findByParentCommentId(commentId);

        return replies.stream()
                .filter(comment -> !comment.getIsDeleted())
                .map(this::buildCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countPostComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            return 0;
        }

        return Math.toIntExact(commentRepository.countByPostId(postId));
    }

    @Override
    public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<Comment> comments = commentRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId, pageable);
        return new PageImpl<>(
                comments.getContent().stream()
                        .map(this::buildCommentResponse)
                        .collect(Collectors.toList()),
                pageable,
                comments.getTotalElements()
        );
    }

    // Helper methods

    private CommentResponse buildCommentResponse(Comment comment) {
        CommentResponse response = commentMapper.toResponse(comment);

        // Set reply count
        if (comment.getParentCommentId() == null) {
            long replyCount = commentRepository.findByParentCommentId(comment.getId()).stream()
                    .filter(reply -> !reply.getIsDeleted())
                    .count();
            response.setReplyCount((int) replyCount);
        } else {
            response.setReplyCount(0);
        }

        // Set empty replies list by default
        response.setReplies(Collections.emptyList());

        return response;
    }
}

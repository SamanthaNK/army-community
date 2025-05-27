package com.armycommunity.service.comment;

import com.armycommunity.dto.request.post.CommentRequest;
import com.armycommunity.dto.request.user.NotificationRequest;
import com.armycommunity.dto.response.post.CommentResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.exception.UnauthorizedException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Creating comment for postId: {} by userId: {}", postId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        Comment comment = commentMapper.toEntity(request, user, post);
        comment.setIsDeleted(false);

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created with id: {} for postId: {} by userId: {}", savedComment.getId(), postId, userId);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_CREATE", "COMMENT", savedComment.getId(), Collections.emptyMap());

        // Send notification to the post's owner if it's a different user
        if (!Objects.equals(post.getUser().getId(), userId)) {
            notificationService.createCommentNotification(
                    post.getUser().getId(),
                    userId,
                    postId,
                    user.getUsername()
            );
        }
        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse replyToComment(Long parentCommentId, CommentRequest request, Long userId) {
        log.info("Replying to commentId: {} by userId: {}", parentCommentId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + parentCommentId));

        if (parentComment.getIsDeleted()) {
            throw new IllegalArgumentException("Comment not found with id: " + parentCommentId);
        }

        Comment reply = commentMapper.toEntity(request, user, parentComment.getPost());
        reply.setParentCommentId(parentCommentId);
        reply.setIsDeleted(false);


        Comment savedReply = commentRepository.save(reply);
        log.info("Reply created with id: {} to commentId: {} by userId: {}", savedReply.getId(), parentCommentId, userId);

        // Log activity
        activityLogService.logActivity(
                userId, "COMMENT_REPLY", "COMMENT", savedReply.getId(), Collections.emptyMap());

        // Send notification to the parent comment owner if it's a different user
        if (!parentComment.getUser().getId().equals(userId)) {
            NotificationRequest replyNotificationRequest = NotificationRequest.builder()
                    .userId(parentComment.getUser().getId())
                    .type("REPLY")
                    .message(user.getUsername() + " replied to your comment")
                    .relatedEntityId(savedReply.getId())
                    .relatedEntityType("COMMENT")
                    .build();

            notificationService.createNotification(replyNotificationRequest);
        }

        // Notify the original post owner if they're different from both the replier and parent comment owner
        Post originalPost = parentComment.getPost();
        if (!originalPost.getUser().getId().equals(userId) &&
                !originalPost.getUser().getId().equals(parentComment.getUser().getId())) {

            notificationService.createCommentNotification(
                    originalPost.getUser().getId(),
                    userId,
                    originalPost.getId(),
                    user.getUsername()
            );
        }
        return commentMapper.toResponse(savedReply);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        log.info("Updating commentId: {} by userId: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("Comment not found with id: " + commentId);
        }

        // Check if the user is the owner of the comment
        if (Objects.equals(comment.getUser().getId(), userId)) {
            throw new UnauthorizedException("You can only update your own comments");
        }

        commentMapper.updateCommentFromRequest(request, comment);
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment updated with id: {} by userId: {}", updatedComment.getId(), userId);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_UPDATE", "COMMENT", updatedComment.getId(), Collections.emptyMap());

        return commentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        log.info("Deleting commentId: {} by userId: {}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (comment.getIsDeleted()) {
            log.warn("Comment with id: {} is already deleted", commentId);
            return;
        }

        if (Objects.equals(comment.getUser().getId(), userId)) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        comment.setIsDeleted(true);
        comment.setContent("[Comment deleted]");
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // Log activity
        activityLogService.logActivity(userId, "COMMENT_DELETE", "COMMENT", commentId, Collections.emptyMap());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getPostComments(Long postId) {
        log.info("Fetching comments for postId: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseAndParentCommentIdNullOrderByCreatedAtDesc(postId);
        return commentMapper.toResponseList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentReplies(Long parentCommentId) {
        log.info("Fetching replies for commentId: {}", parentCommentId);

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + parentCommentId));

        if (parentComment.getIsDeleted()) {
            throw new ResourceNotFoundException("Comment not found with id: " + parentCommentId);
        }

        List<Comment> replies = commentRepository.findByParentCommentId(parentCommentId);

        return commentMapper.toResponseList(replies);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPostComments(Long postId) {
        log.info("Counting comments for postId: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getIsDeleted()) {
            return 0L;
        }

        Long count = commentRepository.countByPostId(postId);
        log.debug("Post {} has {} comments", postId, count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getUserComments(Long userId) {
        log.info("Fetching comments for userId: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Comment> comments = commentRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
        return commentMapper.toResponseList(comments);
    }
}

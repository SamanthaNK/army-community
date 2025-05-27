package com.armycommunity.repository.post;

import com.armycommunity.model.post.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CommentRepository is an interface for managing Comment entities.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndIsDeletedFalseAndParentCommentIdNullOrderByCreatedAtDesc(Long postId);

    List<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    long countByPostId(Long postId);
}

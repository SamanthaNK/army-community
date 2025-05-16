package com.armycommunity.repository.post;

import com.armycommunity.model.post.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostIdAndIsDeletedFalseAndParentCommentIdNullOrderByCreatedAtDesc(Long postId, Pageable pageable);

    Page<Comment> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Comment> findByParentCommentId(Long parentCommentId);

    long countByPostId(Long postId);
}

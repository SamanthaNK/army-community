package com.armycommunity.repository.post;

import com.armycommunity.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByIsDeletedFalse(Pageable pageable);

    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findAllActivePosts(Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p " +
            "INNER JOIN follows f ON p.user_id = f.following_id " +
            "WHERE f.follower_id = ?1 AND p.is_deleted = false " +
            "ORDER BY p.created_at DESC",
            nativeQuery = true)
    Page<Post> findPostsFromFollowedUsers(Long userId, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p " +
            "INNER JOIN post_tags pt ON p.id = pt.post_id " +
            "WHERE pt.tag_id = ?1 AND p.is_deleted = false " +
            "ORDER BY p.created_at DESC",
            nativeQuery = true)
    Page<Post> findPostsByTagId(Long tagId, Pageable pageable);
}

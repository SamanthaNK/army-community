package com.armycommunity.repository.post;

import com.armycommunity.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query("SELECT p FROM Post p " +
            "WHERE p.isDeleted = false " +
            "AND (LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.user.username) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    @Query(value = """
    SELECT p.* FROM posts p
    LEFT JOIN reactions r ON p.id = r.post_id
    LEFT JOIN comments c ON p.id = c.post_id
    WHERE p.is_deleted = false
    AND (p.created_at >= :since
        OR r.created_at >= :since
        OR c.created_at >= :since)
    GROUP BY p.id
    ORDER BY
        (COUNT(DISTINCT r.id) * 2 + COUNT(DISTINCT c.id)) DESC,
        p.created_at DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Post> findTrendingPosts(@Param("since") LocalDateTime since, @Param("limit") int limit);
}

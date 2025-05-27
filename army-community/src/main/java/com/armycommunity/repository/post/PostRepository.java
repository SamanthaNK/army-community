package com.armycommunity.repository.post;

import com.armycommunity.model.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PostRepository is an interface for managing Post entities.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

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

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND " +
            "(p.user.userRole = 'USER' OR p.content LIKE %:keyword%) " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsNeedingModeration(@Param("keyword") String keyword);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Post p WHERE p.user.id = :userId AND p.originalPost.id = :originalPostId AND p.isDeleted = false")
    boolean existsByUserIdAndOriginalPostId(@Param("userId") Long userId, @Param("originalPostId") Long originalPostId);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.user.userRole = 'USER' " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsFromRegularUsers(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND " +
            "p.createdAt > :sinceDate ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(@Param("sinceDate") LocalDateTime sinceDate, Pageable pageable);

    // For finding posts that might need moderation
    default List<Post> findPostsNeedingModeration() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return findRecentPosts(oneDayAgo, PageRequest.of(0, 50));
    }
}

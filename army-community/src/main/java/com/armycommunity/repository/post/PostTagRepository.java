package com.armycommunity.repository.post;

import com.armycommunity.model.post.Post;
import com.armycommunity.model.post.PostTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing PostTag entities.
 */
@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTag.PostTagId> {
    List<PostTag> findByPostId(Long postId); // TODO: use this method

    void deleteByPostId(Long postId); // TODO: use this method

    @Query("SELECT pt.tag.id, COUNT(pt) FROM PostTag pt GROUP BY pt.tag ORDER BY COUNT(pt) DESC")
    List<Object[]> findMostUsedTags(Pageable pageable); // TODO: use this method
}

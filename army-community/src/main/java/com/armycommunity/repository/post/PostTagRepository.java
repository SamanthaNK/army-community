package com.armycommunity.repository.post;

import com.armycommunity.model.post.PostTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTag.PostTagId> {
    List<PostTag> findByPostId(Long postId);

    void deleteByPostId(Long postId);

    @Query("SELECT pt.tag.id, COUNT(pt) FROM PostTag pt GROUP BY pt.tag ORDER BY COUNT(pt) DESC")
    List<Object[]> findMostUsedTags(Pageable pageable);
}

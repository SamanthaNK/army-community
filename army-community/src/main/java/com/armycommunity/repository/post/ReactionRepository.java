package com.armycommunity.repository.post;

import com.armycommunity.model.post.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Reaction entities.
 */
@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Reaction.ReactionId> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    long countByPostId(Long postId);
}

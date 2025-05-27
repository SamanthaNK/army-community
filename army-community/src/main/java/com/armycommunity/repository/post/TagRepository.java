package com.armycommunity.repository.post;

import com.armycommunity.model.post.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TagRepository is an interface for managing Tag entities.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}

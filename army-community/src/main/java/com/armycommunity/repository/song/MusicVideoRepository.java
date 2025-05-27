package com.armycommunity.repository.song;

import com.armycommunity.model.song.MusicVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing MusicVideo entities
 */
@Repository
public interface MusicVideoRepository extends JpaRepository<MusicVideo, Long> {
    List<MusicVideo> findBySongId(Long songId);

    List<MusicVideo> findByVideoType(String videoType);

    Long countBySongId(Long songId);
}

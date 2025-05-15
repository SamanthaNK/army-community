package com.armycommunity.service.musicvideo;

import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.response.song.MusicVideoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MusicVideoService {
    MusicVideoResponse createMusicVideo(MusicVideoRequest request);

    MusicVideoResponse getMusicVideoById(Long id);

    MusicVideoResponse updateMusicVideo(Long id, MusicVideoRequest request);

    void deleteMusicVideo(Long id);

    List<MusicVideoResponse> getMusicVideosBySong(Long songId);

    List<MusicVideoResponse> getMusicVideosByType(String videoType);

    Page<MusicVideoResponse> getAllMusicVideos(Pageable pageable);
}

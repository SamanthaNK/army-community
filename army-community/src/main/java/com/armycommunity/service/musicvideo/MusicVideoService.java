package com.armycommunity.service.musicvideo;

import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.response.song.MusicVideoResponse;

import java.util.List;

public interface MusicVideoService {
    MusicVideoResponse createMusicVideo(MusicVideoRequest request);

    MusicVideoResponse getMusicVideoById(Long id);

    MusicVideoResponse updateMusicVideo(Long id, MusicVideoRequest request);

    void deleteMusicVideo(Long id);

    List<MusicVideoResponse> getMusicVideosBySong(Long songId);

    List<MusicVideoResponse> getMusicVideosByType(String videoType);

    List<MusicVideoResponse> getAllMusicVideos();

    Boolean existsById(Long id);

    Long countBySongId(Long songId);
}

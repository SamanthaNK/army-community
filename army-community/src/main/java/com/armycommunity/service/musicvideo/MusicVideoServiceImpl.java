package com.armycommunity.service.musicvideo;

import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.response.song.MusicVideoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicVideoServiceImpl implements MusicVideoService {

    @Override
    public MusicVideoResponse createMusicVideo(MusicVideoRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public MusicVideoResponse getMusicVideoById(Long id) {
        // TODO: implement
        return null;
    }

    @Override
    public MusicVideoResponse updateMusicVideo(Long id, MusicVideoRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteMusicVideo(Long id) {
        // TODO: implement
    }

    @Override
    public List<MusicVideoResponse> getMusicVideosBySong(Long songId) {
        // TODO: implement
        return null;
    }

    @Override
    public List<MusicVideoResponse> getMusicVideosByType(String videoType) {
        // TODO: implement
        return null;
    }

    @Override
    public Page<MusicVideoResponse> getAllMusicVideos(Pageable pageable) {
        // TODO: implement
        return null;
    }
}

package com.armycommunity.service.musicvideo;

import com.armycommunity.dto.request.song.MusicVideoRequest;
import com.armycommunity.dto.response.song.MusicVideoResponse;
import com.armycommunity.exception.ResourceNotFoundException;
import com.armycommunity.mapper.MusicVideoMapper;
import com.armycommunity.model.song.MusicVideo;
import com.armycommunity.model.song.Song;
import com.armycommunity.repository.song.MusicVideoRepository;
import com.armycommunity.repository.song.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of MusicVideoService interface for managing music videos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MusicVideoServiceImpl implements MusicVideoService {

    private final MusicVideoRepository musicVideoRepository;
    private final SongRepository songRepository;
    private final MusicVideoMapper musicVideoMapper;

    @Override
    @Transactional
    public MusicVideoResponse createMusicVideo(MusicVideoRequest request) {
        log.info("Creating music video with title: {}", request.getTitle());
        // Find the song to associate with the music video
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + request.getSongId()));

        // Create and map the music video entity
        MusicVideo musicVideo = musicVideoMapper.toEntity(request);
        musicVideo.setSong(song);

        // Save and return the mapped response
        MusicVideo savedMusicVideo = musicVideoRepository.save(musicVideo);
        log.info("Music video created with ID: {} for song: {}", savedMusicVideo.getId(), song.getTitle());
        return musicVideoMapper.toResponse(savedMusicVideo);
    }

    @Override
    @Transactional(readOnly = true)
    public MusicVideoResponse getMusicVideoById(Long id) {
        log.info("Fetching music video with ID: {}", id);

        MusicVideo musicVideo = musicVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music video not found with id: " + id));
        return musicVideoMapper.toResponse(musicVideo);
    }

    @Override
    @Transactional
    public MusicVideoResponse updateMusicVideo(Long id, MusicVideoRequest request) {
        log.info("Updating music video with ID: {}", id);

        // Find the music video to update
        MusicVideo musicVideo = musicVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music video not found with id: " + id));

        // Update song if provided and different
        if (request.getSongId() != null && !request.getSongId().equals(musicVideo.getSong().getId())) {
            Song song = songRepository.findById(request.getSongId())
                    .orElseThrow(() -> new ResourceNotFoundException("Song not found with ID: " + request.getSongId()));
            musicVideo.setSong(song);
            log.debug("Updated song association for music video ID: {}", id);
        }

        // Update other fields
        musicVideoMapper.updateMusicVideoFromRequest(request, musicVideo);

        // Save updated music video
        MusicVideo updatedMusicVideo = musicVideoRepository.save(musicVideo);
        log.info("Successfully updated music video with ID: {}", id);

        return musicVideoMapper.toResponse(updatedMusicVideo);
    }

    @Override
    @Transactional
    public void deleteMusicVideo(Long id) {
        log.info("Deleting music video with ID: {}", id);

        if (!musicVideoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Music video not found with id: " + id);
        }
        musicVideoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicVideoResponse> getMusicVideosBySong(Long songId) {
        log.debug("Fetching music videos for song ID: {}", songId);

        // Check if the song exists
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song not found with id: " + songId);
        }

        List<MusicVideo> musicVideos = musicVideoRepository.findBySongId(songId);
        return musicVideoMapper.toResponseList(musicVideos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicVideoResponse> getMusicVideosByType(String videoType) {
        log.debug("Fetching music videos of type: {}", videoType);

        List<MusicVideo> musicVideos = musicVideoRepository.findByVideoType(videoType);
        return musicVideoMapper.toResponseList(musicVideos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicVideoResponse> getAllMusicVideos() {
        log.debug("Fetching all music videos");
        List<MusicVideo> musicVideos = musicVideoRepository.findAll();
        return musicVideoMapper.toResponseList(musicVideos);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsById(Long id) {
        log.debug("Checking if music video exists with ID: {}", id);

        boolean exists = musicVideoRepository.existsById(id);
        log.debug("Music video exists with ID {}: {}", id, exists);

        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countBySongId(Long songId) {
        log.debug("Counting music videos for song ID: {}", songId);
        return musicVideoRepository.countBySongId(songId);
    }
}

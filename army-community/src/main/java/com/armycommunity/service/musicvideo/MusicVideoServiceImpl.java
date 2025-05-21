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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MusicVideoService interface for managing music videos
 */
@Service
@RequiredArgsConstructor
public class MusicVideoServiceImpl implements MusicVideoService {

    private final MusicVideoRepository musicVideoRepository;
    private final SongRepository songRepository;
    private final MusicVideoMapper musicVideoMapper;

    /**
     * Creates a new music video
     *
     * @param request The music video request containing details
     * @return The created music video response
     */
    @Override
    @Transactional
    public MusicVideoResponse createMusicVideo(MusicVideoRequest request) {
        // Find the song to associate with the music video
        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + request.getSongId()));

        // Create and map the music video entity
        MusicVideo musicVideo = musicVideoMapper.toEntity(request);
        musicVideo.setSong(song);

        // Save and return the mapped response
        MusicVideo savedMusicVideo = musicVideoRepository.save(musicVideo);
        return musicVideoMapper.toResponse(savedMusicVideo);
    }

    /**
     * Retrieves a music video by its ID
     *
     * @param id The ID of the music video to retrieve
     * @return The music video response
     */
    @Override
    @Transactional(readOnly = true)
    public MusicVideoResponse getMusicVideoById(Long id) {
        MusicVideo musicVideo = musicVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music video not found with id: " + id));
        return musicVideoMapper.toResponse(musicVideo);
    }

    /**
     * Updates an existing music video
     *
     * @param id      The ID of the music video to update
     * @param request The updated music video request
     * @return The updated music video response
     */
    @Override
    @Transactional
    public MusicVideoResponse updateMusicVideo(Long id, MusicVideoRequest request) {
        // Find the music video to update
        MusicVideo musicVideo = musicVideoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Music video not found with id: " + id));

        // If the song is changing, find the new song
        if (!musicVideo.getSong().getId().equals(request.getSongId())) {
            Song song = songRepository.findById(request.getSongId())
                    .orElseThrow(() -> new ResourceNotFoundException("Song not found with id: " + request.getSongId()));
            musicVideo.setSong(song);
        }

        // Update the music video entity
        musicVideoMapper.updateEntity(request, musicVideo);

        // Save and return the updated music video
        MusicVideo updatedMusicVideo = musicVideoRepository.save(musicVideo);
        return musicVideoMapper.toResponse(updatedMusicVideo);
    }

    /**
     * Deletes a music video by its ID
     *
     * @param id The ID of the music video to delete
     */
    @Override
    @Transactional
    public void deleteMusicVideo(Long id) {
        if (!musicVideoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Music video not found with id: " + id);
        }
        musicVideoRepository.deleteById(id);
    }

    /**
     * Retrieves all music videos associated with a specific song
     *
     * @param songId The ID of the song
     * @return A list of music video responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<MusicVideoResponse> getMusicVideosBySong(Long songId) {
        // Check if the song exists
        if (!songRepository.existsById(songId)) {
            throw new ResourceNotFoundException("Song not found with id: " + songId);
        }

        return musicVideoRepository.findBySongId(songId).stream()
                .map(musicVideoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all music videos of a specific type
     *
     * @param videoType The type of music video (e.g., OFFICIAL_MV, PERFORMANCE)
     * @return A list of music video responses
     */
    @Override
    @Transactional(readOnly = true)
    public List<MusicVideoResponse> getMusicVideosByType(String videoType) {
        return musicVideoRepository.findByVideoType(videoType).stream()
                .map(musicVideoMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all music videos with pagination
     *
     * @param pageable The pagination information
     * @return A page of music video responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MusicVideoResponse> getAllMusicVideos(Pageable pageable) {
        return musicVideoRepository.findAll(pageable)
                .map(musicVideoMapper::toResponse);
    }
}

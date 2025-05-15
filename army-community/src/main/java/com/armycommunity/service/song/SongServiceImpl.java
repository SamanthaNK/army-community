package com.armycommunity.service.song;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.song.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService{

    @Override
    public SongDetailResponse createSong(SongRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public SongDetailResponse getSongById(Long songId) {
        // TODO: implement
        return null;
    }

    @Override
    public SongDetailResponse updateSong(Long songId, SongRequest request) {
        // TODO: implement
        return null;
    }

    @Override
    public void deleteSong(Long songId) {
        // TODO: implement
    }

    @Override
    public List<SongSummaryResponse> getSongsByAlbum(Long albumId) {
        // TODO: implement
        return null;
    }

    @Override
    public List<SongSummaryResponse> getTitleTracks() {
        // TODO: implement
        return null;
    }

    @Override
    public List<SongSummaryResponse> getSongsByMember(Long memberId) {
        // TODO: implement
        return null;
    }

    @Override
    public List<SongSummaryResponse> getSongsByLanguage(String language) {
        // TODO: implement
        return null;
    }

    @Override
    public Page<SongSummaryResponse> searchSongs(String keyword, Pageable pageable) {
        // TODO: implement
        return null;
    }

    @Override
    public Song findOrCreateSong(SongRequest request) {
        // TODO: implement
        return null;
    }
}

package com.armycommunity.service.song;

import com.armycommunity.dto.request.song.SongRequest;
import com.armycommunity.dto.response.song.SongDetailResponse;
import com.armycommunity.dto.response.song.SongSummaryResponse;
import com.armycommunity.model.song.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SongService {
    SongDetailResponse createSong(SongRequest request);

    SongDetailResponse getSongById(Long songId);

    SongDetailResponse updateSong(Long songId, SongRequest request);

    void deleteSong(Long songId);

    List<SongSummaryResponse> getSongsByAlbum(Long albumId);

    List<SongSummaryResponse> getTitleTracks();

    List<SongSummaryResponse> getSongsByMember(Long memberId);

    List<SongSummaryResponse> getSongsByLanguage(String language);

    Page<SongSummaryResponse> searchSongs(String keyword, Pageable pageable);

    Song findOrCreateSong(SongRequest request);
}

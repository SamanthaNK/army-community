package com.armycommunity.dto.response.song;

import com.armycommunity.model.song.Song;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongLyricsResponse {

    private Long songId;
    private String songTitle;
    private String doolsetUrl;
    private String geniusUrl;
    private boolean hasLyricsLinks;

    public static SongLyricsResponse fromSong(Song song) {
        return SongLyricsResponse.builder()
                .songId(song.getId())
                .songTitle(song.getTitle())
                .doolsetUrl(song.getDoolsetUrl())
                .geniusUrl(song.getGeniusUrl())
                .hasLyricsLinks(song.hasLyricsLinks())
                .build();
    }
}

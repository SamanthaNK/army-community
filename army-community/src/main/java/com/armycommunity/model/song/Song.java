package com.armycommunity.model.song;

import com.armycommunity.model.album.Album;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a song.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "korean_title")
    private String koreanTitle;

    @Column(name = "duration",nullable = false)
    private Integer duration; // in seconds

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "isTitle")
    private Boolean isTitle = false;

    @Column(name = "lyrics", columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "language")
    private String language;

    @Column(name = "featuring_artist", columnDefinition = "text[]")
    private String[] featuringArtist;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "release_type", length = 50)
    private String releaseType;

    @Column(name = "artist")
    private String artist;

    @Column(name = "url")
    private String url;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SongMember> songMembers = new HashSet<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MusicVideo> musicVideos = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

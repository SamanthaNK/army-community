package com.armycommunity.model.album;

import com.armycommunity.model.song.Song;
import com.armycommunity.model.user.UserCollection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title",nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type", nullable = false)
    private AlbumType albumType;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "korean_title", length = 200)
    private String koreanTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "era_id")
    private Era era;

    @Column(name = "artist", nullable = false)
    private String artist;

    @Column(name = "is_official")
    private Boolean isOfficial = true;

    @Column(name = "cover_image_path")
    private String coverImagePath;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Song> songs = new HashSet<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberAlbum> memberAlbums = new HashSet<>();

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserCollection> collections = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /*
    // Helper methods
    public void addSong(Song song) {
        songs.add(song);
        song.setAlbum(this);
    }

    public void removeSong(Song song) {
        songs.remove(song);
        song.setAlbum(null);
    }

    public void addMemberRole(Member member, String role) {
        MemberAlbum memberAlbum = new MemberAlbum();
        memberAlbum.setMember(member);
        memberAlbum.setAlbum(this);
        memberAlbum.setRole(role);
        memberAlbums.add(memberAlbum);
        member.getAlbums().add(memberAlbum);
    }
     */
}

-- Enum tables
-- MEMBER_LINES enum type
CREATE TYPE member_line AS ENUM ('RAP_LINE', 'VOCAL_LINE', 'HYUNG_LINE', 'MAKNAE_LINE', 'DANCE_LINE');

-- ALBUM_TYPES enum type
CREATE TYPE album_type AS ENUM (
    'STUDIO_ALBUM', 'MINI_ALBUM', 'EP', 'SINGLE', 'DIGITAL_SINGLE',
    'MIXTAPE', 'COMPILATION', 'SOUNDTRACK', 'REMIX', 'REPACKAGE',
    'SPECIAL_ALBUM', 'COLLABORATION', 'SOUNDCLOUD', 'PRE_DEBUT', 'LIVE_ALBUM', 'OTHER'
);

-- USERS table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100),
    profile_image_path VARCHAR(255),
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    language_preference VARCHAR(10) DEFAULT 'en',
    time_zone VARCHAR(50) DEFAULT 'UTC',
    oauth_provider VARCHAR(20),
    oauth_id VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- SETTINGS table for user and application settings
CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    theme VARCHAR(50) NOT NULL DEFAULT 'light',
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    setting_key VARCHAR(100),
    setting_value TEXT,
    is_global BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, setting_key)
);
CREATE INDEX idx_settings_user_id ON settings(user_id);
CREATE INDEX idx_settings_key ON settings(setting_key);
CREATE INDEX idx_settings_global ON settings(is_global);

-- ACTIVITY_LOGS table to track user activities
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    ip_address VARCHAR(50),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_action ON activity_logs(action_type);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);

-- MEMBERS table to store BTS members with their details
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    stage_name VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(100) NOT NULL,
    birthday DATE NOT NULL,
    position VARCHAR(100),
    profile_image_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_members_stage_name ON members(stage_name);

-- MEMBER_LINE_ASSIGNMENTS junction table to associate members with lines
CREATE TABLE member_line_assignments (
    member_id BIGINT NOT NULL REFERENCES members(id),
    line_type member_line NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id, line_type)
);

-- ERAS table to store BTS eras
CREATE TABLE eras (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_eras_name ON eras(name);
CREATE INDEX idx_eras_date_range ON eras(start_date, end_date);

-- ALBUMS table for all types of releases
CREATE TABLE albums (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    album_type album_type NOT NULL,
    release_date DATE NOT NULL,
    korean_title VARCHAR(200),
    era_id BIGINT REFERENCES eras(id),
    artist VARCHAR(100) NOT NULL, -- 'BTS', 'RM', 'Jin', etc.
    is_official BOOLEAN DEFAULT TRUE,
    cover_image_path VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_albums_title ON albums(title);
CREATE INDEX idx_albums_release_date ON albums(release_date);
CREATE INDEX idx_albums_artist ON albums(artist);
CREATE INDEX idx_albums_era_id ON albums(era_id);

-- SONGS table to store all songs across all albums
CREATE TABLE songs (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT REFERENCES albums(id), -- Can be NULL for standalone releases
    title VARCHAR(200) NOT NULL,
    korean_title VARCHAR(200),
    duration INTEGER NOT NULL, -- in seconds
    track_number INTEGER, -- Can be NULL for standalone releases
    isTitle BOOLEAN DEFAULT FALSE, -- TRUE for title tracks
    lyrics TEXT,
    language VARCHAR(20),
    featuring_artist TEXT[], -- Array of featuring artists
    release_date DATE, -- For standalone releases not tied to albums
    release_type VARCHAR(50) CHECK (release_type IN ('ALBUM_TRACK', 'SOUNDCLOUD', 'YOUTUBE', 'FREE_RELEASE', 'UNOFFICIAL', 'OTHER')),
    artist VARCHAR(100), -- Only needed for standalone tracks
    url VARCHAR(255), -- Direct link for standalone tracks (SoundCloud, YouTube, etc.)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_songs_title ON songs(title);
CREATE INDEX idx_songs_album_id ON songs(album_id);
CREATE INDEX idx_songs_isTitle ON songs(isTitle);
CREATE INDEX idx_songs_release_date ON songs(release_date);

-- SONG_MEMBERS junction table to track which members perform on which songs
CREATE TABLE song_members (
    song_id BIGINT NOT NULL REFERENCES songs(id),
    member_id BIGINT NOT NULL REFERENCES members(id),
    PRIMARY KEY (song_id, member_id)
);
CREATE INDEX idx_song_members_song_id ON song_members(song_id);
CREATE INDEX idx_song_members_member_id ON song_members(member_id);

-- MEMBER_ALBUMS junction table for solo works/collaborations
CREATE TABLE member_albums (
    member_id BIGINT NOT NULL REFERENCES members(id),
    album_id BIGINT NOT NULL REFERENCES albums(id),
    role VARCHAR(50) NOT NULL, -- 'Main Artist', 'Featured Artist', 'Producer', etc.
    PRIMARY KEY (member_id, album_id)
);
CREATE INDEX idx_member_albums_member_id ON member_albums(member_id);
CREATE INDEX idx_member_albums_album_id ON member_albums(album_id);

-- MUSIC_VIDEOS table to track official and unofficial music videos
CREATE TABLE music_videos (
    id BIGSERIAL PRIMARY KEY,
    song_id BIGINT NOT NULL REFERENCES songs(id),
    title VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    video_type VARCHAR(50) NOT NULL CHECK (video_type IN ('OFFICIAL_MV', 'PERFORMANCE', 'LYRIC_VIDEO', 'DANCE_PRACTICE', 'CONCEPT_FILM', 'OTHER')),
    url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_music_videos_song_id ON music_videos(song_id);
CREATE INDEX idx_music_videos_type ON music_videos(video_type);

-- POSTS table
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    image_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at);

-- COMMENTS table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_comment_id BIGINT REFERENCES comments(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_comment_id);

-- FOLLOWS table
CREATE TABLE follows (
    follower_id BIGINT NOT NULL REFERENCES users(id),
    following_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id)
);
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);

-- REACTIONS table
CREATE TABLE reactions (
    user_id BIGINT NOT NULL REFERENCES users(id),
    post_id BIGINT NOT NULL REFERENCES posts(id),
    reaction_type VARCHAR(20) NOT NULL DEFAULT 'LIKE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id)
);
CREATE INDEX idx_reactions_post_id ON reactions(post_id);

-- TAGS table
CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tags_name ON tags(name);

-- POST_TAGS junction table
CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);
CREATE INDEX idx_post_tags_post_id ON post_tags(post_id);
CREATE INDEX idx_post_tags_tag_id ON post_tags(tag_id);

-- USER_COLLECTIONS table
CREATE TABLE user_collections (
    user_id BIGINT NOT NULL REFERENCES users(id),
    album_id BIGINT NOT NULL REFERENCES albums(id),
    purchase_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, album_id)
);
CREATE INDEX idx_collections_user_id ON user_collections(user_id);

-- EVENTS table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(100),
    time_zone VARCHAR(50) DEFAULT 'UTC',
    event_type VARCHAR(50) NOT NULL,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_events_event_type ON events(event_type);

-- NOTIFICATIONS table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
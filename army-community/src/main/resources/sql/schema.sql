select * from users;
-- Enum tables
-- MEMBER_LINES enum type
create type member_line as ENUM ('RAP_LINE', 'VOCAL_LINE', 'HYUNG_LINE', 'MAKNAE_LINE', 'DANCE_LINE');

-- ALBUM_TYPES enum type
create type album_type as ENUM (
    'STUDIO_ALBUM', 'MINI_ALBUM', 'EP', 'SINGLE', 'DIGITAL_SINGLE',
    'MIXTAPE', 'COMPILATION', 'SOUNDTRACK', 'REMIX', 'REPACKAGE',
    'SPECIAL_ALBUM', 'COLLABORATION', 'SOUNDCLOUD', 'PRE_DEBUT', 'LIVE_ALBUM', 'OTHER'
);

-- USER_ROLES enum type
create type user_role as ENUM (
    'USER',
    'VERIFIED',
    'MODERATOR',
    'ADMIN',
    'SUPER_ADMIN'
);

-- USERS table
CREATE TABLE users (
    id bigserial primary key,
    username varchar(50) not null unique,
    email varchar(100) not null unique,
    password varchar(100),
    profile_image_path varchar(255),
    bio text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',
    verified_at TIMESTAMP,
    verification_type VARCHAR(50),
    suspended_until TIMESTAMP,
    suspension_reason TEXT,
    language_preference varchar(10) default 'en',
    time_zone varchar(50) default 'UTC',
    oauth_provider varchar(20),
    oauth_id varchar(100),
    is_active boolean default true,
    last_login_at timestamp
);
CREATE INDEX idx_users_username on users(username);
CREATE INDEX idx_users_email on users(email);
CREATE INDEX idx_users_user_role ON users(user_role);
CREATE INDEX idx_users_verified_at ON users(verified_at);
CREATE INDEX idx_users_suspended_until ON users(suspended_until);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Set verified_at for users who already have elevated roles
UPDATE users
SET verified_at = NOW(), verification_type = 'LEGACY'
WHERE user_role IN ('VERIFIED', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN');

-- SETTINGS table for user and application settings
CREATE TABLE settings (
    id bigserial primary key,
    user_id bigint references users(id),
    theme varchar(50) not null default 'light',
    notifications_enabled boolean not null default true,
    setting_key varchar(100),
    setting_value text,
    is_global boolean default false,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    unique (user_id, setting_key)
);
CREATE INDEX idx_settings_user_id on settings(user_id);
CREATE INDEX idx_settings_key on settings(setting_key);
CREATE INDEX idx_settings_global on settings(is_global);

-- ACTIVITY_LOGS table to track user activities
CREATE TABLE activity_logs (
    id bigserial primary key,
    user_id bigint references users(id),
    action_type varchar(50) not null,
    entity_type varchar(50) not null,
    entity_id bigint,
    ip_address varchar(50),
    user_agent text,
    details jsonb,
    created_at timestamp not null default current_timestamp
);
CREATE INDEX idx_activity_logs_user_id on activity_logs(user_id);
CREATE INDEX idx_activity_logs_action on activity_logs(action_type);
CREATE INDEX idx_activity_logs_created_at on activity_logs(created_at);

-- MEMBERS table to store BTS members with their details
CREATE TABLE members (
    id bigserial primary key,
    stage_name varchar(50) not null unique,
    real_name varchar(100) not null,
    birthday date not null,
    position varchar(100),
    profile_image_path varchar(255),
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
CREATE INDEX idx_members_stage_name on members(stage_name);

-- MEMBER_LINE_ASSIGNMENTS junction table to associate members with lines
CREATE TABLE member_line_assignments (
    member_id bigint not null references members(id),
    line_type member_line not null,
    created_at timestamp not null default current_timestamp,
    primary key (member_id, line_type)
);

-- ERAS table to store BTS eras
CREATE TABLE eras (
    id bigserial primary key,
    name varchar(100) not null,
    start_date date not null,
    end_date date,
    description text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
CREATE INDEX idx_eras_name on eras(name);
CREATE INDEX idx_eras_date_range on eras(start_date, end_date);

-- ALBUMS table for all types of releases
create table albums (
    id bigserial primary key,
    title varchar(200) not null,
    album_type album_type not null,
    release_date date not null,
    korean_title varchar(200),
    era_id bigint references eras(id),
    artist varchar(100) not null, -- 'BTS', 'RM', 'Jin', etc.
    is_official boolean default true,
    cover_image_path varchar(255),
    description text,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
CREATE INDEX idx_albums_title on albums(title);
CREATE INDEX idx_albums_release_date on albums(release_date);
CREATE INDEX idx_albums_artist on albums(artist);
CREATE INDEX idx_albums_era_id on albums(era_id);

-- SONGS table to store all songs across all albums
create table songs (
    id bigserial primary key,
    album_id bigint references albums(id), -- Can be NULL for standalone releases
    title varchar(200) not null,
    korean_title varchar(200),
    duration integer not null, -- in seconds
    track_number integer, -- Can be NULL for standalone releases
    isTitle boolean default false, -- TRUE for title tracks
    lyrics text,
    language varchar(20),
    featuring_artist text[], -- Array of featuring artists
    release_date date, -- For standalone releases not tied to albums
    release_type varchar(50) check (release_type in ('ALBUM_TRACK', 'SOUNDCLOUD', 'YOUTUBE', 'FREE_RELEASE', 'UNOFFICIAL', 'OTHER')),
    artist varchar(100), -- Only needed for standalone tracks
    url varchar(255), -- Direct link for standalone tracks (SoundCloud, YouTube, etc.)
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
create INDEX idx_songs_title on songs(title);
create INDEX idx_songs_album_id on songs(album_id);
create INDEX idx_songs_isTitle on songs(isTitle);
create INDEX idx_songs_release_date on songs(release_date);

-- SONG_MEMBERS junction table to track which members perform on which songs
create table song_members (
    song_id bigint not null references songs(id),
    member_id bigint not null references members(id),
    primary key (song_id, member_id)
);
create INDEX idx_song_members_song_id on song_members(song_id);
create INDEX idx_song_members_member_id on song_members(member_id);

-- MEMBER_ALBUMS junction table for solo works/collaborations
create TABLE member_albums (
  member_id BIGINT NOT NULL REFERENCES members(id),
  album_id BIGINT NOT NULL REFERENCES albums(id),
  PRIMARY KEY (member_id, album_id)
);
create INDEX idx_member_albums_member_id on member_albums(member_id);
create INDEX idx_member_albums_album_id on member_albums(album_id);

-- MUSIC_VIDEOS table to track official and unofficial music videos
create table music_videos (
    id bigserial primary key,
    song_id bigint not null references songs(id),
    title varchar(200) not null,
    release_date date not null,
    video_type varchar(50) not null check (video_type in ('OFFICIAL_MV', 'PERFORMANCE', 'LYRIC_VIDEO', 'DANCE_PRACTICE', 'CONCEPT_FILM', 'OTHER')),
    url varchar(255) not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);
create INDEX idx_music_videos_song_id on music_videos(song_id);
create INDEX idx_music_videos_type on music_videos(video_type);

-- POSTS table
create TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    image_path VARCHAR(1000),
    original_post_id BIGINT REFERENCES posts(id) ON DELETE SET NULL,
    repost_comment VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_original_post_id ON posts(original_post_id);
CREATE INDEX idx_posts_is_deleted ON posts(is_deleted);
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);

-- COMMENTS table
create table comments (
    id bigserial primary key,
    post_id bigint not null references posts(id),
    user_id bigint not null references users(id),
    parent_comment_id bigint references comments(id),
    content text not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    is_deleted boolean default false
);
create INDEX idx_comments_post_id on comments(post_id);
create INDEX idx_comments_user_id on comments(user_id);
create INDEX idx_comments_parent_id on comments(parent_comment_id);

-- FOLLOWS table
create table follows (
    follower_id bigint not null references users(id),
    following_id bigint not null references users(id),
    created_at timestamp not null default current_timestamp,
    primary key (follower_id, following_id)
);
create INDEX idx_follows_follower on follows(follower_id);
create INDEX idx_follows_following on follows(following_id);

-- REACTIONS table
create table reactions (
    user_id bigint not null references users(id),
    post_id bigint not null references posts(id),
    reaction_type varchar(20) not null default 'LIKE',
    created_at timestamp not null default current_timestamp,
    primary key (user_id, post_id)
);
create INDEX idx_reactions_post_id on reactions(post_id);

-- TAGS table
create table tags (
    id bigserial primary key,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at timestamp not null default current_timestamp
);
create INDEX idx_tags_name on tags(name);

-- POST_TAGS junction table
create table post_tags (
    post_id bigint not null references posts(id),
    tag_id bigint not null references tags(id),
    primary key (post_id, tag_id)
);
create INDEX idx_post_tags_post_id on post_tags(post_id);
create INDEX idx_post_tags_tag_id on post_tags(tag_id);

-- USER_COLLECTIONS table
create table user_collections (
    user_id bigint not null references users(id),
    album_id bigint not null references albums(id),
    purchase_date date,
    notes text,
    created_at timestamp not null default current_timestamp,
    primary key (user_id, album_id)
);
create INDEX idx_collections_user_id on user_collections(user_id);

-- EVENTS table
create table events (
    id bigserial primary key,
    title varchar(100) not null,
    description text,
    event_date timestamp not null,
    location varchar(100),
    time_zone varchar(50) default 'UTC',
    event_type varchar(50) not null,
    created_by bigint references users(id),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp,
    CONSTRAINT fk_events_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
);
create INDEX idx_events_event_date ON events(event_date);
create INDEX idx_events_event_type ON events(event_type);
create INDEX idx_events_is_verified ON events(is_verified);
create INDEX idx_events_verified_by ON events(verified_by);
create INDEX idx_events_verified_at ON events(verified_at);

-- NOTIFICATIONS table
create table notifications (
    id bigserial primary key,
    user_id bigint not null references users(id),
    type varchar(50) not null,
    message text not null,
    is_read boolean default false,
    related_entity_id bigint,
    related_entity_type varchar(50),
    created_at timestamp not null default current_timestamp
);
create INDEX idx_notifications_user_id on notifications(user_id);
create INDEX idx_notifications_is_read on notifications(is_read);



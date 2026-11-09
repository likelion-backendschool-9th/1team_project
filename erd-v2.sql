-- =============================================
-- 찍먹 ERD v2 — DDL (36 Tables, PostgreSQL 16)
-- =============================================
-- erdcloud.com import용
-- =============================================

-- ── 회원 ──

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(30) NOT NULL UNIQUE,
    profile_image VARCHAR(500),
    bio TEXT,
    tech_stacks JSONB,
    github_username VARCHAR(50),
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    chip_balance INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE admins (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(30) NOT NULL,
    admin_role VARCHAR(20) NOT NULL DEFAULT 'STAFF',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE social_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (provider, provider_id)
);

-- ── 미니홈피 ──

CREATE TABLE mini_homes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    featured_demo_id BIGINT,
    title VARCHAR(50),
    intro VARCHAR(300),
    skin_theme VARCHAR(50) DEFAULT 'default',
    today_count INT NOT NULL DEFAULT 0,
    total_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE guestbooks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    home_id BIGINT NOT NULL REFERENCES mini_homes(id),
    writer_id BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(500) NOT NULL,
    is_secret BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- ── 데모 ──

CREATE TABLE demos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    demo_type VARCHAR(20) NOT NULL DEFAULT 'STATIC',
    file_path VARCHAR(500),
    api_url VARCHAR(500),
    openapi_json TEXT,
    scenario_json TEXT,
    external_url VARCHAR(500),
    thumbnail VARCHAR(500),
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    price_chips INT NOT NULL DEFAULT 0,
    play_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

ALTER TABLE mini_homes
    ADD CONSTRAINT fk_mini_homes_featured_demo
    FOREIGN KEY (featured_demo_id) REFERENCES demos(id);

CREATE TABLE tags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE demo_tags (
    demo_id BIGINT NOT NULL REFERENCES demos(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY (demo_id, tag_id)
);

CREATE TABLE demo_type_config (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    demo_id BIGINT NOT NULL UNIQUE REFERENCES demos(id),
    health_check_url VARCHAR(500),
    last_health_at TIMESTAMP,
    is_healthy BOOLEAN NOT NULL DEFAULT TRUE,
    fallback_to_replay BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE demo_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    demo_id BIGINT NOT NULL REFERENCES demos(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, demo_id)
);

CREATE TABLE demo_feedbacks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    demo_id BIGINT NOT NULL REFERENCES demos(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ── 블로그 ──

CREATE TABLE posts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    demo_id BIGINT REFERENCES demos(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url VARCHAR(500),
    is_paid BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    view_count INT NOT NULL DEFAULT 0,
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE post_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    post_id BIGINT NOT NULL REFERENCES posts(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, post_id)
);

CREATE TABLE post_comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT REFERENCES post_comments(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ── 피드 ──

CREATE TABLE feeds (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(500) NOT NULL,
    demo_id BIGINT REFERENCES demos(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    repost_count INT NOT NULL DEFAULT 0,
    poll_options JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE feed_tags (
    feed_id BIGINT NOT NULL REFERENCES feeds(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    PRIMARY KEY (feed_id, tag_id)
);

CREATE TABLE feed_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    feed_id BIGINT NOT NULL REFERENCES feeds(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, feed_id)
);

CREATE TABLE feed_comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    feed_id BIGINT NOT NULL REFERENCES feeds(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    parent_id BIGINT REFERENCES feed_comments(id),
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE feed_votes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    feed_id BIGINT NOT NULL REFERENCES feeds(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    option_index SMALLINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (feed_id, user_id)
);

CREATE TABLE feed_reposts (
    user_id BIGINT NOT NULL REFERENCES users(id),
    feed_id BIGINT NOT NULL REFERENCES feeds(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, feed_id)
);

-- ── 소셜 ──

CREATE TABLE follows (
    follower_id BIGINT NOT NULL REFERENCES users(id),
    following_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id != following_id)
);

CREATE TABLE blocks (
    blocker_id BIGINT NOT NULL REFERENCES users(id),
    blocked_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_id, blocked_id),
    CHECK (blocker_id != blocked_id)
);

CREATE TABLE bookmarks (
    user_id BIGINT NOT NULL REFERENCES users(id),
    demo_id BIGINT NOT NULL REFERENCES demos(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, demo_id)
);

-- ── DM (방 기반) ──

CREATE TABLE dm_rooms (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE dm_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES dm_rooms(id),
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE TABLE dm_room_members (
    room_id BIGINT NOT NULL REFERENCES dm_rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    last_read_message_id BIGINT REFERENCES dm_messages(id),
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    left_at TIMESTAMP,
    PRIMARY KEY (room_id, user_id)
);

-- ── 알림 ──

CREATE TABLE notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    receiver_id BIGINT NOT NULL REFERENCES users(id),
    actor_id BIGINT REFERENCES users(id),
    type VARCHAR(30) NOT NULL,
    target_type VARCHAR(20),
    target_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── 결제·구독 ──

CREATE TABLE blog_subscriptions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subscriber_id BIGINT NOT NULL REFERENCES users(id),
    blog_owner_id BIGINT NOT NULL REFERENCES users(id),
    price_chips INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE chip_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    balance_after INT NOT NULL,
    description VARCHAR(200),
    admin_id BIGINT REFERENCES admins(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE settlements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_chips INT NOT NULL,
    amount_krw INT NOT NULL,
    fee INT NOT NULL,
    net_amount INT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    confirmed_by BIGINT REFERENCES admins(id),
    confirmed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── 관리자 운영 ──

CREATE TABLE reports (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id),
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    handled_by BIGINT REFERENCES admins(id),
    admin_note TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    handled_at TIMESTAMP
);

CREATE TABLE user_sanctions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    admin_id BIGINT NOT NULL REFERENCES admins(id),
    reason VARCHAR(500) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,
    released_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE admin_action_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES admins(id),
    action_type VARCHAR(30) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── 피드 추천 ──

CREATE TABLE user_interest_tags (
    user_id BIGINT NOT NULL REFERENCES users(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, tag_id)
);

CREATE TABLE user_tag_profile (
    user_id BIGINT NOT NULL REFERENCES users(id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    weight INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, tag_id)
);

-- ── 추가 인덱스 ──

CREATE INDEX idx_demos_user_created ON demos (user_id, created_at DESC);
CREATE INDEX idx_demos_popular ON demos (like_count DESC, created_at DESC);
CREATE INDEX idx_posts_user_created ON posts (user_id, created_at DESC);
CREATE INDEX idx_feeds_created ON feeds (created_at DESC);
CREATE INDEX idx_demo_feedbacks_demo ON demo_feedbacks (demo_id, created_at);
CREATE INDEX idx_post_comments_post ON post_comments (post_id, created_at);
CREATE INDEX idx_feed_comments_feed ON feed_comments (feed_id, created_at);
CREATE INDEX idx_notifications_user ON notifications (receiver_id, is_read, created_at DESC);
CREATE INDEX idx_dm_messages_room ON dm_messages (room_id, created_at);
CREATE INDEX idx_reports_status ON reports (status, created_at DESC);
CREATE INDEX idx_chip_transactions_user ON chip_transactions (user_id, created_at);
CREATE INDEX idx_admin_logs_admin ON admin_action_logs (admin_id, created_at);
CREATE INDEX idx_admin_logs_target ON admin_action_logs (target_type, target_id);

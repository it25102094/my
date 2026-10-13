-- =============================================================================
-- SPORTIX — SMART SPORTS STREAMING & HIGHLIGHTS PLATFORM
-- SE2030 Web Engineering Schema  (Group 2026-Y2-S1-MTR-15)
-- Database Name: sportix_db
--
-- HOW TO RUN (MySQL Workbench 8.x / MySQL Server 8.0.16+):
--   1. Open this file in MySQL Workbench (File > Open SQL Script).
--   2. Click the "Execute" (lightning) button to run the WHOLE script.
--   3. Refresh the SCHEMAS panel — "sportix_db" will appear with all tables
--      and demo data.
--
-- WARNING: The first statement DROPS sportix_db, so running this script again
--          resets the database to the demo state below.
--
-- MySQL 8.0.16 or newer is required (CHECK constraints are enforced).
--
-- FIXES APPLIED IN THIS VERSION
--   * Seed password hash now really is BCrypt("Sportix@123") — the previous
--     hash did not match, so none of the demo accounts could log in.
--   * likes.reaction_type added so the Like / Dislike buttons (Member 4) work.
--   * campaigns / advertisements / notifications got CHECK constraints that
--     match the values used by the Java code (Member 5).
--   * Demo dates are relative to CURDATE() so the featured video and the demo
--     ad campaign are always "running" whenever the project is demonstrated.
--   * Extra demo rows (followed teams, notifications, comments, reactions,
--     ratings, saved searches, Watch Later folders, a pending report) so every
--     member's CRUD screen has data to show.
-- =============================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS sportix_db;
CREATE DATABASE sportix_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sportix_db;

-- =============================================================================
-- 1. CORE SPORTS & CLASSIFICATION TABLES (Member 3)
-- =============================================================================

CREATE TABLE sports (
    sport_id    INT PRIMARY KEY AUTO_INCREMENT,
    sport_name  VARCHAR(60)  NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_class  VARCHAR(60)  DEFAULT 'fa-trophy',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    category_id   INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(80)  NOT NULL UNIQUE,
    description   VARCHAR(255),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE teams (
    team_id    INT PRIMARY KEY AUTO_INCREMENT,
    team_name  VARCHAR(100) NOT NULL,
    country    VARCHAR(60),
    logo_url   VARCHAR(255) DEFAULT 'default-team.png',
    sport_id   INT          NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_teams_sport FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE CASCADE
);

CREATE TABLE tournaments (
    tournament_id   INT PRIMARY KEY AUTO_INCREMENT,
    tournament_name VARCHAR(120) NOT NULL,
    sport_id        INT          NOT NULL,
    start_date      DATE,
    end_date        DATE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournaments_sport FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE CASCADE
);

-- =============================================================================
-- 2. MODULE 1: USER ACCOUNT MANAGEMENT (Member 1)
-- =============================================================================

CREATE TABLE users (
    user_id          INT PRIMARY KEY AUTO_INCREMENT,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    first_name       VARCHAR(50)  NOT NULL,
    last_name        VARCHAR(50)  NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    contact_number   VARCHAR(20),
    password_hash    VARCHAR(255) NOT NULL,
    role             VARCHAR(30)  NOT NULL DEFAULT 'USER',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    favourite_sports VARCHAR(255) DEFAULT NULL,
    spoiler_free     BOOLEAN      NOT NULL DEFAULT FALSE,
    company_name     VARCHAR(120) DEFAULT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_role   CHECK (role IN ('USER', 'CONTENT_MANAGER', 'ADVERTISER', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_users_role ON users(role);

CREATE TABLE follow_teams (
    follow_id        INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT      NOT NULL,
    team_id          INT      NOT NULL,
    notify_on_upload BOOLEAN  NOT NULL DEFAULT TRUE,
    followed_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_team (user_id, team_id),
    CONSTRAINT fk_ft_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ft_team FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE
);

CREATE TABLE watch_later_folders (
    folder_id   INT PRIMARY KEY AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    folder_name VARCHAR(100) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wlf_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================================================
-- 3. MODULE 3: CONTENT POSTING MANAGEMENT (Member 3)
-- =============================================================================

CREATE TABLE videos (
    video_id          INT PRIMARY KEY AUTO_INCREMENT,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    youtube_url       VARCHAR(500) NOT NULL,
    youtube_video_id  VARCHAR(30)  NOT NULL,
    thumbnail_url     VARCHAR(500) NOT NULL,
    video_type        VARCHAR(30)  NOT NULL DEFAULT 'HIGHLIGHT',
    duration_seconds  INT          NOT NULL DEFAULT 0,
    match_score_text  VARCHAR(100) DEFAULT NULL,
    interviewee_name  VARCHAR(100) DEFAULT NULL,
    is_live_now       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_featured       BOOLEAN      NOT NULL DEFAULT FALSE,
    feature_start     DATE         DEFAULT NULL,
    feature_end       DATE         DEFAULT NULL,
    view_count        INT          NOT NULL DEFAULT 0,
    submission_status VARCHAR(20)  NOT NULL DEFAULT 'APPROVED',
    sport_id          INT          NOT NULL,
    category_id       INT          NOT NULL,
    tournament_id     INT          DEFAULT NULL,
    uploaded_by       INT          NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_videos_sport      FOREIGN KEY (sport_id)      REFERENCES sports(sport_id),
    CONSTRAINT fk_videos_category   FOREIGN KEY (category_id)   REFERENCES categories(category_id),
    CONSTRAINT fk_videos_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id) ON DELETE SET NULL,
    CONSTRAINT fk_videos_uploader   FOREIGN KEY (uploaded_by)   REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_video_type       CHECK (video_type IN ('LIVE', 'HIGHLIGHT', 'SHORT', 'INTERVIEW')),
    CONSTRAINT chk_video_sub_status CHECK (submission_status IN ('APPROVED', 'PENDING', 'REJECTED'))
);

CREATE INDEX idx_videos_status   ON videos(submission_status);
CREATE INDEX idx_videos_type     ON videos(video_type);
CREATE INDEX idx_videos_views    ON videos(view_count DESC);
CREATE INDEX idx_videos_featured ON videos(is_featured, feature_start, feature_end);

CREATE TABLE video_teams (
    video_id INT NOT NULL,
    team_id  INT NOT NULL,
    PRIMARY KEY (video_id, team_id),
    CONSTRAINT fk_vt_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_vt_team  FOREIGN KEY (team_id)  REFERENCES teams(team_id)   ON DELETE CASCADE
);

CREATE TABLE watch_later_items (
    item_id   INT PRIMARY KEY AUTO_INCREMENT,
    user_id   INT          NOT NULL,
    video_id  INT          NOT NULL,
    folder_id INT          DEFAULT NULL,
    notes     VARCHAR(255) DEFAULT NULL,
    added_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wl_user_video (user_id, video_id),
    CONSTRAINT fk_wli_user   FOREIGN KEY (user_id)   REFERENCES users(user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_wli_video  FOREIGN KEY (video_id)  REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_wli_folder FOREIGN KEY (folder_id) REFERENCES watch_later_folders(folder_id) ON DELETE SET NULL
);

-- =============================================================================
-- 4. MODULE 2: VIDEO BROWSING MANAGEMENT (Member 2)
-- =============================================================================

CREATE TABLE search_history (
    search_id      INT PRIMARY KEY AUTO_INCREMENT,
    user_id        INT          NOT NULL,
    search_keyword VARCHAR(150) NOT NULL,
    sport_id       INT          DEFAULT NULL,
    sort_strategy  VARCHAR(30)  DEFAULT 'NEWEST',
    searched_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sh_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_sh_sport FOREIGN KEY (sport_id) REFERENCES sports(sport_id) ON DELETE SET NULL
);

CREATE TABLE saved_searches (
    saved_search_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    search_label    VARCHAR(100) NOT NULL,
    search_keyword  VARCHAR(150) NOT NULL,
    sport_id        INT          DEFAULT NULL,
    category_id     INT          DEFAULT NULL,
    sort_strategy   VARCHAR(30)  NOT NULL DEFAULT 'NEWEST',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ss_user     FOREIGN KEY (user_id)     REFERENCES users(user_id)           ON DELETE CASCADE,
    CONSTRAINT fk_ss_sport    FOREIGN KEY (sport_id)    REFERENCES sports(sport_id)         ON DELETE SET NULL,
    CONSTRAINT fk_ss_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- =============================================================================
-- 5. MODULE 4: USER ENGAGEMENT MANAGEMENT (Member 4)
-- =============================================================================

CREATE TABLE comments (
    comment_id   INT PRIMARY KEY AUTO_INCREMENT,
    video_id     INT          NOT NULL,
    user_id      INT          NOT NULL,
    comment_text VARCHAR(500) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON DELETE CASCADE
);

-- FIX: reaction_type added. The Java code (EngagementDAO) stores both LIKE and
-- DISLIKE reactions here; one reaction per user per video (unique key).
CREATE TABLE likes (
    like_id       INT PRIMARY KEY AUTO_INCREMENT,
    video_id      INT         NOT NULL,
    user_id       INT         NOT NULL,
    reaction_type VARCHAR(10) NOT NULL DEFAULT 'LIKE',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_like_video_user (video_id, user_id),
    CONSTRAINT fk_likes_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON DELETE CASCADE,
    CONSTRAINT chk_reaction_type CHECK (reaction_type IN ('LIKE', 'DISLIKE'))
);

CREATE TABLE ratings (
    rating_id    INT PRIMARY KEY AUTO_INCREMENT,
    video_id     INT      NOT NULL,
    user_id      INT      NOT NULL,
    rating_value INT      NOT NULL,
    rated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating_video_user (video_id, user_id),
    CONSTRAINT fk_ratings_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)   ON DELETE CASCADE,
    CONSTRAINT chk_rating_value CHECK (rating_value BETWEEN 1 AND 5)
);

CREATE TABLE content_reports (
    report_id        INT PRIMARY KEY AUTO_INCREMENT,
    reporter_id      INT          NOT NULL,
    video_id         INT          DEFAULT NULL,
    comment_id       INT          DEFAULT NULL,
    reason           VARCHAR(500) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    resolution_notes VARCHAR(500) DEFAULT NULL,
    resolved_by      INT          DEFAULT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at      DATETIME     DEFAULT NULL,
    CONSTRAINT fk_cr_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id)       ON DELETE CASCADE,
    CONSTRAINT fk_cr_video    FOREIGN KEY (video_id)    REFERENCES videos(video_id)     ON DELETE CASCADE,
    CONSTRAINT fk_cr_comment  FOREIGN KEY (comment_id)  REFERENCES comments(comment_id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_admin    FOREIGN KEY (resolved_by) REFERENCES users(user_id)       ON DELETE SET NULL,
    CONSTRAINT chk_cr_status  CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED'))
);

-- =============================================================================
-- 6. MODULE 5: ADVERTISING & NOTIFICATION MANAGEMENT (Member 5 - IT25102094)
-- =============================================================================

-- A campaign groups ads and controls WHEN they run (start/end date), HOW
-- important they are (priority 1 = high, 2 = normal, 3 = low) and whether
-- they are running (ACTIVE) or temporarily stopped (PAUSED).
CREATE TABLE campaigns (
    campaign_id   INT PRIMARY KEY AUTO_INCREMENT,
    advertiser_id INT            NOT NULL,
    campaign_name VARCHAR(120)   NOT NULL,
    budget        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    priority      INT            NOT NULL DEFAULT 2,
    start_date    DATE           NOT NULL,
    end_date      DATE           NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaigns_advertiser FOREIGN KEY (advertiser_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_campaign_dates      CHECK (end_date >= start_date),
    CONSTRAINT chk_campaign_priority   CHECK (priority BETWEEN 1 AND 3),
    CONSTRAINT chk_campaign_budget     CHECK (budget >= 0),
    CONSTRAINT chk_campaign_status     CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED'))
);

-- An ad is displayed to viewers only when: ad.status = 'ACTIVE' AND its
-- campaign is 'ACTIVE' AND CURDATE() is between the campaign start/end date.
--   BANNER          -> wide image banner (home page)
--   SPONSORED_VIDEO -> YouTube thumbnail card (video page)
--   TEXT            -> text-only sponsored card (video page / search page)
CREATE TABLE advertisements (
    ad_id       INT PRIMARY KEY AUTO_INCREMENT,
    campaign_id INT          NOT NULL,
    ad_title    VARCHAR(150) NOT NULL,
    ad_type     VARCHAR(30)  NOT NULL DEFAULT 'BANNER',
    ad_content  VARCHAR(500),
    media_url   VARCHAR(500),
    target_url  VARCHAR(500) NOT NULL,
    impressions INT          NOT NULL DEFAULT 0,
    clicks      INT          NOT NULL DEFAULT 0,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ads_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(campaign_id) ON DELETE CASCADE,
    CONSTRAINT chk_ad_type     CHECK (ad_type IN ('BANNER', 'SPONSORED_VIDEO', 'TEXT')),
    CONSTRAINT chk_ad_status   CHECK (status IN ('ACTIVE', 'PAUSED'))
);

-- Notifications are created by NotificationFactory (Factory pattern) and by
-- UserObserver objects (Observer pattern) when a followed team gets a new video.
CREATE TABLE notifications (
    notification_id   INT PRIMARY KEY AUTO_INCREMENT,
    user_id           INT          NOT NULL,
    title             VARCHAR(120) NOT NULL,
    message           VARCHAR(300) NOT NULL,
    notification_type VARCHAR(40)  NOT NULL DEFAULT 'NEW_VIDEO',
    target_link       VARCHAR(255) DEFAULT NULL,
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_notif_type CHECK (notification_type IN
        ('NEW_VIDEO', 'VIDEO_APPROVED', 'VIDEO_REJECTED', 'ANNOUNCEMENT', 'PROMOTION', 'REPORT_UPDATE', 'SYSTEM'))
);

CREATE INDEX idx_notif_user_read ON notifications(user_id, is_read);

-- =============================================================================
-- 7. MODULE 6: REPORTING, ANALYTICS & ADMIN MANAGEMENT (Member 6)
-- =============================================================================

CREATE TABLE admin_logs (
    log_id       INT PRIMARY KEY AUTO_INCREMENT,
    admin_id     INT          NOT NULL,
    action_type  VARCHAR(60)  NOT NULL,
    target_table VARCHAR(50)  NOT NULL,
    target_id    INT          DEFAULT NULL,
    description  VARCHAR(500) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_logs_user FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE saved_reports (
    saved_report_id INT PRIMARY KEY AUTO_INCREMENT,
    admin_id        INT          NOT NULL,
    report_title    VARCHAR(150) NOT NULL,
    report_type     VARCHAR(50)  NOT NULL,
    summary_content TEXT         NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_reports_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================================================
-- 8. SEED DATA FOR IMMEDIATE TESTING & DEMO
-- Default password for ALL seed accounts:  Sportix@123
--   kasun_p    -> USER (viewer)          vihan_cm  -> CONTENT_MANAGER
--   thilini_ad -> ADVERTISER (Member 5)  nadun_adm -> ADMIN
-- =============================================================================

INSERT INTO sports (sport_id, sport_name, description, icon_class) VALUES
    (1, 'Cricket',    'ICC T20 World Cup, ODI, Test & LPL',          'fa-baseball-bat-ball'),
    (2, 'Football',   'UEFA Champions League, Premier League, FIFA', 'fa-futbol'),
    (3, 'Basketball', 'NBA Playoffs & FIBA Championships',           'fa-basketball'),
    (4, 'Formula 1',  'FIA Formula 1 World Championship',            'fa-flag-checkered'),
    (5, 'Tennis',     'Grand Slam Wimbledon & US Open',              'fa-table-tennis-paddle-ball');

INSERT INTO categories (category_id, category_name, description) VALUES
    (1, 'Live Matches',     'Real-time live sports broadcasts'),
    (2, 'Match Highlights', 'Extended and condensed key match moments'),
    (3, 'Sports Shorts',    'Quick under-60-second viral plays'),
    (4, 'Interviews',       'Post-match player and coach press conferences');

INSERT INTO teams (team_id, team_name, country, sport_id) VALUES
    (1, 'Sri Lanka Lions',    'Sri Lanka', 1),
    (2, 'India Blue',         'India',     1),
    (3, 'Real Madrid',        'Spain',     2),
    (4, 'Manchester United',  'England',   2),
    (5, 'Los Angeles Lakers', 'USA',       3),
    (6, 'Scuderia Ferrari',   'Italy',     4);

INSERT INTO tournaments (tournament_id, tournament_name, sport_id, start_date, end_date) VALUES
    (1, 'ICC Men''s T20 World Cup 2026', 1, '2026-02-01', '2026-11-30'),
    (2, 'UEFA Champions League 2026',    2, '2026-01-15', '2026-12-15'),
    (3, 'NBA Playoffs 2026',             3, '2026-04-10', '2026-10-30'),
    (4, 'Monaco Grand Prix 2026',        4, '2026-05-20', '2026-05-26');

-- password_hash = BCrypt("Sportix@123")  (verified with jBCrypt 0.4)
INSERT INTO users (user_id, username, first_name, last_name, email, contact_number, password_hash, role, status, favourite_sports, spoiler_free, company_name) VALUES
    (1, 'kasun_p',    'Kasun',   'Perera',      'kasun@sportix.lk',  '0771234561', '$2a$10$2940SyR57J7L1fqIdsh4N.0iHrkJe/CWCBqYKkJDOdct0wEMgNIEe', 'USER',            'ACTIVE', 'Cricket,Football', FALSE, NULL),
    (2, 'vihan_cm',   'Vihan',   'Rajapaksha',  'vihan@sportix.lk',  '0771234566', '$2a$10$2940SyR57J7L1fqIdsh4N.0iHrkJe/CWCBqYKkJDOdct0wEMgNIEe', 'CONTENT_MANAGER', 'ACTIVE', 'Cricket,Football', FALSE, NULL),
    (3, 'thilini_ad', 'Thilini', 'Dissanayake', 'thilini@dialog.lk', '0771234571', '$2a$10$2940SyR57J7L1fqIdsh4N.0iHrkJe/CWCBqYKkJDOdct0wEMgNIEe', 'ADVERTISER',      'ACTIVE', 'Cricket',          FALSE, 'Dialog Axiata PLC'),
    (4, 'nadun_adm',  'Nadun',   'Weerasinghe', 'admin@sportix.lk',  '0771234576', '$2a$10$2940SyR57J7L1fqIdsh4N.0iHrkJe/CWCBqYKkJDOdct0wEMgNIEe', 'ADMIN',           'ACTIVE', 'Cricket,Football', FALSE, NULL);

INSERT INTO videos (video_id, title, description, youtube_url, youtube_video_id, thumbnail_url, video_type, duration_seconds, match_score_text, is_live_now, is_featured, feature_start, feature_end, view_count, submission_status, sport_id, category_id, tournament_id, uploaded_by) VALUES
    (1, 'Sri Lanka vs India - Super Over Thriller | T20 World Cup',
        'Relive every ball of the nail-biting Super Over clash between Sri Lanka and India in Colombo.',
        'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'aqz-KE-bpKQ',
        'https://img.youtube.com/vi/aqz-KE-bpKQ/hqdefault.jpg',
        'LIVE', 7200, 'SL 178/6 (20) tied IND 178/7 (20) - SL Won Super Over', TRUE, TRUE,
        CURDATE() - INTERVAL 30 DAY, CURDATE() + INTERVAL 365 DAY, 48500, 'APPROVED', 1, 1, 1, 2),
    (2, 'Real Madrid vs Manchester United - Champions League Highlights',
        'Every goal, save, and tactical breakdown from a classic European night under the floodlights.',
        'https://www.youtube.com/watch?v=ysz5S6PUM-U', 'ysz5S6PUM-U',
        'https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg',
        'HIGHLIGHT', 680, 'RMA 3 - 2 MUN', FALSE, TRUE,
        CURDATE() - INTERVAL 30 DAY, CURDATE() + INTERVAL 365 DAY, 92400, 'APPROVED', 2, 2, 2, 2),
    (3, 'Buzzer Beater 3-Pointer From Half Court! #Shorts',
        'Unbelievable final-second shot sends the arena into complete pandemonium.',
        'https://www.youtube.com/watch?v=jNQXAC9IVRw', 'jNQXAC9IVRw',
        'https://img.youtube.com/vi/jNQXAC9IVRw/hqdefault.jpg',
        'SHORT', 45, 'LAL 112 - 110 GSW', FALSE, FALSE, NULL, NULL, 154200, 'APPROVED', 3, 3, 3, 2),
    (4, 'Post-Match Captain''s Interview: How We Won The Final',
        'Exclusive pitch-side interview immediately after lifting the championship trophy.',
        'https://www.youtube.com/watch?v=ScMzIvxBSi4', 'ScMzIvxBSi4',
        'https://img.youtube.com/vi/ScMzIvxBSi4/hqdefault.jpg',
        'INTERVIEW', 420, NULL, FALSE, FALSE, NULL, NULL, 19800, 'APPROVED', 1, 4, 1, 2),
    (5, 'Fan Submitted: Club Knockout Final Highlights (Awaiting Moderation)',
        'User submitted highlight video waiting for Member 6 Admin approval.',
        'https://www.youtube.com/watch?v=ysz5S6PUM-U', 'ysz5S6PUM-U',
        'https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg',
        'HIGHLIGHT', 310, '2 - 1', FALSE, FALSE, NULL, NULL, 0, 'PENDING', 2, 2, NULL, 1);

INSERT INTO video_teams (video_id, team_id) VALUES (1, 1), (1, 2), (2, 3), (2, 4), (3, 5), (4, 1), (5, 3);

-- Member 1: kasun_p follows two teams (with upload alerts ON) so the
-- Observer pattern has followers to notify when a new video is approved.
INSERT INTO follow_teams (user_id, team_id, notify_on_upload) VALUES
    (1, 1, TRUE),
    (1, 3, TRUE);

-- Member 2: Watch Later folder + saved item, and a saved search.
INSERT INTO watch_later_folders (folder_id, user_id, folder_name) VALUES (1, 1, 'Weekend Highlights');
INSERT INTO watch_later_items (user_id, video_id, folder_id, notes) VALUES
    (1, 2, 1, 'Watch the 2nd half again'),
    (1, 3, NULL, NULL);
INSERT INTO saved_searches (user_id, search_label, search_keyword, sort_strategy) VALUES
    (1, 'Cricket clips', 'Cricket', 'VIEWS');

-- Member 4: comments, reactions, ratings and one pending content report.
INSERT INTO comments (comment_id, video_id, user_id, comment_text) VALUES
    (1, 1, 1, 'What a finish! Best Super Over I have ever watched.'),
    (2, 2, 2, 'Brilliant tactical breakdown, thanks for uploading.');
INSERT INTO likes (video_id, user_id, reaction_type) VALUES
    (1, 1, 'LIKE'), (1, 2, 'LIKE'), (2, 1, 'LIKE'), (2, 3, 'DISLIKE');
INSERT INTO ratings (video_id, user_id, rating_value) VALUES
    (1, 1, 5), (1, 2, 4), (2, 1, 4);
INSERT INTO content_reports (reporter_id, video_id, reason) VALUES
    (1, 3, 'Wrong Sport Category');

-- Member 5: one running campaign with three ads (one of every ad type).
INSERT INTO campaigns (campaign_id, advertiser_id, campaign_name, budget, priority, start_date, end_date, status) VALUES
    (1, 3, 'Dialog 5G Ultra Sports Streaming Promo', 150000.00, 1, CURDATE() - INTERVAL 30 DAY, CURDATE() + INTERVAL 365 DAY, 'ACTIVE');

INSERT INTO advertisements (ad_id, campaign_id, ad_title, ad_type, ad_content, media_url, target_url, impressions, clicks, status) VALUES
    (1, 1, 'Dialog 5G - Stream Live Cricket With Zero Buffering!', 'BANNER',
        'Upgrade to Dialog 5G Sports Pack and enjoy 4K match streams anywhere in Sri Lanka.',
        'https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=1200&q=80',
        'https://www.dialog.lk', 1240, 89, 'ACTIVE'),
    (2, 1, 'Dialog Sports Pack - Rs. 299 / month', 'TEXT',
        'Unlimited sports streaming data for every match of the season. Activate today!',
        NULL, 'https://www.dialog.lk', 310, 12, 'ACTIVE'),
    (3, 1, 'Watch the T20 World Cup on Dialog ViU', 'SPONSORED_VIDEO',
        'Every ball, every wicket - live on Dialog ViU.',
        'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'https://www.dialog.lk', 150, 9, 'ACTIVE');

INSERT INTO notifications (user_id, title, message, notification_type, target_link, is_read) VALUES
    (1, 'Welcome to Sportix!', 'Follow your favourite teams to get an alert whenever they post a new video.', 'SYSTEM', '/profile', FALSE),
    (1, 'New video: Sri Lanka Lions', 'Sri Lanka Lions posted "Post-Match Captain''s Interview: How We Won The Final".', 'NEW_VIDEO', '/video?id=4', TRUE);

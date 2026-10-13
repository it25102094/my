-- =============================================================================
-- SPORTIX — SMART SPORTS STREAMING & HIGHLIGHTS PLATFORM
-- Simplified SE2030 Web Engineering Schema
-- Database Name: sportix_db
-- =============================================================================

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

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role  ON users(role);

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
CREATE INDEX idx_videos_sport    ON videos(sport_id);
CREATE INDEX idx_videos_category ON videos(category_id);
CREATE INDEX idx_videos_type     ON videos(video_type);
CREATE INDEX idx_videos_views    ON videos(view_count DESC);
CREATE INDEX idx_videos_featured ON videos(is_featured, feature_start, feature_end);

CREATE TABLE video_teams (
                             video_id INT NOT NULL,
                             team_id  INT NOT NULL,
                             PRIMARY KEY (video_id, team_id),
                             CONSTRAINT fk_vt_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
                             CONSTRAINT fk_vt_team  FOREIGN KEY (team_id)  REFERENCES teams(team_id) ON DELETE CASCADE
);

CREATE TABLE watch_later_items (
                                   item_id   INT PRIMARY KEY AUTO_INCREMENT,
                                   user_id   INT          NOT NULL,
                                   video_id  INT          NOT NULL,
                                   folder_id INT          DEFAULT NULL,
                                   notes     VARCHAR(255) DEFAULT NULL,
                                   added_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE KEY uk_wl_user_video (user_id, video_id),
                                   CONSTRAINT fk_wli_user   FOREIGN KEY (user_id)   REFERENCES users(user_id) ON DELETE CASCADE,
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
                                CONSTRAINT fk_sh_user  FOREIGN KEY (user_id)  REFERENCES users(user_id) ON DELETE CASCADE,
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
                                CONSTRAINT fk_ss_user     FOREIGN KEY (user_id)     REFERENCES users(user_id) ON DELETE CASCADE,
                                CONSTRAINT fk_ss_sport    FOREIGN KEY (sport_id)    REFERENCES sports(sport_id) ON DELETE SET NULL,
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
                          CONSTRAINT fk_comments_user  FOREIGN KEY (user_id)  REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE likes (
                       like_id    INT PRIMARY KEY AUTO_INCREMENT,
                       video_id   INT      NOT NULL,
                       user_id    INT      NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       UNIQUE KEY uk_like_video_user (video_id, user_id),
                       CONSTRAINT fk_likes_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
                       CONSTRAINT fk_likes_user  FOREIGN KEY (user_id)  REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE ratings (
                         rating_id    INT PRIMARY KEY AUTO_INCREMENT,
                         video_id     INT      NOT NULL,
                         user_id      INT      NOT NULL,
                         rating_value INT      NOT NULL,
                         rated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE KEY uk_rating_video_user (video_id, user_id),
                         CONSTRAINT fk_ratings_video FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
                         CONSTRAINT fk_ratings_user  FOREIGN KEY (user_id)  REFERENCES users(user_id) ON DELETE CASCADE,
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
                                 CONSTRAINT fk_cr_reporter FOREIGN KEY (reporter_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_cr_video    FOREIGN KEY (video_id)    REFERENCES videos(video_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_cr_comment  FOREIGN KEY (comment_id)  REFERENCES comments(comment_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_cr_admin    FOREIGN KEY (resolved_by) REFERENCES users(user_id) ON DELETE SET NULL,
                                 CONSTRAINT chk_cr_status  CHECK (status IN ('PENDING', 'RESOLVED', 'DISMISSED'))
);

-- =============================================================================
-- 6. MODULE 5: ADVERTISING & NOTIFICATION MANAGEMENT (Member 5)
-- =============================================================================

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
                           CONSTRAINT chk_campaign_dates      CHECK (end_date >= start_date)
);

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
                                CONSTRAINT chk_ad_type     CHECK (ad_type IN ('BANNER', 'SPONSORED_VIDEO', 'TEXT'))
);

CREATE TABLE notifications (
                               notification_id   INT PRIMARY KEY AUTO_INCREMENT,
                               user_id           INT          NOT NULL,
                               title             VARCHAR(120) NOT NULL,
                               message           VARCHAR(300) NOT NULL,
                               notification_type VARCHAR(40)  NOT NULL DEFAULT 'NEW_VIDEO',
                               target_link       VARCHAR(255) DEFAULT NULL,
                               is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
                               created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
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
-- Default password for all seed accounts: Sportix@123
-- =============================================================================

INSERT INTO sports (sport_id, sport_name, description, icon_class) VALUES
                                                                       (1, 'Cricket',    'ICC T20 World Cup, ODI, Test & LPL',         'fa-baseball-bat-ball'),
                                                                       (2, 'Football',   'UEFA Champions League, Premier League, FIFA','fa-futbol'),
                                                                       (3, 'Basketball', 'NBA Playoffs & FIBA Championships',          'fa-basketball'),
                                                                       (4, 'Formula 1',  'FIA Formula 1 World Championship',           'fa-flag-checkered'),
                                                                       (5, 'Tennis',     'Grand Slam Wimbledon & US Open',             'fa-table-tennis-paddle-ball');

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

INSERT INTO users (user_id, username, first_name, last_name, email, contact_number, password_hash, role, status, favourite_sports, spoiler_free, company_name) VALUES
                                                                                                                                                                   (1, 'kasun_p',    'Kasun',   'Perera',      'kasun@sportix.lk',   '0771234561', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER',            'ACTIVE', 'Cricket,Football', FALSE, NULL),
                                                                                                                                                                   (2, 'vihan_cm',   'Vihan',   'Rajapaksha',  'vihan@sportix.lk',   '0771234566', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CONTENT_MANAGER', 'ACTIVE', 'Cricket,Football', FALSE, NULL),
                                                                                                                                                                   (3, 'thilini_ad', 'Thilini', 'Dissanayake', 'thilini@dialog.lk',  '0771234571', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADVERTISER',      'ACTIVE', 'Cricket',          FALSE, 'Dialog Axiata PLC'),
                                                                                                                                                                   (4, 'nadun_adm',  'Nadun',   'Weerasinghe', 'admin@sportix.lk',   '0771234576', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN',           'ACTIVE', 'Cricket,Football', FALSE, NULL);

INSERT INTO videos (video_id, title, description, youtube_url, youtube_video_id, thumbnail_url, video_type, duration_seconds, match_score_text, is_live_now, is_featured, feature_start, feature_end, view_count, submission_status, sport_id, category_id, tournament_id, uploaded_by) VALUES
                                                                                                                                                                                                                                                                                            (1, 'Sri Lanka vs India - Super Over Thriller | T20 World Cup',
                                                                                                                                                                                                                                                                                             'Relive every ball of the nail-biting Super Over clash between Sri Lanka and India in Colombo.',
                                                                                                                                                                                                                                                                                             'https://www.youtube.com/watch?v=aqz-KE-bpKQ', 'aqz-KE-bpKQ',
                                                                                                                                                                                                                                                                                             'https://img.youtube.com/vi/aqz-KE-bpKQ/hqdefault.jpg',
                                                                                                                                                                                                                                                                                             'LIVE', 7200, 'SL 178/6 (20) tied IND 178/7 (20) • SL Won Super Over', TRUE, TRUE, '2026-01-01', '2026-12-31', 48500, 'APPROVED', 1, 1, 1, 2),

                                                                                                                                                                                                                                                                                            (2, 'Real Madrid vs Manchester United - Champions League Highlights',
                                                                                                                                                                                                                                                                                             'Every goal, save, and tactical breakdown from a classic European night under the floodlights.',
                                                                                                                                                                                                                                                                                             'https://www.youtube.com/watch?v=ysz5S6PUM-U', 'ysz5S6PUM-U',
                                                                                                                                                                                                                                                                                             'https://img.youtube.com/vi/ysz5S6PUM-U/hqdefault.jpg',
                                                                                                                                                                                                                                                                                             'HIGHLIGHT', 680, 'RMA 3 - 2 MUN', FALSE, TRUE, '2026-01-01', '2026-12-31', 92400, 'APPROVED', 2, 2, 2, 2),

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

INSERT INTO video_teams (video_id, team_id) VALUES (1, 1), (1, 2), (2, 3), (2, 4), (3, 5), (4, 1);

INSERT INTO campaigns (campaign_id, advertiser_id, campaign_name, budget, priority, start_date, end_date, status) VALUES
    (1, 3, 'Dialog 5G Ultra Sports Streaming Promo', 150000.00, 1, '2026-01-01', '2026-12-31', 'ACTIVE');

INSERT INTO advertisements (ad_id, campaign_id, ad_title, ad_type, ad_content, media_url, target_url, impressions, clicks, status) VALUES
    (1, 1, 'Dialog 5G - Stream Live Cricket With Zero Buffering!', 'BANNER',
     'Upgrade to Dialog 5G Sports Pack and enjoy 4K match streams anywhere in Sri Lanka.',
     'https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?auto=format&fit=crop&w=1200&q=80',
     'https://www.dialog.lk', 1240, 89, 'ACTIVE');
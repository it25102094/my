# Sportix – defects found and fixed

The original project **compiled without errors**, so the problems only appeared at run time:
SQL that referenced tables/columns that do not exist in `sportix_schema.sql`, JSP pages that did not
exist, request attributes that were never set, UI that was never built, and missing access checks.

All fixes keep the original project structure (same packages, same servlets, same JSP folder).
The whole application was re-tested end-to-end (133 automated checks over HTTP: login as every
role and create / read / update / delete in every module) plus an HTML check of 40 pages.

### Proof (original code against the original schema)

| Original code | Result |
|---------------|--------|
| `AdCampaignDAO.createCampaign` → `INSERT INTO ad_campaigns (... priority_level ...)` | **fails** – table `ad_campaigns` not found (every campaign "Database rejected the campaign") |
| `NotificationDAO.addNotification` → `INSERT INTO notifications (user_id, message)` | **fails** – NULL not allowed for column `title` (Observer alerts were never saved) |
| `EngagementDAO` Like / Dislike → table `video_reactions` | **fails** – table not found |
| `EngagementDAO.addReport` → table `video_reports` | **fails** – table not found |
| Upload form "Athletics" (sport_id 6) | **fails** – foreign key violation |
| Seed users with password `Sportix@123` | **cannot log in** – the BCrypt hash in the script was not the hash of `Sportix@123` |

---

## Member 5 – Advertising & Notification Management (IT25102094)

| # | Problem | Fix |
|---|---------|-----|
| 1 | Campaign insert used table `ad_campaigns` / column `priority_level` → every create failed | `AdCampaignDAO` rewritten for the real `campaigns` table with full CRUD + per-campaign ad count, impressions, clicks |
| 2 | Only *create campaign* existed – no list, edit, pause or delete | `CampaignServlet` (`/advertiser/dashboard`, `/advertiser/campaign`) handles create / read / update / status / delete |
| 3 | **No code at all for advertisements** – ads could not be created, listed, edited, deleted or shown | new `AdvertisementDAO`, `AdvertisementServlet` (`/advertiser/ad`), ad CRUD in `AdService` |
| 4 | **Created ads were never displayed** to viewers | new `AdService.getAdsForPlacement()` + `includes/ad-card.jsp`; banner ads on Home, sponsored-video/text ads on the Video page, text ads on Search/browse pages; impressions counted |
| 5 | No click tracking | `/ad/click?id=` counts the click and redirects to the advertiser's site; CTR shown in the dashboard |
| 6 | `advertiser-dashboard.jsp` sat in the webapp root (opened directly, no data, no role check) and contained a stray `</button>` inside `<style>` | moved to `WEB-INF/views/`, rebuilt with stats, campaign form + table, ad form + table, edit mode, pause/resume, delete with confirmation |
| 7 | `/campaign` had no role check – any logged-in viewer could create campaigns | ADVERTISER/ADMIN required in the servlet **and** in `AuthFilter`; advertisers can only touch their own campaigns/ads (ownership checked in SQL) |
| 8 | `AdService` used `PREMIUM/STANDARD` strings for an INT column, rejected one-day campaigns, ignored the budget | priority 1/2/3 (High/Normal/Low), budget validated, dates validated (end ≥ start, no past start), URLs validated (no `javascript:` links) |
| 9 | `Advertisement` model was missing title, content, status, date; listed a type `AD_CARD` that the DB rejects | model now maps every column (`BANNER`, `SPONSORED_VIDEO`, `TEXT`) + campaign info; new `Campaign` model |
| 10 | `NotificationServlet` forwarded to `/WEB-INF/views/notifications.jsp`, **which did not exist** (HTTP 500) | new `notifications.jsp` (All / Unread tabs, open, mark read/unread, mark all read, delete, delete read) |
| 11 | Notification SQL was written inside the servlet; *mark as read* did not check the owner | all SQL moved to `NotificationDAO`; every update/delete is restricted to `user_id` of the logged-in user |
| 12 | Observer alerts never saved (insert without `title`); `VideoService` registered a hard-coded dummy observer (user #1) and notified on *every* upload, even pending ones | real Observer pattern: `VideoUploadSubject` + `UserObserver`; followers of the video's teams (alerts on) are notified only when the video becomes public |
| 13 | No Factory for notifications | `NotificationFactory` builds all notification types (new video, approved, rejected, announcement, promotion, report update, system) |
| 14 | No unread indicator | `NotificationBadgeFilter` + red badge on the bell in the navbar |
| 15 | Admins could not send notifications | Admin Dashboard → *Send a Notification* (all users or one role) + history with *Recall* |
| 16 | Schema allowed any campaign/ad/notification values | CHECK constraints for priority, budget, statuses and notification types |

## Member 1 – User Account Management

| Problem | Fix |
|---------|-----|
| Seed password hash did not match `Sportix@123` – no demo account could log in | correct BCrypt (`$2a$10$…`) hash in the seed data |
| Email regex rejected sub-domains (`it25102094@my.sliit.lk`) | regex fixed |
| Registration did not save a contact number, so *Forgot password* (email + contact) could never work for new users | contact number captured and validated |
| One generic error message for every registration problem | specific messages (username, names, email, phone, password, company, duplicates) |
| Advertisers could not register | account type *Advertiser* with company name |
| Login page never showed *registered / reset / deleted* messages; no *Forgot password* link | added |
| Following teams was impossible from the UI (the video page used attributes that were never set) | follow/unfollow on the video page and on Profile → *Discover Teams*; alerts on/mute per team |
| `FollowTeamServlet` always redirected to `/home` (`getRequestURI()` never contains "profile") | redirect parameter |
| Dead `/toggle-spoiler` branch in `ProfileServlet` | removed (`ToggleSpoilerServlet` handles it) |
| `theme.js` guessed the context path from the URL → Hide Scores toggle posted to a wrong URL on pages like `/user/watchlater` | real context path from the navbar (`data-ctx`) |

## Member 2 – Video Browsing Management

| Problem | Fix |
|---------|-----|
| Watch Later folder CRUD and item update existed in the service/DAO but had **no servlet action or UI** | folder create/rename/delete, filter by folder, move item to folder, notes |
| Saved search *update* had no UI | *Manage* panel (rename, change sort) |
| Sorting the Live/Highlights/Shorts tabs submitted to `/search?q=Live Now` (wrong results) | sort form submits to the current tab |
| `NewestSortStrategy` sorted by id because `created_at` was never loaded | sorts by `created_at` |
| Watch Later cards always showed 0 views | `view_count` selected |
| Interviews had no *See all* page | `/interviews` tab |
| Search ignored team names | search also matches categories and team names |

## Member 3 – Content Posting Management

| Problem | Fix |
|---------|-----|
| `add-video.jsp` started with two stray `<form>` tags before the page directive | removed |
| Upload form offered "Athletics" (id 6, does not exist) and no *Interview* type | sports, categories and teams loaded from the database; all 4 types |
| `category_id` hard-coded to 2 on insert **and** update | selected category saved |
| Selected teams were never saved (`video_teams`) – no followers could be notified | teams saved in the same transaction |
| Edit page mapped the type to LIVE/HIGHLIGHT only (SHORT/INTERVIEW lost) | real `video_type` |
| `mapResultSetToVideo` ignored type, category, thumbnail, uploader, date | all mapped |
| Title limit 150 in Java vs 200 in DB; invalid YouTube links accepted | aligned to 200; YouTube id validated |
| Uploader could not preview a pending video | owner/admin preview mode |
| Rejected videos could not be re-submitted | editing a rejected video sends it back to PENDING |
| Content managers' uploads also waited for approval | CONTENT_MANAGER / ADMIN uploads are published immediately |

## Member 4 – User Engagement Management

| Problem | Fix |
|---------|-----|
| Like/Dislike used table `video_reactions` (does not exist) | `likes` table (+ `reaction_type` column in the schema) |
| Reports used `video_reports(user_id)` (does not exist) | `content_reports(reporter_id)`; duplicate pending reports blocked |
| Comments had no UPDATE | edit own comment, "(edited)" label |
| Ratings table never used | rate 1–5, change, remove, average shown |
| Bad `videoId` → `NumberFormatException` → HTTP 500 | safe parsing |
| Flash message printed twice; Report `onsubmit` on a `<button>` (never fired); `${isLive}` never set | fixed |

## Member 6 – Reporting, Analytics & Admin Management

| Problem | Fix |
|---------|-----|
| **Security:** `AuthFilter` mapped `/admin/*` (which also matches `/admin`) but only checked `startsWith("/admin/")`, and the servlet only checked "logged in" → **any user could open the admin dashboard and approve/reject videos** | ADMIN role enforced in `AuthFilter` and in the servlet |
| Raw SQL inside `AdminControllerServlet` | moved to `AdminDAO` |
| Stored XSS: titles, reasons and messages printed without escaping | everything escaped with `<c:out>` |
| Light-only colours broke the dark theme | theme variables |
| `saved_reports` table never used | save analytics snapshot, rename, delete |
| No user management | change role, suspend/activate, delete (not yourself) |
| Moderation results not communicated | uploader / reporter notified (Factory), followers notified on approval (Observer) |
| No view of admin actions | activity log |
| CSV export broke on commas in sport names, Excel garbled Unicode | proper CSV escaping + UTF-8 BOM, likes/comments columns |

## General

| Problem | Fix |
|---------|-----|
| `web.xml` welcome file `index.jsp` missing | `index.jsp` added (redirects to `/home`) |
| No request encoding → Sinhala/Tamil form input could be garbled | `<request-character-encoding>UTF-8` |
| Tomcat stack traces shown to users | friendly `error.jsp` for 403 / 404 / 500 |
| Numbers/dates unformatted for browsers with `si-LK` locale | fixed JSTL formatting locale |
| `maven-war-plugin` not pinned (old Maven defaults crash on JDK 17+) | plugin versions pinned |
| DB driver hard-coded, unclear error when MySQL is down | `db.driver` property + clear error message |
| `serverTimezone=UTC` in the JDBC URL shifted every displayed time (comments, notifications) by +5:30 on a Sri Lankan PC | `connectionTimeZone=LOCAL` (Connector/J 8.4 default semantics: same zone as the JVM) |
| Mobile menu button had no handler | responsive navbar |
| Mixed CRLF/LF line endings | normalised to LF |

# Sportix – Web-based Sports Video Browsing System

SE2030 Software Engineering Project – Group **id-2026-Y2-S1-MTR-15**

Java 17 · Jakarta Servlet 6 / JSP / JSTL 3 · Apache Tomcat 10.1 · MySQL 8 · Maven · MVC + DAO

> A full list of the defects that were found and how each one was fixed is in **[FIXES.md](FIXES.md)**.

---

## Modules

| # | Module | Owner | Main code |
|---|--------|-------|-----------|
| 1 | User Account Management | IT25102095 | `LoginServlet`, `RegisterServlet`, `ProfileServlet`, `FollowTeamServlet`, `UserService`, `UserDAO`, `FollowTeamDAO` |
| 2 | Video Browsing Management | IT25102096 | `HomeController`, `CategoryBrowseServlet`, `SearchServlet`, `WatchLaterServlet`, `SearchDAO`, `WatchLaterDAO`, `strategy/*SortStrategy` |
| 3 | Content Posting Management | IT25102093 | `AddVideoServlet`, `EditVideoServlet`, `ManageVideosServlet`, `VideoService`, `VideoDAO` |
| 4 | User Engagement Management | Member 4 | `CommentServlet`, `ReactionServlet`, `ReportServlet`, `EngagementService`, `EngagementDAO` |
| 5 | **Advertising & Notification Management** | **IT25102094** | `CampaignServlet`, `AdvertisementServlet`, `NotificationServlet`, `AdService`, `NotificationService`, `AdCampaignDAO`, `AdvertisementDAO`, `NotificationDAO`, `pattern/*`, `strategy/*AdSelectionStrategy`, `NotificationBadgeFilter` |
| 6 | Reporting, Analytics & Admin Management | Member 6 | `AdminControllerServlet`, `ExportReportServlet`, `AdminService`, `AdminDAO` |

---

## Requirements

* **JDK 17** or newer
* **Apache Tomcat 10.1.x** – the project uses `jakarta.*` packages, so **Tomcat 9 will NOT work**
* **MySQL Server 8.0.16+** and **MySQL Workbench** (8.0.16+ is needed because CHECK constraints are used)
* IntelliJ IDEA (Ultimate has built-in Tomcat support; in Community edition use the *Smart Tomcat* plugin) or Maven 3.9+

---

## 1. Create the database (MySQL Workbench)

1. Open MySQL Workbench and connect to your local MySQL server.
2. **File → Open SQL Script…** → select `database/sportix_schema.sql`.
3. Click **Execute** (the lightning icon) to run the **whole** script.
4. Refresh the *SCHEMAS* panel – `sportix_db` appears with 21 tables and demo data.
5. Open `src/main/resources/db.properties` and set **your** MySQL user name and password:

   ```properties
   db.username=root
   db.password=root
   ```

> Running the script again resets the database to the demo data (the script starts with `DROP DATABASE IF EXISTS sportix_db`).

## 2. Run the application

**IntelliJ IDEA**

1. *File → Open…* → select the project folder (the one with `pom.xml`) → open as a Maven project and let Maven download the dependencies.
2. *Run → Edit Configurations… → + → Tomcat Server → Local*; set *Application server* to your **Tomcat 10.1** folder.
3. *Deployment* tab → **+ → Artifact… → `sportix:war exploded`**, set *Application context* to `/sportix`.
4. Run, then open **http://localhost:8080/sportix/**

**Maven (command line)**

```bash
mvn clean package
# copy target/sportix.war into <tomcat>/webapps/ and start Tomcat
# open http://localhost:8080/sportix/
```

## 3. Demo accounts

All demo accounts use the password **`Sportix@123`** (the old seed hash did not match this password – fixed).

| Username | Role | Use it to show |
|----------|------|----------------|
| `kasun_p` | USER (viewer) | browsing, watch later, comments, likes, ratings, reports, follow teams, notifications |
| `vihan_cm` | CONTENT_MANAGER | uploading videos that are published immediately (followers get notified) |
| `thilini_ad` | ADVERTISER | **Member 5** – campaigns & advertisements CRUD, ad statistics |
| `nadun_adm` | ADMIN | moderation, users, advertising oversight, announcements, saved reports, CSV export |

New viewers **and advertisers** can also register from `/register` (choose *Advertise on Sportix*).

---

## 4. Viva walkthrough – CRUD of every module

| Module | Create | Read | Update | Delete |
|--------|--------|------|--------|--------|
| 1 Users | Register (viewer / advertiser) | Profile | Edit profile, change password, reset password, Hide Scores | Delete account |
| 1 Follow teams | Follow (profile → *Discover Teams*, or on a video) | Followed teams | Alerts on / mute | Unfollow |
| 2 Watch Later | Save video, create folder | List, filter by folder | Move to folder / add note, rename folder | Remove video, delete folder |
| 2 Saved searches | *Save Search* on results page | Saved filters | *Manage* → rename / change sort | Delete |
| 3 Videos | *+ Upload* | *My Videos* | Edit | Delete |
| 4 Comments | Post | Comment list | Edit (own) | Delete (own) |
| 4 Reactions / ratings | Like / Dislike / rate 1-5 | Counts & average | Switch reaction / change rating | Remove |
| 4 Reports | Report a video | Admin queue | Admin resolves / dismisses | Admin removes video |
| **5 Campaigns** | Advertiser Dashboard → *Create campaign* | Campaign table with ads, impressions, clicks, CTR | *Edit* / *Pause* / *Resume* | *Delete* (deletes its ads) |
| **5 Advertisements** | Dashboard → *Create advertisement* (Banner / Sponsored video / Text) | Ad table + **ads shown on Home, Video and Search pages** | *Edit* / *Pause* / *Activate* | *Delete* |
| **5 Notifications** | Automatic (Observer + Factory) and admin broadcast | Bell icon with unread badge → inbox | Mark read / unread / all read | Delete, delete all read, admin *Recall* |
| 6 Admin | Save analytics snapshot, send announcement | Dashboard stats, queues, users, logs, CSV | Approve/reject, roles, suspend, rename report | Delete user / report / campaign |

**Suggested Member 5 demo:** log in as `thilini_ad` → create a campaign → add a *Text* ad → open
`/search?q=cricket` in another browser: the new ad is shown → click it (redirects to the advertiser site) →
back in the dashboard the click and impressions are counted → *Pause* the ad: it disappears from the site.
Then log in as `vihan_cm` and upload a video for *Real Madrid* → log in as `kasun_p` (follows Real Madrid):
the bell shows a new notification created by the Observer pattern.

---

## 5. Design patterns (implemented in code, not only described)

| Pattern | Classes | Where it is used |
|---------|---------|------------------|
| **Singleton** | `util/DBConnection` | one shared connection manager for every DAO |
| **DAO** | `dao/*DAO` | all SQL is isolated from the business logic |
| **MVC** | `controller/*` → `service/*` → `dao/*`, views in `WEB-INF/views/*.jsp` | every page |
| **Observer** (Member 5) | `pattern/Subject`, `pattern/Observer`, `pattern/VideoUploadSubject` (concrete subject), `pattern/UserObserver` (concrete observer) | `NotificationService.notifyFollowersOfNewVideo()` – called when a video becomes public (`VideoService.addVideo/updateVideo`, `AdminService.processVideoStatusUpdate`). Each follower of the video's teams (with alerts on) is registered as a `UserObserver` and receives a NEW_VIDEO notification |
| **Factory** (Member 5) | `pattern/NotificationFactory` | builds every notification (NEW_VIDEO, VIDEO_APPROVED, VIDEO_REJECTED, ANNOUNCEMENT, PROMOTION, REPORT_UPDATE, SYSTEM) for `UserObserver`, `NotificationService`, `UserService` |
| **Strategy** (Member 5) | `strategy/AdSelectionStrategy`, `PriorityAdSelectionStrategy`, `RotationAdSelectionStrategy` | `AdService.getAdsForPlacement()` – home banner uses priority, video/search slots use fair rotation |
| **Strategy** (Member 2) | `strategy/SearchStrategy`, `MostViewedSortStrategy`, `NewestSortStrategy` | sorting in `SearchServlet` and `CategoryBrowseServlet` |

---

## 6. Project structure

The original layout was kept. New files are marked with ★.

```
database/sportix_schema.sql            MySQL Workbench script (fixed)
src/main/resources/db.properties       DB connection settings
src/main/java/lk/sportix/
 ├─ controller/   servlets (+ ★AdvertisementServlet)
 ├─ dao/          data access (+ ★AdvertisementDAO)
 ├─ filter/       AuthFilter (+ ★NotificationBadgeFilter)
 ├─ model/        POJOs (+ ★Campaign, ★Notification, ★Sport, ★SavedReport)
 ├─ pattern/      Observer, Subject, UserObserver (+ ★VideoUploadSubject, ★NotificationFactory)
 ├─ service/      business logic
 ├─ strategy/     sort strategies (+ ★AdSelectionStrategy, ★PriorityAdSelectionStrategy, ★RotationAdSelectionStrategy)
 └─ util/         DBConnection (+ ★WebUtil)
src/main/webapp/
 ├─ ★index.jsp                         welcome file (was referenced in web.xml but missing)
 ├─ css/style.css, js/theme.js
 └─ WEB-INF/
     ├─ web.xml
     └─ views/   all JSP pages (advertiser-dashboard.jsp moved here from the webapp root,
                 + ★notifications.jsp, ★error.jsp, includes/★flash.jsp, ★ad-card.jsp, ★video-form-fields.jsp)
```

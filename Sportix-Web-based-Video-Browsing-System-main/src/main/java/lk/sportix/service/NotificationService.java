package lk.sportix.service;

import lk.sportix.dao.FollowTeamDAO;
import lk.sportix.dao.NotificationDAO;
import lk.sportix.model.FollowTeam;
import lk.sportix.model.Notification;
import lk.sportix.model.Video;
import lk.sportix.pattern.NotificationFactory;
import lk.sportix.pattern.NotificationFactory.NotificationType;
import lk.sportix.pattern.UserObserver;
import lk.sportix.pattern.VideoUploadSubject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NotificationService — Member 5 (IT25102094): Notification Management.
 *
 * DESIGN PATTERNS (implemented, not just described):
 *  - OBSERVER : {@link #notifyFollowersOfNewVideo(Video)} creates a {@link VideoUploadSubject},
 *               registers one {@link UserObserver} per follower and calls notifyObservers().
 *  - FACTORY  : every notification is produced by {@link NotificationFactory}.
 *
 * It also provides the notification CRUD used by NotificationServlet:
 *  Create (automatic alerts + admin broadcast), Read (inbox, unread badge),
 *  Update (mark read / unread / all read), Delete (one / all read / recall announcement).
 */
public class NotificationService {

    private static final Set<String> AUDIENCES = Set.of("ALL", "USER", "CONTENT_MANAGER", "ADVERTISER", "ADMIN");

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final FollowTeamDAO followTeamDAO = new FollowTeamDAO();

    // =====================================================================================
    // OBSERVER PATTERN TRIGGER
    // =====================================================================================

    /**
     * Business Logic Method: triggered when a video becomes publicly visible (uploaded by a
     * content manager / admin, or approved by an admin). Every follower of the video's teams
     * with "notify on upload" switched on receives a NEW_VIDEO notification.
     *
     * @return number of users notified
     */
    public int notifyFollowersOfNewVideo(Video video) {
        if (video == null || video.getVideoId() <= 0) return 0;

        VideoUploadSubject subject = new VideoUploadSubject();
        for (FollowTeam follower : followTeamDAO.getFollowersToNotify(video.getVideoId())) {
            // UserObserver.equals() uses the user id, so a user who follows both teams is added once
            subject.registerObserver(new UserObserver(follower.getUserId(), follower.getTeamName(), notificationDAO));
        }
        subject.notifyObservers(video);
        return subject.getObserverCount();
    }

    // =====================================================================================
    // FACTORY-BASED NOTIFICATIONS
    // =====================================================================================

    /** Tells the uploader that the admin approved / rejected their video. */
    public boolean notifyVideoModeration(Video video, boolean approved) {
        if (video == null || video.getUploadedBy() <= 0) return false;
        Notification n = NotificationFactory.createNotification(
                approved ? NotificationType.VIDEO_APPROVED : NotificationType.VIDEO_REJECTED,
                video.getUploadedBy(), video.getTitle(), null,
                approved ? "/video?id=" + video.getVideoId() : "/user/my-videos");
        return notificationDAO.createNotification(n);
    }

    /** Tells the reporter what happened to their content report. */
    public boolean notifyReportUpdate(int reporterId, String reportedTitle, String newStatus) {
        if (reporterId <= 0) return false;
        Notification n = NotificationFactory.createNotification(NotificationType.REPORT_UPDATE, reporterId,
                reportedTitle != null ? reportedTitle : "reported content", newStatus, null);
        return notificationDAO.createNotification(n);
    }

    /**
     * CREATE (admin): broadcast an announcement or promotion to every active user or to one role.
     * @return number of users who received it
     */
    public int broadcast(String type, String title, String message, String audience, String link) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanMessage = message == null ? "" : message.trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > 100) {
            throw new IllegalArgumentException("Notification title is required (maximum 100 characters).");
        }
        if (cleanMessage.isEmpty() || cleanMessage.length() > 300) {
            throw new IllegalArgumentException("Notification message is required (maximum 300 characters).");
        }
        String aud = (audience == null || audience.isBlank()) ? "ALL" : audience.trim().toUpperCase();
        if (!AUDIENCES.contains(aud)) {
            throw new IllegalArgumentException("Unknown audience.");
        }
        String cleanLink = (link == null || link.isBlank()) ? null : link.trim();
        if (cleanLink != null && (!cleanLink.startsWith("/") || cleanLink.startsWith("//") || cleanLink.length() > 255)) {
            throw new IllegalArgumentException("Link must be a Sportix page path such as /home or /video?id=1");
        }
        NotificationType kind = "PROMOTION".equalsIgnoreCase(type) ? NotificationType.PROMOTION : NotificationType.ANNOUNCEMENT;

        Notification template = NotificationFactory.createNotification(kind, 0, cleanTitle, cleanMessage, cleanLink);
        return notificationDAO.broadcast(template, "ALL".equals(aud) ? null : aud);
    }

    /** CREATE: a single SYSTEM notification (e.g. welcome message after registration). */
    public boolean sendSystemNotification(int userId, String title, String message, String link) {
        return notificationDAO.createNotification(
                NotificationFactory.createNotification(NotificationType.SYSTEM, userId, title, message, link));
    }

    // =====================================================================================
    // USER INBOX CRUD
    // =====================================================================================

    public List<Notification> getNotifications(int userId, boolean unreadOnly) {
        return notificationDAO.getNotificationsByUser(userId, unreadOnly);
    }

    public int getUnreadCount(int userId) {
        return userId > 0 ? notificationDAO.getUnreadCount(userId) : 0;
    }

    public boolean markAsRead(int notificationId, int userId) {
        return notificationDAO.setReadStatus(notificationId, userId, true);
    }

    public boolean markAsUnread(int notificationId, int userId) {
        return notificationDAO.setReadStatus(notificationId, userId, false);
    }

    public int markAllAsRead(int userId) {
        return notificationDAO.markAllAsRead(userId);
    }

    public boolean deleteNotification(int notificationId, int userId) {
        return notificationDAO.deleteNotification(notificationId, userId);
    }

    public int clearRead(int userId) {
        return notificationDAO.deleteAllRead(userId);
    }

    // =====================================================================================
    // ADMIN ANNOUNCEMENT HISTORY
    // =====================================================================================

    public List<Map<String, Object>> getRecentAnnouncements() {
        return notificationDAO.getRecentAnnouncements(10);
    }

    public int recallAnnouncement(String title, String message) {
        if (title == null || message == null) return 0;
        return notificationDAO.deleteAnnouncement(title, message);
    }
}

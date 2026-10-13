package lk.sportix.model;

import java.sql.Timestamp;

/**
 * Notification Model — POJO for Member 5 (IT25102094): Advertising & Notification Management.
 * Maps to the 'notifications' table. Instances are normally built by
 * {@link lk.sportix.pattern.NotificationFactory} (Factory design pattern).
 */
public class Notification {

    private int notificationId;
    private int userId;
    private String title;
    private String message;
    private String notificationType;   // NEW_VIDEO, VIDEO_APPROVED, VIDEO_REJECTED, ANNOUNCEMENT, PROMOTION, REPORT_UPDATE, SYSTEM
    private String targetLink;         // context-relative link, e.g. /video?id=4
    private boolean read;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(int userId, String title, String message, String notificationType, String targetLink) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.targetLink = targetLink;
        this.read = false;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTargetLink() { return targetLink; }
    public void setTargetLink(String targetLink) { this.targetLink = targetLink; }

    public boolean isRead() { return read; }
    public boolean getIsRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    /** Small emoji icon per type, used by notifications.jsp. */
    public String getIcon() {
        if (notificationType == null) return "\uD83D\uDD14";
        switch (notificationType) {
            case "NEW_VIDEO":      return "\uD83C\uDFAC"; // clapper board
            case "VIDEO_APPROVED": return "\u2705";       // check mark
            case "VIDEO_REJECTED": return "\u274C";       // cross mark
            case "ANNOUNCEMENT":   return "\uD83D\uDCE2"; // loudspeaker
            case "PROMOTION":      return "\uD83C\uDF81"; // gift
            case "REPORT_UPDATE":  return "\uD83D\uDEA9"; // flag
            default:               return "\uD83D\uDD14"; // bell
        }
    }
}

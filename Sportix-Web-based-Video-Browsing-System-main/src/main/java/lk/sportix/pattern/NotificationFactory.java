package lk.sportix.pattern;

import lk.sportix.model.Notification;

/**
 * NotificationFactory — FACTORY design pattern (Member 5: IT25102094).
 *
 * Every part of the system that needs to alert a user (Observer pattern, admin moderation,
 * admin broadcasts, advertising promotions, report handling) asks this factory for a
 * {@link Notification} instead of building one by hand. The factory decides the title,
 * message wording and type for each kind of notification, so:
 *   - the wording is consistent everywhere,
 *   - the stored notification_type always matches the chk_notif_type CHECK constraint,
 *   - adding a new kind of notification only requires changing this one class.
 */
public final class NotificationFactory {

    /** The kinds of notifications the factory can produce (stored in notifications.notification_type). */
    public enum NotificationType {
        NEW_VIDEO,        // a followed team has a new video (created by UserObserver)
        VIDEO_APPROVED,   // uploader: admin approved the video
        VIDEO_REJECTED,   // uploader: admin rejected the video
        ANNOUNCEMENT,     // admin broadcast message
        PROMOTION,        // advertising / sponsored message
        REPORT_UPDATE,    // reporter: admin resolved or dismissed the report
        SYSTEM            // account / platform messages
    }

    private static final int MAX_TITLE = 120;    // notifications.title VARCHAR(120)
    private static final int MAX_MESSAGE = 300;  // notifications.message VARCHAR(300)

    private NotificationFactory() {
        // static factory - no instances
    }

    /**
     * Factory method.
     *
     * @param type    kind of notification to build
     * @param userId  receiver
     * @param subject short subject (team name, video title, announcement title ...)
     * @param detail  extra detail (video title, admin message, reason ...) - may be null
     * @param link    context-relative link to open (e.g. "/video?id=3") - may be null
     */
    public static Notification createNotification(NotificationType type, int userId,
                                                  String subject, String detail, String link) {
        String s = (subject == null || subject.isBlank()) ? "Sportix" : subject.trim();
        String d = (detail == null) ? "" : detail.trim();
        String title;
        String message;

        switch (type) {
            case NEW_VIDEO:
                title = "New video: " + s;
                message = s + " posted \"" + d + "\". Watch it now!";
                break;
            case VIDEO_APPROVED:
                title = "Your video was approved";
                message = "Good news! \"" + s + "\" is now live on Sportix.";
                break;
            case VIDEO_REJECTED:
                title = "Your video was not approved";
                message = "\"" + s + "\" was rejected by a moderator."
                        + (d.isEmpty() ? "" : " Reason: " + d);
                break;
            case ANNOUNCEMENT:
                title = "\uD83D\uDCE2 " + s;
                message = d.isEmpty() ? s : d;
                break;
            case PROMOTION:
                title = "Sponsored: " + s;
                message = d.isEmpty() ? "Check out this offer from our partners." : d;
                break;
            case REPORT_UPDATE:
                title = "Update on your report";
                message = "Your report about \"" + s + "\" was " + (d.isEmpty() ? "reviewed" : d.toLowerCase())
                        + " by a moderator. Thank you for helping keep Sportix safe.";
                break;
            case SYSTEM:
            default:
                title = s;
                message = d.isEmpty() ? s : d;
                break;
        }

        return new Notification(userId, truncate(title, MAX_TITLE), truncate(message, MAX_MESSAGE),
                type.name(), link);
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "\u2026";
    }
}

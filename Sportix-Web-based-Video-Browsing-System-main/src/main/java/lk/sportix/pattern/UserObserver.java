package lk.sportix.pattern;

import lk.sportix.dao.NotificationDAO;
import lk.sportix.model.Notification;
import lk.sportix.model.Video;

import java.util.Objects;

/**
 * Concrete Observer — OBSERVER design pattern (Member 5: Notifications).
 *
 * One UserObserver represents one follower of a team (row in follow_teams with
 * notify_on_upload = TRUE). When notified, it asks the {@link NotificationFactory}
 * (FACTORY design pattern) to build a NEW_VIDEO notification and saves it with
 * {@link NotificationDAO}, so the alert appears on the user's Notifications page.
 */
public class UserObserver implements Observer {

    private final int userId;
    private final String teamName;
    private final NotificationDAO notificationDAO;

    public UserObserver(int userId, String teamName) {
        this(userId, teamName, new NotificationDAO());
    }

    public UserObserver(int userId, String teamName, NotificationDAO notificationDAO) {
        this.userId = userId;
        this.teamName = teamName;
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void update(Video publishedVideo) {
        Notification alert = NotificationFactory.createNotification(
                NotificationFactory.NotificationType.NEW_VIDEO,
                userId,
                teamName,
                publishedVideo.getTitle(),
                "/video?id=" + publishedVideo.getVideoId());
        // When notified, insert the alert into the database for this specific user
        notificationDAO.createNotification(alert);
    }

    public int getUserId() { return userId; }
    public String getTeamName() { return teamName; }

    /**
     * Two observers are equal when they represent the same user. This stops a user who follows
     * BOTH teams of a match video from receiving the same alert twice.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserObserver)) return false;
        return userId == ((UserObserver) o).userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

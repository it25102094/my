package lk.sportix.pattern;

import lk.sportix.dao.NotificationDAO;

/**
 * Concrete Observer — Updates the database when notified by the Subject.
 */
public class UserObserver implements Observer {

    private final int userId;
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public UserObserver(int userId) {
        this.userId = userId;
    }

    @Override
    public void update(String message) {
        // When notified, insert the alert into the database for this specific user
        notificationDAO.addNotification(userId, message);
    }
}
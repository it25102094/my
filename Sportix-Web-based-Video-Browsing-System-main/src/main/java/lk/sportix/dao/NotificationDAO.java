package lk.sportix.dao;

import lk.sportix.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * NotificationDAO — Handles database operations for user alerts.
 */
public class NotificationDAO {

    public boolean addNotification(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, message);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("NotificationDAO.addNotification error: " + e.getMessage());
            return false;
        }
    }
}
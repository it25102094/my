package lk.sportix.dao;

import lk.sportix.model.Notification;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationDAO — CRUD for the 'notifications' table (Member 5: IT25102094).
 *
 * FIX: the old DAO only had addNotification(user_id, message). The table's "title" column
 * is NOT NULL without a default, so every insert failed and the Observer alerts were never
 * saved. Reading / marking / deleting was done with raw SQL inside the servlet (no MVC) and
 * "mark as read" did not check the owner, so any user could change anyone's notifications.
 */
public class NotificationDAO {

    // CREATE -------------------------------------------------------------------------------
    public boolean createNotification(Notification n) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, target_link, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, n.getUserId());
            stmt.setString(2, n.getTitle());
            stmt.setString(3, n.getMessage());
            stmt.setString(4, n.getNotificationType());
            stmt.setString(5, n.getTargetLink());
            stmt.setBoolean(6, n.isRead());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NotificationDAO.createNotification error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin broadcast: copies one notification to every ACTIVE user (or only to one role).
     * Returns how many users received it.
     */
    public int broadcast(Notification template, String roleOrNull) {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, target_link) " +
                     "SELECT user_id, ?, ?, ?, ? FROM users WHERE status = 'ACTIVE'" +
                     (roleOrNull != null ? " AND role = ?" : "");
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, template.getTitle());
            stmt.setString(2, template.getMessage());
            stmt.setString(3, template.getNotificationType());
            stmt.setString(4, template.getTargetLink());
            if (roleOrNull != null) stmt.setString(5, roleOrNull);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("NotificationDAO.broadcast error: " + e.getMessage());
            return 0;
        }
    }

    // READ ---------------------------------------------------------------------------------
    public List<Notification> getNotificationsByUser(int userId, boolean unreadOnly) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ?" + (unreadOnly ? " AND is_read = FALSE" : "") +
                     " ORDER BY created_at DESC, notification_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("NotificationDAO.getNotificationsByUser error: " + e.getMessage());
        }
        return list;
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("NotificationDAO.getUnreadCount error: " + e.getMessage());
        }
        return 0;
    }

    /** Admin view: the most recent announcements that were broadcast (one row per message). */
    public List<Map<String, Object>> getRecentAnnouncements(int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT title, message, MIN(created_at) AS sent_at, COUNT(*) AS recipients, " +
                     "SUM(CASE WHEN is_read THEN 1 ELSE 0 END) AS read_count " +
                     "FROM notifications WHERE notification_type = 'ANNOUNCEMENT' " +
                     "GROUP BY title, message ORDER BY sent_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("title", rs.getString("title"));
                    row.put("message", rs.getString("message"));
                    row.put("sentAt", rs.getTimestamp("sent_at"));
                    row.put("recipients", rs.getInt("recipients"));
                    row.put("readCount", rs.getInt("read_count"));
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("NotificationDAO.getRecentAnnouncements error: " + e.getMessage());
        }
        return rows;
    }

    // UPDATE -------------------------------------------------------------------------------
    /** Marks one notification read/unread — only if it belongs to the user. */
    public boolean setReadStatus(int notificationId, int userId, boolean read) {
        String sql = "UPDATE notifications SET is_read = ? WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, read);
            stmt.setInt(2, notificationId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NotificationDAO.setReadStatus error: " + e.getMessage());
            return false;
        }
    }

    public int markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("NotificationDAO.markAllAsRead error: " + e.getMessage());
            return 0;
        }
    }

    // DELETE -------------------------------------------------------------------------------
    public boolean deleteNotification(int notificationId, int userId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("NotificationDAO.deleteNotification error: " + e.getMessage());
            return false;
        }
    }

    public int deleteAllRead(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ? AND is_read = TRUE";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("NotificationDAO.deleteAllRead error: " + e.getMessage());
            return 0;
        }
    }

    /** Admin: recall an announcement from every inbox. */
    public int deleteAnnouncement(String title, String message) {
        String sql = "DELETE FROM notifications WHERE notification_type = 'ANNOUNCEMENT' AND title = ? AND message = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, message);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("NotificationDAO.deleteAnnouncement error: " + e.getMessage());
            return 0;
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setNotificationType(rs.getString("notification_type"));
        n.setTargetLink(rs.getString("target_link"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}

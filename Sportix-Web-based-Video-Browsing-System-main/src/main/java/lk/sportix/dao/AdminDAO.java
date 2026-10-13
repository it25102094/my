package lk.sportix.dao;

import lk.sportix.model.AdminLog;
import lk.sportix.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDAO {

    public boolean updateVideoStatus(int videoId, String newStatus) {
        // Corrected to 'submission_status' based on schema
        String sql = "UPDATE videos SET submission_status = ? WHERE video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, videoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.updateVideoStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateReportStatus(int reportId, String newStatus, int resolvedByAdminId) {
        // Corrected to 'content_reports' table based on schema
        String sql = "UPDATE content_reports SET status = ?, resolved_by = ?, resolved_at = CURRENT_TIMESTAMP WHERE report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, resolvedByAdminId);
            stmt.setInt(3, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.updateReportStatus error: " + e.getMessage());
            return false;
        }
    }

    public void logAdminAction(AdminLog log) {
        String sql = "INSERT INTO admin_logs (admin_id, action_type, target_table, target_id, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, log.getAdminId());
            stmt.setString(2, log.getActionType());
            stmt.setString(3, log.getTargetTable());
            stmt.setInt(4, log.getTargetId());
            stmt.setString(5, log.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AdminDAO.logAdminAction error: " + e.getMessage());
        }
    }

    public Map<String, Integer> getSystemStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM users"); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) stats.put("totalUsers", rs.getInt("total"));
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM videos WHERE submission_status = 'APPROVED'"); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) stats.put("totalVideos", rs.getInt("total"));
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM content_reports WHERE status = 'PENDING'"); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) stats.put("pendingReports", rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getSystemStatistics error: " + e.getMessage());
        }
        return stats;
    }
    public List<Map<String, Object>> getVideoAnalyticsReport() {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT v.video_id, v.title, s.sport_name, v.video_type, v.submission_status, v.view_count, v.created_at " +
                "FROM videos v JOIN sports s ON v.sport_id = s.sport_id ORDER BY v.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("videoId", rs.getInt("video_id"));
                map.put("title", rs.getString("title"));
                map.put("sportName", rs.getString("sport_name"));
                map.put("videoType", rs.getString("video_type"));
                map.put("status", rs.getString("submission_status"));
                map.put("views", rs.getInt("view_count"));
                map.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(map);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getVideoAnalyticsReport error: " + e.getMessage());
        }
        return rows;
    }
}
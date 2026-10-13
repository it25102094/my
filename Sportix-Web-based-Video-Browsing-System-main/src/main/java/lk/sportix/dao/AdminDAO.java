package lk.sportix.dao;

import lk.sportix.model.AdminLog;
import lk.sportix.model.SavedReport;
import lk.sportix.model.User;
import lk.sportix.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminDAO — Member 6: Reporting, Analytics & Admin Management.
 * FIX: the pending-video / pending-report queries used to be raw SQL inside the servlet
 * (MVC violation). They live here now, together with the new user management,
 * saved report CRUD and admin activity log queries.
 */
public class AdminDAO {

    // ===================================================================================
    // MODERATION
    // ===================================================================================

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

    public boolean updateReportStatus(int reportId, String newStatus, int resolvedByAdminId, String notes) {
        // Corrected to 'content_reports' table based on schema
        String sql = "UPDATE content_reports SET status = ?, resolution_notes = ?, resolved_by = ?, resolved_at = CURRENT_TIMESTAMP WHERE report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.toUpperCase());
            if (notes == null || notes.isBlank()) stmt.setNull(2, Types.VARCHAR); else stmt.setString(2, notes.trim());
            stmt.setInt(3, resolvedByAdminId);
            stmt.setInt(4, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.updateReportStatus error: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getPendingVideos() {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT v.video_id, v.title, v.youtube_url, v.video_type, v.created_at, s.sport_name, " +
                "u.username AS uploader " +
                "FROM videos v JOIN sports s ON v.sport_id = s.sport_id JOIN users u ON v.uploaded_by = u.user_id " +
                "WHERE v.submission_status = 'PENDING' ORDER BY v.created_at ASC, v.video_id ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> v = new HashMap<>();
                v.put("videoId", rs.getInt("video_id"));
                v.put("title", rs.getString("title"));
                v.put("youtubeUrl", rs.getString("youtube_url"));
                v.put("videoType", rs.getString("video_type"));
                v.put("sportName", rs.getString("sport_name"));
                v.put("uploader", rs.getString("uploader"));
                v.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(v);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getPendingVideos error: " + e.getMessage());
        }
        return rows;
    }

    public List<Map<String, Object>> getPendingReports() {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT r.report_id, r.reason, r.created_at, r.video_id, v.title, u.username AS reporter " +
                "FROM content_reports r LEFT JOIN videos v ON r.video_id = v.video_id " +
                "JOIN users u ON r.reporter_id = u.user_id " +
                "WHERE r.status = 'PENDING' ORDER BY r.created_at ASC, r.report_id ASC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> rep = new HashMap<>();
                rep.put("reportId", rs.getInt("report_id"));
                rep.put("reason", rs.getString("reason"));
                rep.put("videoId", rs.getInt("video_id"));
                rep.put("videoTitle", rs.getString("title") != null ? rs.getString("title") : "Comment/User Report");
                rep.put("reporter", rs.getString("reporter"));
                rep.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(rep);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getPendingReports error: " + e.getMessage());
        }
        return rows;
    }

    /** Report details needed to notify the reporter: reporterId, videoId, videoTitle. */
    public Map<String, Object> getReportById(int reportId) {
        String sql = "SELECT r.report_id, r.reporter_id, r.video_id, v.title FROM content_reports r " +
                "LEFT JOIN videos v ON r.video_id = v.video_id WHERE r.report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("reporterId", rs.getInt("reporter_id"));
                    m.put("videoId", rs.getInt("video_id"));
                    m.put("videoTitle", rs.getString("title"));
                    return m;
                }
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getReportById error: " + e.getMessage());
        }
        return null;
    }

    /** Admin removes a video completely (its reports, comments, likes ... cascade). */
    public boolean deleteVideo(int videoId) {
        String sql = "DELETE FROM videos WHERE video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.deleteVideo error: " + e.getMessage());
            return false;
        }
    }

    // ===================================================================================
    // USER MANAGEMENT (admin side of Member 1's users table)
    // ===================================================================================

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, first_name, last_name, email, role, status, company_name, created_at " +
                "FROM users ORDER BY user_id";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                u.setStatus(rs.getString("status"));
                u.setCompanyName(rs.getString("company_name"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    public boolean updateUserRole(int userId, String role) {
        return updateUserColumn("UPDATE users SET role = ? WHERE user_id = ?", role, userId);
    }

    public boolean updateUserStatus(int userId, String status) {
        return updateUserColumn("UPDATE users SET status = ? WHERE user_id = ?", status, userId);
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.deleteUser error: " + e.getMessage());
            return false;
        }
    }

    private boolean updateUserColumn(String sql, String value, int userId) {
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.updateUserColumn error: " + e.getMessage());
            return false;
        }
    }

    // ===================================================================================
    // ADMIN LOGS
    // ===================================================================================

    public void logAdminAction(AdminLog log) {
        String sql = "INSERT INTO admin_logs (admin_id, action_type, target_table, target_id, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, log.getAdminId());
            stmt.setString(2, log.getActionType());
            stmt.setString(3, log.getTargetTable());
            if (log.getTargetId() > 0) stmt.setInt(4, log.getTargetId()); else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, log.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("AdminDAO.logAdminAction error: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getRecentLogs(int limit) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT l.log_id, l.action_type, l.target_table, l.target_id, l.description, l.created_at, u.username " +
                "FROM admin_logs l JOIN users u ON l.admin_id = u.user_id ORDER BY l.created_at DESC, l.log_id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("logId", rs.getInt("log_id"));
                    m.put("actionType", rs.getString("action_type"));
                    m.put("targetTable", rs.getString("target_table"));
                    m.put("targetId", rs.getObject("target_id"));
                    m.put("description", rs.getString("description"));
                    m.put("createdAt", rs.getTimestamp("created_at"));
                    m.put("admin", rs.getString("username"));
                    rows.add(m);
                }
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getRecentLogs error: " + e.getMessage());
        }
        return rows;
    }

    // ===================================================================================
    // STATISTICS & CSV
    // ===================================================================================

    public Map<String, Integer> getSystemStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String[][] queries = {
                {"totalUsers", "SELECT COUNT(*) FROM users"},
                {"totalVideos", "SELECT COUNT(*) FROM videos WHERE submission_status = 'APPROVED'"},
                {"pendingVideos", "SELECT COUNT(*) FROM videos WHERE submission_status = 'PENDING'"},
                {"pendingReports", "SELECT COUNT(*) FROM content_reports WHERE status = 'PENDING'"},
                {"totalComments", "SELECT COUNT(*) FROM comments"},
                {"totalViews", "SELECT COALESCE(SUM(view_count), 0) FROM videos"},
                {"activeCampaigns", "SELECT COUNT(*) FROM campaigns WHERE status = 'ACTIVE' AND CURRENT_DATE BETWEEN start_date AND end_date"},
                {"totalAds", "SELECT COUNT(*) FROM advertisements"},
                {"adClicks", "SELECT COALESCE(SUM(clicks), 0) FROM advertisements"},
                {"adImpressions", "SELECT COALESCE(SUM(impressions), 0) FROM advertisements"}
        };
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            for (String[] q : queries) {
                try (PreparedStatement stmt = conn.prepareStatement(q[1]); ResultSet rs = stmt.executeQuery()) {
                    stats.put(q[0], rs.next() ? rs.getInt(1) : 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getSystemStatistics error: " + e.getMessage());
        }
        return stats;
    }

    public List<Map<String, Object>> getVideoAnalyticsReport() {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT v.video_id, v.title, s.sport_name, v.video_type, v.submission_status, v.view_count, v.created_at, " +
                "(SELECT COUNT(*) FROM likes l WHERE l.video_id = v.video_id AND l.reaction_type = 'LIKE') AS like_count, " +
                "(SELECT COUNT(*) FROM comments c WHERE c.video_id = v.video_id) AS comment_count " +
                "FROM videos v JOIN sports s ON v.sport_id = s.sport_id ORDER BY v.created_at DESC, v.video_id DESC";
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
                map.put("likes", rs.getInt("like_count"));
                map.put("comments", rs.getInt("comment_count"));
                map.put("createdAt", rs.getTimestamp("created_at"));
                rows.add(map);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getVideoAnalyticsReport error: " + e.getMessage());
        }
        return rows;
    }

    // ===================================================================================
    // SAVED REPORTS CRUD (saved_reports table — was never used before)
    // ===================================================================================

    public boolean createSavedReport(SavedReport report) {
        String sql = "INSERT INTO saved_reports (admin_id, report_title, report_type, summary_content) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, report.getAdminId());
            stmt.setString(2, report.getReportTitle());
            stmt.setString(3, report.getReportType());
            stmt.setString(4, report.getSummaryContent());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.createSavedReport error: " + e.getMessage());
            return false;
        }
    }

    public List<SavedReport> getSavedReports() {
        List<SavedReport> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM saved_reports r JOIN users u ON r.admin_id = u.user_id " +
                "ORDER BY r.created_at DESC, r.saved_report_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SavedReport r = new SavedReport();
                r.setSavedReportId(rs.getInt("saved_report_id"));
                r.setAdminId(rs.getInt("admin_id"));
                r.setAdminName(rs.getString("username"));
                r.setReportTitle(rs.getString("report_title"));
                r.setReportType(rs.getString("report_type"));
                r.setSummaryContent(rs.getString("summary_content"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("AdminDAO.getSavedReports error: " + e.getMessage());
        }
        return list;
    }

    public boolean renameSavedReport(int reportId, String newTitle) {
        String sql = "UPDATE saved_reports SET report_title = ? WHERE saved_report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newTitle);
            stmt.setInt(2, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.renameSavedReport error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSavedReport(int reportId) {
        String sql = "DELETE FROM saved_reports WHERE saved_report_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdminDAO.deleteSavedReport error: " + e.getMessage());
            return false;
        }
    }
}

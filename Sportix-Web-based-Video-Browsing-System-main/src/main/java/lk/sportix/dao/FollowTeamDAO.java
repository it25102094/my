package lk.sportix.dao;

import lk.sportix.model.FollowTeam;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified DAO for 'follow_teams' table CRUD operations.
 * Owned by Member 1 (IT25102095).
 */
public class FollowTeamDAO {

    // CREATE: Follow a team
    public boolean followTeam(int userId, int teamId, boolean notifyOnUpload) {
        String sql = "INSERT IGNORE INTO follow_teams (user_id, team_id, notify_on_upload) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, teamId);
            ps.setBoolean(3, notifyOnUpload);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.followTeam error: " + e.getMessage());
            return false;
        }
    }

    // READ: Get only the teams followed by this user
    public List<FollowTeam> getFollowedTeamsByUser(int userId) {
        List<FollowTeam> list = new ArrayList<>();
        String sql = "SELECT ft.follow_id, ft.user_id, ft.team_id, ft.notify_on_upload, " +
                "t.team_name, t.country, s.sport_name " +
                "FROM follow_teams ft " +
                "JOIN teams t ON ft.team_id = t.team_id " +
                "JOIN sports s ON t.sport_id = s.sport_id " +
                "WHERE ft.user_id = ? " +
                "ORDER BY s.sport_name ASC, t.team_name ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FollowTeam ft = new FollowTeam();
                    ft.setFollowId(rs.getInt("follow_id"));
                    ft.setUserId(rs.getInt("user_id"));
                    ft.setTeamId(rs.getInt("team_id"));
                    ft.setNotifyOnUpload(rs.getBoolean("notify_on_upload"));
                    ft.setTeamName(rs.getString("team_name"));
                    ft.setCountry(rs.getString("country"));
                    ft.setSportName(rs.getString("sport_name"));
                    ft.setFollowedByCurrentUser(true);
                    list.add(ft);
                }
            }
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.getFollowedTeamsByUser error: " + e.getMessage());
        }
        return list;
    }

    // READ: Get all teams and mark which ones this user follows
    public List<FollowTeam> getAllTeamsWithFollowStatus(int userId) {
        List<FollowTeam> list = new ArrayList<>();
        String sql = "SELECT t.team_id, t.team_name, t.country, s.sport_name, " +
                "ft.follow_id, ft.notify_on_upload " +
                "FROM teams t " +
                "JOIN sports s ON t.sport_id = s.sport_id " +
                "LEFT JOIN follow_teams ft ON t.team_id = ft.team_id AND ft.user_id = ? " +
                "ORDER BY s.sport_name ASC, t.team_name ASC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FollowTeam ft = new FollowTeam();
                    ft.setTeamId(rs.getInt("team_id"));
                    ft.setTeamName(rs.getString("team_name"));
                    ft.setCountry(rs.getString("country"));
                    ft.setSportName(rs.getString("sport_name"));

                    int followId = rs.getInt("follow_id");
                    if (!rs.wasNull()) {
                        ft.setFollowId(followId);
                        ft.setUserId(userId);
                        ft.setNotifyOnUpload(rs.getBoolean("notify_on_upload"));
                        ft.setFollowedByCurrentUser(true);
                    } else {
                        ft.setFollowedByCurrentUser(false);
                    }
                    list.add(ft);
                }
            }
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.getAllTeamsWithFollowStatus error: " + e.getMessage());
        }
        return list;
    }

    // UPDATE: Toggle upload notifications ON or OFF for a followed team
    public boolean updateNotificationPreference(int userId, int teamId, boolean notifyOnUpload) {
        String sql = "UPDATE follow_teams SET notify_on_upload = ? WHERE user_id = ? AND team_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, notifyOnUpload);
            ps.setInt(2, userId);
            ps.setInt(3, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.updateNotificationPreference error: " + e.getMessage());
            return false;
        }
    }

    /**
     * READ (used by the Observer pattern - Member 5): every ACTIVE user who follows at least one
     * team of the given video AND has upload alerts switched on. The uploader is excluded
     * (no alert for your own video). One row per (user, team) - duplicates per user are
     * removed by UserObserver.equals().
     */
    public List<FollowTeam> getFollowersToNotify(int videoId) {
        List<FollowTeam> list = new ArrayList<>();
        String sql = "SELECT ft.user_id, ft.team_id, t.team_name " +
                "FROM follow_teams ft " +
                "JOIN video_teams vt ON vt.team_id = ft.team_id " +
                "JOIN teams t ON t.team_id = ft.team_id " +
                "JOIN users u ON u.user_id = ft.user_id " +
                "JOIN videos v ON v.video_id = vt.video_id " +
                "WHERE vt.video_id = ? AND ft.notify_on_upload = TRUE AND u.status = 'ACTIVE' " +
                "AND ft.user_id <> v.uploaded_by " +
                "ORDER BY ft.user_id, t.team_name";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, videoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    FollowTeam ft = new FollowTeam();
                    ft.setUserId(rs.getInt("user_id"));
                    ft.setTeamId(rs.getInt("team_id"));
                    ft.setTeamName(rs.getString("team_name"));
                    ft.setNotifyOnUpload(true);
                    list.add(ft);
                }
            }
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.getFollowersToNotify error: " + e.getMessage());
        }
        return list;
    }

    // DELETE: Unfollow a team
    public boolean unfollowTeam(int userId, int teamId) {
        String sql = "DELETE FROM follow_teams WHERE user_id = ? AND team_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, teamId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FollowTeamDAO.unfollowTeam error: " + e.getMessage());
            return false;
        }
    }
}
package lk.sportix.dao;

import lk.sportix.model.WatchLater;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified DAO for Watch Later folders and saved videos using a single WatchLater model.
 * Owned by Member 2 (IT25102096).
 */
public class WatchLaterDAO {

    // =========================================================================
    // 1. FOLDERS CRUD (watch_later_folders table)
    // =========================================================================

    // CREATE Folder
    public boolean createFolder(WatchLater folder) {
        String sql = "INSERT INTO watch_later_folders (user_id, folder_name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, folder.getUserId());
            ps.setString(2, folder.getFolderName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.createFolder error: " + e.getMessage());
            return false;
        }
    }

    // READ Folders by User
    public List<WatchLater> getFoldersByUser(int userId) {
        List<WatchLater> list = new ArrayList<>();
        String sql = "SELECT f.folder_id, f.user_id, f.folder_name, COUNT(i.item_id) AS item_count " +
                "FROM watch_later_folders f " +
                "LEFT JOIN watch_later_items i ON f.folder_id = i.folder_id " +
                "WHERE f.user_id = ? " +
                "GROUP BY f.folder_id, f.user_id, f.folder_name " +
                "ORDER BY f.folder_id DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WatchLater wl = new WatchLater();
                    wl.setFolderId(rs.getInt("folder_id"));
                    wl.setUserId(rs.getInt("user_id"));
                    wl.setFolderName(rs.getString("folder_name"));
                    wl.setItemCount(rs.getInt("item_count"));
                    list.add(wl);
                }
            }
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.getFoldersByUser error: " + e.getMessage());
        }
        return list;
    }

    // UPDATE Folder Name
    public boolean renameFolder(int folderId, int userId, String newName) {
        String sql = "UPDATE watch_later_folders SET folder_name = ? WHERE folder_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setInt(2, folderId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.renameFolder error: " + e.getMessage());
            return false;
        }
    }

    // DELETE Folder
    public boolean deleteFolder(int folderId, int userId) {
        String sql = "DELETE FROM watch_later_folders WHERE folder_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, folderId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.deleteFolder error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 2. SAVED VIDEOS CRUD (watch_later_items table)
    // =========================================================================

    // CREATE Saved Video Item
    public boolean addWatchLaterItem(WatchLater item) {
        String sql = "INSERT IGNORE INTO watch_later_items (user_id, video_id, folder_id, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getUserId());
            ps.setInt(2, item.getVideoId());
            if (item.getFolderId() != null && item.getFolderId() > 0) {
                ps.setInt(3, item.getFolderId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, item.getNotes());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.addWatchLaterItem error: " + e.getMessage());
            return false;
        }
    }

    // READ Saved Videos by User (optionally filtered by folderId)
    public List<WatchLater> getItemsByUser(int userId, Integer folderIdFilter) {
        List<WatchLater> list = new ArrayList<>();
        String sql = "SELECT w.item_id, w.user_id, w.video_id, w.folder_id, w.notes, " +
                "v.title AS video_title, v.thumbnail_url, v.is_live_now, " +
                "s.sport_name, f.folder_name " +
                "FROM watch_later_items w " +
                "JOIN videos v ON w.video_id = v.video_id " +
                "JOIN sports s ON v.sport_id = s.sport_id " +
                "LEFT JOIN watch_later_folders f ON w.folder_id = f.folder_id " +
                "WHERE w.user_id = ? " +
                (folderIdFilter != null && folderIdFilter > 0 ? "AND w.folder_id = ? " : "") +
                "ORDER BY w.item_id DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            if (folderIdFilter != null && folderIdFilter > 0) {
                ps.setInt(2, folderIdFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WatchLater wl = new WatchLater();
                    wl.setItemId(rs.getInt("item_id"));
                    wl.setUserId(rs.getInt("user_id"));
                    wl.setVideoId(rs.getInt("video_id"));
                    int fId = rs.getInt("folder_id");
                    wl.setFolderId(rs.wasNull() ? null : fId);
                    wl.setNotes(rs.getString("notes"));
                    wl.setVideoTitle(rs.getString("video_title"));
                    wl.setThumbnailUrl(rs.getString("thumbnail_url"));
                    wl.setLive(rs.getBoolean("is_live_now"));
                    wl.setSportName(rs.getString("sport_name"));
                    wl.setFolderName(rs.getString("folder_name"));
                    list.add(wl);
                }
            }
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.getItemsByUser error: " + e.getMessage());
        }
        return list;
    }

    // UPDATE Saved Video Folder or Notes
    public boolean updateWatchLaterItem(int itemId, int userId, Integer folderId, String notes) {
        String sql = "UPDATE watch_later_items SET folder_id = ?, notes = ? WHERE item_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (folderId != null && folderId > 0) {
                ps.setInt(1, folderId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, notes);
            ps.setInt(3, itemId);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.updateWatchLaterItem error: " + e.getMessage());
            return false;
        }
    }

    // DELETE Saved Video Item
    public boolean removeWatchLaterItem(int itemId, int userId) {
        String sql = "DELETE FROM watch_later_items WHERE item_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.removeWatchLaterItem error: " + e.getMessage());
            return false;
        }
    }

    public boolean removeByUserAndVideo(int userId, int videoId) {
        String sql = "DELETE FROM watch_later_items WHERE user_id = ? AND video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, videoId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("WatchLaterDAO.removeByUserAndVideo error: " + e.getMessage());
            return false;
        }
    }
}
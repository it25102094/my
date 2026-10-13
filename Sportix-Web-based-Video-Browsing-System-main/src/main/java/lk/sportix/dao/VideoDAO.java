package lk.sportix.dao;

import lk.sportix.model.Team;
import lk.sportix.model.Video;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoDAO {
    // ===================================================================================
    // MEMBER 2'S METHODS (Browsing)
    // ===================================================================================

    public Video getFeaturedVideo() {
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.is_featured = TRUE AND CURRENT_DATE BETWEEN v.feature_start AND v.feature_end AND v.submission_status = 'APPROVED' ORDER BY v.view_count DESC LIMIT 1";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return mapResultSetToVideo(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Video> getTrendingVideos(int limit) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.submission_status = 'APPROVED' ORDER BY v.view_count DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return videos;
    }

    public List<Video> getVideosByType(String videoType, int limit) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.submission_status = 'APPROVED' AND v.video_type = ? ORDER BY v.video_id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoType);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return videos;
    }

    public Video getVideoById(int videoId) {
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.video_id = ? AND v.submission_status = 'APPROVED'";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return mapResultSetToVideo(rs); }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Video> searchVideos(String keyword) {
        List<Video> videos = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty() || "All Sports".equalsIgnoreCase(keyword.trim())) {
            return getTrendingVideos(20);
        }
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.submission_status = 'APPROVED' AND (v.title LIKE ? OR v.description LIKE ? OR s.sport_name LIKE ?) ORDER BY v.view_count DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return videos;
    }

    public void incrementViewCount(int videoId) {
        String sql = "UPDATE videos SET view_count = view_count + 1 WHERE video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Team> getTeamsForVideo(int videoId) {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT t.team_id, t.team_name, t.sport_id FROM teams t JOIN video_teams vt ON t.team_id = vt.team_id WHERE vt.video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) teams.add(new Team(rs.getInt("team_id"), rs.getString("team_name"), rs.getInt("sport_id"))); }
        } catch (SQLException e) { e.printStackTrace(); }
        return teams;
    }

    public List<Video> getVideosBySportName(String sportName) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.submission_status = 'APPROVED' AND LOWER(s.sport_name) = LOWER(?) ORDER BY v.view_count DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sportName.trim());
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return videos;
    }

    // ===================================================================================
    // MEMBER 3'S METHODS (Content Management CRUD)
    // ===================================================================================

    // it25102093 - Content Management CRUD (Create)
    public boolean addVideo(Video video, String videoType, List<Integer> teamIds, int uploadedBy) {
        String insertVideoSQL = "INSERT INTO videos (title, description, youtube_url, youtube_video_id, thumbnail_url, video_type, is_live_now, submission_status, sport_id, category_id, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?, 2, ?)";
        String insertTeamSQL = "INSERT INTO video_teams (video_id, team_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            String ytId = extractYoutubeId(video.getYouTubeURL());
            String thumbUrl = "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg";

            int generatedVideoId;
            try (PreparedStatement stmt1 = conn.prepareStatement(insertVideoSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt1.setString(1, video.getTitle());
                stmt1.setString(2, video.getDescription());
                stmt1.setString(3, video.getYouTubeURL());
                stmt1.setString(4, ytId);
                stmt1.setString(5, thumbUrl);
                stmt1.setString(6, videoType);
                stmt1.setBoolean(7, "LIVE".equalsIgnoreCase(videoType));
                stmt1.setInt(8, video.getSportId());
                stmt1.setInt(9, uploadedBy);
                stmt1.executeUpdate();

                try (ResultSet rs = stmt1.getGeneratedKeys()) {
                    if (rs.next()) generatedVideoId = rs.getInt(1);
                    else throw new SQLException("Failed to retrieve generated video_id.");
                }
            }

            if (teamIds != null && !teamIds.isEmpty()) {
                try (PreparedStatement stmt2 = conn.prepareStatement(insertTeamSQL)) {
                    for (int teamId : teamIds) {
                        stmt2.setInt(1, generatedVideoId);
                        stmt2.setInt(2, teamId);
                        stmt2.addBatch(); // Batch processing
                    }
                    stmt2.executeBatch();
                }
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Database error during addVideo: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // it25102093 - Content Management CRUD (Read)
    public List<Video> getVideosByUserId(int userId) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.uploaded_by = ? ORDER BY v.created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) {
            System.err.println("Database error fetching user videos: " + e.getMessage());
        }
        return videos;
    }

    // it25102093 - Content Management CRUD (Read Single)
    public Video getVideoByIdAndUser(int videoId, int userId) {
        String sql = "SELECT v.*, s.sport_name FROM videos v JOIN sports s ON v.sport_id = s.sport_id WHERE v.video_id = ? AND v.uploaded_by = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return mapResultSetToVideo(rs); }
        } catch (SQLException e) {
            System.err.println("Database error fetching video by ID and User: " + e.getMessage());
        }
        return null;
    }

    // it25102093 - Content Management CRUD (Update)
    public boolean updateVideo(Video video, String videoType, int userId) {
        String sql = "UPDATE videos SET title=?, description=?, youtube_url=?, youtube_video_id=?, thumbnail_url=?, video_type=?, is_live_now=?, sport_id=?, category_id=? WHERE video_id=? AND uploaded_by=?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, video.getTitle());
            pst.setString(2, video.getDescription());
            pst.setString(3, video.getYouTubeURL());
            pst.setString(4, extractYoutubeId(video.getYouTubeURL()));
            pst.setString(5, "https://img.youtube.com/vi/" + extractYoutubeId(video.getYouTubeURL()) + "/hqdefault.jpg");
            pst.setString(6, videoType);
            pst.setBoolean(7, "LIVE".equalsIgnoreCase(videoType));
            pst.setInt(8, video.getSportId());
            pst.setInt(9, 2);
            pst.setInt(10, video.getVideoId());
            pst.setInt(11, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error during updateVideo: " + e.getMessage());
            return false;
        }
    }

    // it25102093 - Content Management CRUD (Delete)
    public boolean deleteVideo(int videoId, int userId) {
        String sql = "DELETE FROM videos WHERE video_id = ? AND uploaded_by = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, videoId);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Database error during deleteVideo: " + e.getMessage());
            return false;
        }
    }

    // ===================================================================================
    // HELPER MAPPING METHODS
    // ===================================================================================

    private Video mapResultSetToVideo(ResultSet rs) throws SQLException {
        Video video = new Video();
        video.setVideoId(rs.getInt("video_id"));
        video.setTitle(rs.getString("title"));
        video.setDescription(rs.getString("description"));
        video.setYouTubeURL("https://www.youtube.com/embed/" + rs.getString("youtube_video_id"));
        video.setViewCount(rs.getInt("view_count"));
        video.setStatus(rs.getString("submission_status"));
        video.setSportId(rs.getInt("sport_id"));
        video.setSportName(rs.getString("sport_name"));
        video.setLive(rs.getBoolean("is_live_now") || "LIVE".equalsIgnoreCase(rs.getString("video_type")));
        return video;
    }

    private String extractYoutubeId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }

        if (url.contains("v=")) {
            String id = url.split("v=")[1];
            return id.contains("&") ? id.substring(0, id.indexOf("&")) : id;
        } else if (url.contains("youtu.be/")) {
            String id = url.split("youtu.be/")[1];
            return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;
        } else if (url.contains("/embed/")) {
            String id = url.split("/embed/")[1];
            return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;
        } else if (url.contains("/shorts/")) {
            String id = url.split("/shorts/")[1];
            return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;
        } else if (url.contains("/live/")) {
            String id = url.split("/live/")[1];
            return id.contains("?") ? id.substring(0, id.indexOf("?")) : id;
        }

        // If the user pastes just the 11-character ID itself by mistake
        if (url.length() == 11) {
            return url;
        }

        return ""; // Returns empty instead of a hardcoded video
    }
}
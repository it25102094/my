package lk.sportix.dao;

import lk.sportix.model.Category;
import lk.sportix.model.Sport;
import lk.sportix.model.Team;
import lk.sportix.model.Video;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VideoDAO {

    /** Common SELECT with the JOINed display columns (sport, category, uploader). */
    private static final String BASE_SELECT =
            "SELECT v.*, s.sport_name, c.category_name, u.first_name AS uploader_first, u.last_name AS uploader_last " +
            "FROM videos v " +
            "JOIN sports s ON v.sport_id = s.sport_id " +
            "JOIN categories c ON v.category_id = c.category_id " +
            "JOIN users u ON v.uploaded_by = u.user_id ";

    // ===================================================================================
    // MEMBER 2'S METHODS (Browsing)
    // ===================================================================================

    public Video getFeaturedVideo() {
        String sql = BASE_SELECT + "WHERE v.is_featured = TRUE AND CURRENT_DATE BETWEEN v.feature_start AND v.feature_end " +
                "AND v.submission_status = 'APPROVED' ORDER BY v.view_count DESC LIMIT 1";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return mapResultSetToVideo(rs);
        } catch (SQLException e) { System.err.println("VideoDAO.getFeaturedVideo error: " + e.getMessage()); }
        return null;
    }

    public List<Video> getTrendingVideos(int limit) {
        List<Video> videos = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE v.submission_status = 'APPROVED' ORDER BY v.view_count DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { System.err.println("VideoDAO.getTrendingVideos error: " + e.getMessage()); }
        return videos;
    }

    public List<Video> getVideosByType(String videoType, int limit) {
        List<Video> videos = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE v.submission_status = 'APPROVED' AND v.video_type = ? ORDER BY v.created_at DESC, v.video_id DESC LIMIT ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, videoType);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { System.err.println("VideoDAO.getVideosByType error: " + e.getMessage()); }
        return videos;
    }

    /** Public read: only APPROVED videos are visible to everyone. */
    public Video getVideoById(int videoId) {
        return findOne(BASE_SELECT + "WHERE v.video_id = ? AND v.submission_status = 'APPROVED'", videoId);
    }

    /** Read regardless of status (owner preview, admin moderation, notifications). */
    public Video getVideoByIdAnyStatus(int videoId) {
        return findOne(BASE_SELECT + "WHERE v.video_id = ?", videoId);
    }

    public List<Video> searchVideos(String keyword) {
        List<Video> videos = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty() || "All Sports".equalsIgnoreCase(keyword.trim())) {
            return getTrendingVideos(20);
        }
        String sql = BASE_SELECT + "WHERE v.submission_status = 'APPROVED' AND (v.title LIKE ? OR v.description LIKE ? OR s.sport_name LIKE ? " +
                "OR c.category_name LIKE ? OR EXISTS (SELECT 1 FROM video_teams vt JOIN teams t ON vt.team_id = t.team_id " +
                "WHERE vt.video_id = v.video_id AND t.team_name LIKE ?)) ORDER BY v.view_count DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            for (int i = 1; i <= 5; i++) stmt.setString(i, pattern);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { System.err.println("VideoDAO.searchVideos error: " + e.getMessage()); }
        return videos;
    }

    public void incrementViewCount(int videoId) {
        String sql = "UPDATE videos SET view_count = view_count + 1 WHERE video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.executeUpdate();
        } catch (SQLException e) { System.err.println("VideoDAO.incrementViewCount error: " + e.getMessage()); }
    }

    public List<Team> getTeamsForVideo(int videoId) {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT t.team_id, t.team_name, t.sport_id FROM teams t JOIN video_teams vt ON t.team_id = vt.team_id WHERE vt.video_id = ? ORDER BY t.team_name";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) teams.add(new Team(rs.getInt("team_id"), rs.getString("team_name"), rs.getInt("sport_id"))); }
        } catch (SQLException e) { System.err.println("VideoDAO.getTeamsForVideo error: " + e.getMessage()); }
        return teams;
    }

    public List<Video> getVideosBySportName(String sportName) {
        List<Video> videos = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE v.submission_status = 'APPROVED' AND LOWER(s.sport_name) = LOWER(?) ORDER BY v.view_count DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sportName.trim());
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) { System.err.println("VideoDAO.getVideosBySportName error: " + e.getMessage()); }
        return videos;
    }

    // ===================================================================================
    // LOOKUP DATA (drop-down lists for the upload / edit forms)
    // ===================================================================================

    public List<Sport> getAllSports() {
        List<Sport> list = new ArrayList<>();
        String sql = "SELECT sport_id, sport_name FROM sports ORDER BY sport_name";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(new Sport(rs.getInt("sport_id"), rs.getString("sport_name")));
        } catch (SQLException e) { System.err.println("VideoDAO.getAllSports error: " + e.getMessage()); }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description FROM categories ORDER BY category_id";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(new Category(rs.getInt("category_id"), rs.getString("category_name"), rs.getString("description")));
        } catch (SQLException e) { System.err.println("VideoDAO.getAllCategories error: " + e.getMessage()); }
        return list;
    }

    public List<Team> getAllTeams() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT team_id, team_name, sport_id FROM teams ORDER BY team_name";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(new Team(rs.getInt("team_id"), rs.getString("team_name"), rs.getInt("sport_id")));
        } catch (SQLException e) { System.err.println("VideoDAO.getAllTeams error: " + e.getMessage()); }
        return list;
    }

    // ===================================================================================
    // MEMBER 3'S METHODS (Content Management CRUD)
    // ===================================================================================

    /**
     * it25102093 - Content Management CRUD (Create).
     * Inserts the video and its team links in ONE transaction and returns the new video_id (or -1).
     * FIX: category_id was hard-coded to 2 and the selected teams were never saved.
     */
    public int addVideo(Video video, int uploadedBy, String submissionStatus) {
        String insertVideoSQL = "INSERT INTO videos (title, description, youtube_url, youtube_video_id, thumbnail_url, video_type, " +
                "match_score_text, is_live_now, submission_status, sport_id, category_id, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            String ytId = Video.extractVideoId(video.getSourceUrl());
            int generatedVideoId;
            try (PreparedStatement stmt1 = conn.prepareStatement(insertVideoSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmt1.setString(1, video.getTitle());
                stmt1.setString(2, video.getDescription());
                stmt1.setString(3, video.getSourceUrl());
                stmt1.setString(4, ytId);
                stmt1.setString(5, "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg");
                stmt1.setString(6, video.getVideoType());
                stmt1.setString(7, video.getMatchScoreText());
                stmt1.setBoolean(8, "LIVE".equalsIgnoreCase(video.getVideoType()));
                stmt1.setString(9, submissionStatus);
                stmt1.setInt(10, video.getSportId());
                stmt1.setInt(11, video.getCategoryId());
                stmt1.setInt(12, uploadedBy);
                stmt1.executeUpdate();

                try (ResultSet rs = stmt1.getGeneratedKeys()) {
                    if (rs.next()) generatedVideoId = rs.getInt(1);
                    else throw new SQLException("Failed to retrieve generated video_id.");
                }
            }

            insertTeams(conn, generatedVideoId, video.getTeamIds());
            conn.commit(); // Commit Transaction
            video.setVideoId(generatedVideoId);
            return generatedVideoId;

        } catch (SQLException e) {
            System.err.println("Database error during addVideo: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            }
            return -1;
        } finally {
            closeQuietly(conn);
        }
    }

    // it25102093 - Content Management CRUD (Read)
    public List<Video> getVideosByUserId(int userId) {
        List<Video> videos = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE v.uploaded_by = ? ORDER BY v.created_at DESC, v.video_id DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) videos.add(mapResultSetToVideo(rs)); }
        } catch (SQLException e) {
            System.err.println("Database error fetching user videos: " + e.getMessage());
        }
        return videos;
    }

    // it25102093 - Content Management CRUD (Read Single, owner only - any status)
    public Video getVideoByIdAndUser(int videoId, int userId) {
        String sql = BASE_SELECT + "WHERE v.video_id = ? AND v.uploaded_by = ?";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Video v = mapResultSetToVideo(rs);
                    for (Team t : getTeamsForVideo(videoId)) v.getTeamIds().add(t.getTeamId());
                    return v;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching video by ID and User: " + e.getMessage());
        }
        return null;
    }

    /**
     * it25102093 - Content Management CRUD (Update).
     * Updates the video row (owner only) and replaces its team links in one transaction.
     * FIX: category_id was overwritten with 2 and video_type lost SHORT / INTERVIEW.
     */
    public boolean updateVideo(Video video, int userId, String newStatus) {
        String sql = "UPDATE videos SET title=?, description=?, youtube_url=?, youtube_video_id=?, thumbnail_url=?, video_type=?, " +
                "match_score_text=?, is_live_now=?, sport_id=?, category_id=?, submission_status=? WHERE video_id=? AND uploaded_by=?";
        Connection conn = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            String ytId = Video.extractVideoId(video.getSourceUrl());
            int rows;
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, video.getTitle());
                pst.setString(2, video.getDescription());
                pst.setString(3, video.getSourceUrl());
                pst.setString(4, ytId);
                pst.setString(5, "https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg");
                pst.setString(6, video.getVideoType());
                pst.setString(7, video.getMatchScoreText());
                pst.setBoolean(8, "LIVE".equalsIgnoreCase(video.getVideoType()));
                pst.setInt(9, video.getSportId());
                pst.setInt(10, video.getCategoryId());
                pst.setString(11, newStatus);
                pst.setInt(12, video.getVideoId());
                pst.setInt(13, userId);
                rows = pst.executeUpdate();
            }
            if (rows == 0) {
                conn.rollback();
                return false;
            }
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM video_teams WHERE video_id = ?")) {
                del.setInt(1, video.getVideoId());
                del.executeUpdate();
            }
            insertTeams(conn, video.getVideoId(), video.getTeamIds());
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Database error during updateVideo: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            }
            return false;
        } finally {
            closeQuietly(conn);
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

    private void insertTeams(Connection conn, int videoId, List<Integer> teamIds) throws SQLException {
        if (teamIds == null || teamIds.isEmpty()) return;
        try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO video_teams (video_id, team_id) VALUES (?, ?)")) {
            for (int teamId : teamIds) {
                stmt2.setInt(1, videoId);
                stmt2.setInt(2, teamId);
                stmt2.addBatch(); // Batch processing
            }
            stmt2.executeBatch();
        }
    }

    private Video findOne(String sql, int videoId) {
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return mapResultSetToVideo(rs); }
        } catch (SQLException e) { System.err.println("VideoDAO.findOne error: " + e.getMessage()); }
        return null;
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) { System.err.println("VideoDAO close error: " + e.getMessage()); }
        }
    }

    /** FIX: now maps every column the JSPs and strategies need (type, category, thumbnail, created_at ...). */
    private Video mapResultSetToVideo(ResultSet rs) throws SQLException {
        Video video = new Video();
        video.setVideoId(rs.getInt("video_id"));
        video.setTitle(rs.getString("title"));
        video.setDescription(rs.getString("description"));
        video.setYoutubeVideoId(rs.getString("youtube_video_id"));
        video.setYouTubeURL("https://www.youtube.com/embed/" + rs.getString("youtube_video_id"));
        video.setSourceUrl(rs.getString("youtube_url"));
        video.setThumbnail(rs.getString("thumbnail_url"));
        video.setViewCount(rs.getInt("view_count"));
        video.setStatus(rs.getString("submission_status"));
        video.setSportId(rs.getInt("sport_id"));
        video.setSportName(rs.getString("sport_name"));
        video.setCategoryId(rs.getInt("category_id"));
        video.setCategoryName(rs.getString("category_name"));
        video.setVideoType(rs.getString("video_type"));
        video.setMatchScoreText(rs.getString("match_score_text"));
        video.setFeatured(rs.getBoolean("is_featured"));
        video.setUploadedBy(rs.getInt("uploaded_by"));
        video.setUploaderName((rs.getString("uploader_first") + " " + rs.getString("uploader_last")).trim());
        video.setCreatedAt(rs.getTimestamp("created_at"));
        video.setLive(rs.getBoolean("is_live_now") || "LIVE".equalsIgnoreCase(rs.getString("video_type")));
        return video;
    }
}

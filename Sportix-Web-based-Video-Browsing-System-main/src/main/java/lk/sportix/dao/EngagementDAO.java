package lk.sportix.dao;

import lk.sportix.model.Comment;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EngagementDAO — Handles interactions for comments, likes, and reports.
 */
public class EngagementDAO {

    /**
     * Inserts a new comment into the database.
     */
    public boolean addComment(int videoId, int userId, String text) {
        String sql = "INSERT INTO comments (video_id, user_id, comment_text) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            stmt.setString(3, text.trim());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.addComment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all comments for a specific video, joining the users table to get usernames.
     */
    public List<Comment> getCommentsByVideoId(int videoId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.video_id, c.user_id, c.comment_text, c.created_at, u.username " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.user_id " +
                "WHERE c.video_id = ? " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, videoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(rs.getInt("comment_id"));
                    comment.setVideoId(rs.getInt("video_id"));
                    comment.setUserId(rs.getInt("user_id"));
                    comment.setUsername(rs.getString("username"));
                    comment.setCommentText(rs.getString("comment_text"));
                    comment.setCreatedAt(rs.getTimestamp("created_at"));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getCommentsByVideoId error: " + e.getMessage());
        }
        return comments;
    }

    /**
     * Deletes a comment, strictly verifying that the requesting user is the owner.
     */
    public boolean deleteComment(int commentId, int userId) {
        String sql = "DELETE FROM comments WHERE comment_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.deleteComment error: " + e.getMessage());
            return false;
        }
    }
    // ===================================================================================
    // REACTION (LIKE/DISLIKE) METHODS
    // ===================================================================================

    /**
     * Adds or updates a user's reaction (LIKE or DISLIKE) on a video.
     */
    public boolean addOrUpdateReaction(int videoId, int userId, String reactionType) {
        // Uses MySQL UPSERT (ON DUPLICATE KEY UPDATE) if your table has a composite unique key,
        // or a standard REPLACE/UPDATE fallback. Assuming standard schema with unique (video_id, user_id).
        String sql = "INSERT INTO video_reactions (video_id, user_id, reaction_type) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reaction_type = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            stmt.setString(3, reactionType);
            stmt.setString(4, reactionType);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.addOrUpdateReaction error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a reaction (if a user clicks 'Like' again to unlike it).
     */
    public boolean removeReaction(int videoId, int userId) {
        String sql = "DELETE FROM video_reactions WHERE video_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.removeReaction error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the total count of a specific reaction type (LIKE or DISLIKE) for a video.
     */
    public int getReactionCount(int videoId, String reactionType) {
        String sql = "SELECT COUNT(*) FROM video_reactions WHERE video_id = ? AND reaction_type = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setString(2, reactionType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getReactionCount error: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a specific user has liked or disliked a video (returns "LIKE", "DISLIKE", or null).
     */
    public String getUserReaction(int videoId, int userId) {
        String sql = "SELECT reaction_type FROM video_reactions WHERE video_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("reaction_type");
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getUserReaction error: " + e.getMessage());
        }
        return null;
    }
    // ===================================================================================
    // CONTENT REPORTING METHODS
    //IT25102094-Dilshan
    // ===================================================================================

    public boolean addReport(int videoId, int userId, String reason) {
        String sql = "INSERT INTO video_reports (video_id, user_id, reason) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            stmt.setString(3, reason.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.addReport error: " + e.getMessage());
            return false;
        }
    }
}
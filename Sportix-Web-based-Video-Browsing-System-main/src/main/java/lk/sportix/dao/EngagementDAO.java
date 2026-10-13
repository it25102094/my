package lk.sportix.dao;

import lk.sportix.model.Comment;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EngagementDAO — Handles interactions for comments, likes/dislikes, ratings and reports (Member 4).
 *
 * FIXES:
 *  - Reactions used a table "video_reactions" that does not exist -> now uses "likes"
 *    (reaction_type column added in the schema), so Like / Dislike works.
 *  - Reports used "video_reports(user_id)" -> the real table is "content_reports(reporter_id)".
 *  - Added comment UPDATE (edit) and full ratings CRUD (the ratings table was never used).
 */
public class EngagementDAO {

    // ===================================================================================
    // COMMENTS (Create / Read / Update / Delete)
    // ===================================================================================

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
        String sql = "SELECT c.comment_id, c.video_id, c.user_id, c.comment_text, c.created_at, c.updated_at, u.username " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.user_id " +
                "WHERE c.video_id = ? " +
                "ORDER BY c.created_at DESC, c.comment_id DESC";

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
                    Timestamp created = rs.getTimestamp("created_at");
                    Timestamp updated = rs.getTimestamp("updated_at");
                    comment.setCreatedAt(created);
                    comment.setEdited(created != null && updated != null && updated.after(created));
                    comments.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getCommentsByVideoId error: " + e.getMessage());
        }
        return comments;
    }

    /**
     * Edits a comment, strictly verifying that the requesting user is the owner.
     */
    public boolean updateComment(int commentId, int userId, String newText) {
        String sql = "UPDATE comments SET comment_text = ?, updated_at = CURRENT_TIMESTAMP WHERE comment_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newText.trim());
            stmt.setInt(2, commentId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.updateComment error: " + e.getMessage());
            return false;
        }
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
    // REACTION (LIKE/DISLIKE) METHODS  -> table: likes(video_id, user_id, reaction_type)
    // ===================================================================================

    /**
     * Adds or updates a user's reaction (LIKE or DISLIKE) on a video.
     * Uses the unique key uk_like_video_user (video_id, user_id) for the MySQL UPSERT.
     */
    public boolean addOrUpdateReaction(int videoId, int userId, String reactionType) {
        String sql = "INSERT INTO likes (video_id, user_id, reaction_type) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE reaction_type = ?";

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
        String sql = "DELETE FROM likes WHERE video_id = ? AND user_id = ?";
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
        String sql = "SELECT COUNT(*) FROM likes WHERE video_id = ? AND reaction_type = ?";
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
        String sql = "SELECT reaction_type FROM likes WHERE video_id = ? AND user_id = ?";
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
    // RATINGS (1-5 stars)  -> table: ratings(video_id, user_id, rating_value)
    // ===================================================================================

    /** Create or update the user's rating (UPSERT on uk_rating_video_user). */
    public boolean saveRating(int videoId, int userId, int ratingValue) {
        String sql = "INSERT INTO ratings (video_id, user_id, rating_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating_value = ?, rated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            stmt.setInt(3, ratingValue);
            stmt.setInt(4, ratingValue);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.saveRating error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteRating(int videoId, int userId) {
        String sql = "DELETE FROM ratings WHERE video_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.deleteRating error: " + e.getMessage());
            return false;
        }
    }

    /** Returns {average rating, number of ratings} for a video (average is 0 when nobody rated it). */
    public double[] getRatingSummary(int videoId) {
        String sql = "SELECT COALESCE(AVG(rating_value), 0) AS avg_rating, COUNT(*) AS cnt FROM ratings WHERE video_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new double[]{rs.getDouble("avg_rating"), rs.getInt("cnt")};
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getRatingSummary error: " + e.getMessage());
        }
        return new double[]{0, 0};
    }

    /** The user's own rating for the video, or 0 when not rated. */
    public int getUserRating(int videoId, int userId) {
        String sql = "SELECT rating_value FROM ratings WHERE video_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.getUserRating error: " + e.getMessage());
        }
        return 0;
    }

    // ===================================================================================
    // CONTENT REPORTING METHODS
    //IT25102094-Dilshan
    // ===================================================================================

    /** FIX: real table is content_reports (reporter_id), not video_reports (user_id). */
    public boolean addReport(int videoId, int userId, String reason) {
        String sql = "INSERT INTO content_reports (reporter_id, video_id, reason) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, videoId);
            stmt.setString(3, reason.trim());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("EngagementDAO.addReport error: " + e.getMessage());
            return false;
        }
    }

    /** Prevents the same user flooding the admin queue with duplicate PENDING reports. */
    public boolean hasPendingReport(int videoId, int userId) {
        String sql = "SELECT 1 FROM content_reports WHERE video_id = ? AND reporter_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, videoId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("EngagementDAO.hasPendingReport error: " + e.getMessage());
            return false;
        }
    }
}

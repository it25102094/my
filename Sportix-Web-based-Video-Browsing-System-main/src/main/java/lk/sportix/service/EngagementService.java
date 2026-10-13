package lk.sportix.service;

import lk.sportix.dao.EngagementDAO;
import lk.sportix.model.Comment;

import java.util.List;

/**
 * EngagementService — Handles business logic and validation for User Engagement (Member 4).
 */
public class EngagementService {

    private final EngagementDAO engagementDAO = new EngagementDAO();

    /**
     * Validates and adds a new comment to a video.
     */
    public boolean addComment(int videoId, int userId, String text) {
        // 1. Validation: Reject null, empty, or whitespace-only comments
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        // 2. Validation: Enforce maximum length to prevent database truncation/spam
        if (text.length() > 500) {
            throw new IllegalArgumentException("Comment is too long (maximum 500 characters allowed).");
        }

        // 3. Validation: Ensure valid IDs
        if (videoId <= 0 || userId <= 0) {
            return false;
        }

        return engagementDAO.addComment(videoId, userId, text);
    }

    /**
     * Retrieves all comments for a specific video.
     */
    public List<Comment> getCommentsForVideo(int videoId) {
        if (videoId <= 0) return List.of();
        return engagementDAO.getCommentsByVideoId(videoId);
    }

    /**
     * Deletes a comment based on comment ID and owner user ID.
     */
    public boolean deleteComment(int commentId, int userId) {
        if (commentId <= 0 || userId <= 0) return false;
        return engagementDAO.deleteComment(commentId, userId);
    }
    // ===================================================================================
    // REACTION (LIKE/DISLIKE) BUSINESS LOGIC
    // ===================================================================================

    /**
     * Toggles a reaction. If the user already has this exact reaction, it removes it (unlike).
     * If they have a different reaction, it overwrites it (switch dislike to like).
     */
    public void toggleReaction(int videoId, int userId, String newReaction) {
        if (videoId <= 0 || userId <= 0) return;

        String currentReaction = engagementDAO.getUserReaction(videoId, userId);

        if (newReaction.equalsIgnoreCase(currentReaction)) {
            // User clicked the same button again -> Remove the reaction
            engagementDAO.removeReaction(videoId, userId);
        } else {
            // User clicked a new reaction -> Add or Update
            engagementDAO.addOrUpdateReaction(videoId, userId, newReaction.toUpperCase());
        }
    }

    public int getLikes(int videoId) {
        return engagementDAO.getReactionCount(videoId, "LIKE");
    }

    public int getDislikes(int videoId) {
        return engagementDAO.getReactionCount(videoId, "DISLIKE");
    }

    public String getUserReaction(int videoId, int userId) {
        if (userId <= 0) return null;
        return engagementDAO.getUserReaction(videoId, userId);
    }
    // ===================================================================================
    // CONTENT REPORTING BUSINESS LOGIC
    //IT25102094-Dilshan
    // ===================================================================================

    public boolean reportVideo(int videoId, int userId, String reason) {
        if (videoId <= 0 || userId <= 0) return false;
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A reason must be provided to report this video.");
        }
        return engagementDAO.addReport(videoId, userId, reason);
    }
}
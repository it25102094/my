package lk.sportix.service;

import lk.sportix.dao.EngagementDAO;
import lk.sportix.model.Comment;

import java.util.List;

/**
 * EngagementService — Handles business logic and validation for User Engagement (Member 4).
 * Comments: Create / Read / Update / Delete
 * Reactions: Like / Dislike toggle      Ratings: rate 1-5 / change / remove
 * Reports:  report a video to the admins
 */
public class EngagementService {

    private final EngagementDAO engagementDAO = new EngagementDAO();

    /**
     * Validates and adds a new comment to a video.
     */
    public boolean addComment(int videoId, int userId, String text) {
        validateCommentText(text);

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
     * Edits a comment (owner only).
     */
    public boolean updateComment(int commentId, int userId, String newText) {
        validateCommentText(newText);
        if (commentId <= 0 || userId <= 0) return false;
        return engagementDAO.updateComment(commentId, userId, newText);
    }

    /**
     * Deletes a comment based on comment ID and owner user ID.
     */
    public boolean deleteComment(int commentId, int userId) {
        if (commentId <= 0 || userId <= 0) return false;
        return engagementDAO.deleteComment(commentId, userId);
    }

    private void validateCommentText(String text) {
        // 1. Validation: Reject null, empty, or whitespace-only comments
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }
        // 2. Validation: Enforce maximum length to prevent database truncation/spam
        if (text.trim().length() > 500) {
            throw new IllegalArgumentException("Comment is too long (maximum 500 characters allowed).");
        }
    }

    // ===================================================================================
    // REACTION (LIKE/DISLIKE) BUSINESS LOGIC
    // ===================================================================================

    /**
     * Toggles a reaction. If the user already has this exact reaction, it removes it (unlike).
     * If they have a different reaction, it overwrites it (switch dislike to like).
     */
    public void toggleReaction(int videoId, int userId, String newReaction) {
        if (videoId <= 0 || userId <= 0 || newReaction == null) return;
        String reaction = newReaction.trim().toUpperCase();
        if (!"LIKE".equals(reaction) && !"DISLIKE".equals(reaction)) {
            throw new IllegalArgumentException("Unknown reaction.");
        }

        String currentReaction = engagementDAO.getUserReaction(videoId, userId);

        if (reaction.equals(currentReaction)) {
            // User clicked the same button again -> Remove the reaction
            engagementDAO.removeReaction(videoId, userId);
        } else {
            // User clicked a new reaction -> Add or Update
            engagementDAO.addOrUpdateReaction(videoId, userId, reaction);
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
    // RATING BUSINESS LOGIC (ratings table, 1-5)
    // ===================================================================================

    public boolean rateVideo(int videoId, int userId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }
        if (videoId <= 0 || userId <= 0) return false;
        return engagementDAO.saveRating(videoId, userId, rating);
    }

    public boolean removeRating(int videoId, int userId) {
        if (videoId <= 0 || userId <= 0) return false;
        return engagementDAO.deleteRating(videoId, userId);
    }

    /** Average rating rounded to one decimal place. */
    public double getAverageRating(int videoId) {
        double avg = engagementDAO.getRatingSummary(videoId)[0];
        return Math.round(avg * 10.0) / 10.0;
    }

    public int getRatingCount(int videoId) {
        return (int) engagementDAO.getRatingSummary(videoId)[1];
    }

    public int getUserRating(int videoId, int userId) {
        if (userId <= 0) return 0;
        return engagementDAO.getUserRating(videoId, userId);
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
        if (reason.trim().length() > 500) {
            throw new IllegalArgumentException("The report reason is too long (maximum 500 characters).");
        }
        if (engagementDAO.hasPendingReport(videoId, userId)) {
            throw new IllegalArgumentException("You have already reported this video. The admins will review it soon.");
        }
        return engagementDAO.addReport(videoId, userId, reason);
    }
}

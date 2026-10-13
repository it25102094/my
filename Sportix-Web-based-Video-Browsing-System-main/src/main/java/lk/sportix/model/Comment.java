package lk.sportix.model;

import java.sql.Timestamp;

/**
 * Comment Model — POJO representing a user's comment on a specific video.
 */
public class Comment {
    private int commentId;
    private int videoId;
    private int userId;
    private String username; // Fetched via JOIN for UI display
    private String commentText;
    private Timestamp createdAt;

    public Comment() {}

    public Comment(int commentId, int videoId, int userId, String username, String commentText, Timestamp createdAt) {
        this.commentId = commentId;
        this.videoId = videoId;
        this.userId = userId;
        this.username = username;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
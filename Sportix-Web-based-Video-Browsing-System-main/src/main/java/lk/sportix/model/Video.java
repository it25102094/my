package lk.sportix.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// it25102093 - Member 3: Content Posting Management (Core Entity)
public class Video {
    private int videoId;
    private String title;
    private String description;
    private String youTubeURL;      // Embed URL used by the <iframe> player (https://www.youtube.com/embed/{id})
    private String sourceUrl;       // Original link exactly as the uploader entered it (shown in the edit form)
    private String youtubeVideoId;
    private String thumbnail;       // thumbnail_url column
    private int viewCount;
    private String status;          // submission_status: APPROVED / PENDING / REJECTED
    private int sportId;
    private String videoType;       // LIVE / HIGHLIGHT / SHORT / INTERVIEW
    private int categoryId;
    private String matchScoreText;
    private boolean featured;
    private int uploadedBy;
    private Timestamp createdAt;

    // UI-Specific Transient Fields (Populated via SQL JOINs)
    private String sportName;
    private String categoryName;
    private String uploaderName;
    private boolean isLive;
    private List<Integer> teamIds = new ArrayList<>();

    public Video() {}

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYouTubeURL() { return youTubeURL; }
    public void setYouTubeURL(String youTubeURL) { this.youTubeURL = youTubeURL; }

    public String getSourceUrl() { return sourceUrl != null ? sourceUrl : youTubeURL; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSportId() { return sportId; }
    public void setSportId(int sportId) { this.sportId = sportId; }

    public String getVideoType() { return videoType; }
    public void setVideoType(String videoType) { this.videoType = videoType; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getMatchScoreText() { return matchScoreText; }
    public void setMatchScoreText(String matchScoreText) { this.matchScoreText = matchScoreText; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public boolean isLive() { return isLive; }
    public boolean getIsLive() { return isLive; }
    public void setLive(boolean isLive) { this.isLive = isLive; }

    public List<Integer> getTeamIds() { return teamIds; }
    public void setTeamIds(List<Integer> teamIds) { this.teamIds = (teamIds != null) ? teamIds : new ArrayList<>(); }

    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    // it25102093 - Member 3: Custom Data Parsing Logic
    /**
     * Returns the stored thumbnail (thumbnail_url column) when available, otherwise
     * derives the YouTube thumbnail URL automatically from any standard link format.
     * Safely handles malformed database entries without throwing out-of-bounds exceptions.
     */
    public String getThumbnailUrl() {
        if (thumbnail != null && !thumbnail.trim().isEmpty()) {
            return thumbnail;
        }
        String id = extractVideoId(youTubeURL);
        return id.isEmpty() ? "" : "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
    }

    /**
     * Extracts the 11-character YouTube video id from watch / youtu.be / embed / shorts / live links.
     * Returns an empty string when the link is not a recognisable YouTube URL.
     * Shared by VideoDAO (when saving) and by the advertisement cards (SPONSORED_VIDEO ads).
     */
    public static String extractVideoId(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        String u = url.trim();
        String videoIdHash = "";
        try {
            if (u.contains("v=")) {
                videoIdHash = u.substring(u.indexOf("v=") + 2);
            } else if (u.contains("youtu.be/")) {
                videoIdHash = u.substring(u.indexOf("youtu.be/") + 9);
            } else if (u.contains("/embed/")) {
                videoIdHash = u.substring(u.indexOf("/embed/") + 7);
            } else if (u.contains("/shorts/")) {
                videoIdHash = u.substring(u.indexOf("/shorts/") + 8);
            } else if (u.contains("/live/")) {
                videoIdHash = u.substring(u.indexOf("/live/") + 6);
            } else if (u.matches("[A-Za-z0-9_-]{11}")) {
                videoIdHash = u; // the user pasted just the id
            }
            for (String sep : new String[]{"&", "?", "#", "/"}) {
                if (videoIdHash.contains(sep)) {
                    videoIdHash = videoIdHash.substring(0, videoIdHash.indexOf(sep));
                }
            }
        } catch (Exception e) {
            // Failsafe for any completely unexpected URL structures in old data
            return "";
        }
        return videoIdHash.matches("[A-Za-z0-9_-]{6,20}") ? videoIdHash : "";
    }
}

package lk.sportix.model;

// it25102093 - Member 3: Content Posting Management (Core Entity)
public class Video {
    private int videoId;
    private String title;
    private String description;
    private String youTubeURL;
    private int viewCount;
    private String status;
    private int sportId;

    // UI-Specific Transient Fields (Populated via SQL JOINs)
    private String sportName;
    private boolean isLive;

    public Video() {}

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getYouTubeURL() { return youTubeURL; }
    public void setYouTubeURL(String youTubeURL) { this.youTubeURL = youTubeURL; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getSportId() { return sportId; }
    public void setSportId(int sportId) { this.sportId = sportId; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }

    public boolean isLive() { return isLive; }
    public boolean getIsLive() { return isLive; }
    public void setLive(boolean isLive) { this.isLive = isLive; }

    // it25102093 - Member 3: Custom Data Parsing Logic
    /**
     * Derives the YouTube thumbnail URL automatically from any standard link format.
     * Safely handles malformed database entries without throwing out-of-bounds exceptions.
     */
    public String getThumbnailUrl() {
        if (youTubeURL == null || youTubeURL.trim().isEmpty()) {
            return "";
        }

        String videoIdHash = "";

        try {
            if (youTubeURL.contains("v=")) {
                String[] parts = youTubeURL.split("v=");
                if (parts.length > 1) videoIdHash = parts[1];
            } else if (youTubeURL.contains("youtu.be/")) {
                String[] parts = youTubeURL.split("youtu.be/");
                if (parts.length > 1) videoIdHash = parts[1];
            } else if (youTubeURL.contains("/embed/")) {
                String[] parts = youTubeURL.split("/embed/");
                if (parts.length > 1) videoIdHash = parts[1];
            } else if (youTubeURL.contains("/shorts/")) {
                String[] parts = youTubeURL.split("/shorts/");
                if (parts.length > 1) videoIdHash = parts[1];
            } else if (youTubeURL.contains("/live/")) {
                String[] parts = youTubeURL.split("/live/");
                if (parts.length > 1) videoIdHash = parts[1];
            }

            if (!videoIdHash.isEmpty()) {
                if (videoIdHash.contains("&")) videoIdHash = videoIdHash.substring(0, videoIdHash.indexOf("&"));
                if (videoIdHash.contains("?")) videoIdHash = videoIdHash.substring(0, videoIdHash.indexOf("?"));
            }
        } catch (Exception e) {
            // Failsafe for any completely unexpected URL structures in old data
            return "";
        }

        return videoIdHash.isEmpty() ? "" : "https://img.youtube.com/vi/" + videoIdHash + "/hqdefault.jpg";
    }
}
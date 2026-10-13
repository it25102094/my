package lk.sportix.model;

public class WatchLater {

    private int itemId;
    private int userId;
    private int videoId;
    private Integer folderId;
    private String folderName;
    private String notes;
    private int itemCount;

    // Video display fields matching includes/video-card.jsp (${v.title}, ${v.thumbnailUrl}, ${v.sportName}, ${v.viewCount}, ${v.isLive})
    private String title;
    private String thumbnailUrl;
    private String sportName;
    private int viewCount;
    private boolean isLive;

    public WatchLater() {}

    public WatchLater(int userId, String folderName) {
        this.userId = userId;
        this.folderName = folderName;
    }

    public WatchLater(int userId, int videoId, Integer folderId, String notes) {
        this.userId = userId;
        this.videoId = videoId;
        this.folderId = folderId;
        this.notes = notes;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getVideoId() { return videoId; }
    public void setVideoId(int videoId) { this.videoId = videoId; }

    public Integer getFolderId() { return folderId; }
    public void setFolderId(Integer folderId) { this.folderId = folderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    // Matches ${v.title} in video-card.jsp
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVideoTitle() { return title; }
    public void setVideoTitle(String title) { this.title = title; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getSportName() { return sportName; }
    public void setSportName(String sportName) { this.sportName = sportName; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public boolean isLive() { return isLive; }
    public boolean getIsLive() { return isLive; }
    public void setLive(boolean live) { isLive = live; }
}
package lk.sportix.service;

import lk.sportix.dao.VideoDAO;
import lk.sportix.model.Category;
import lk.sportix.model.Sport;
import lk.sportix.model.Team;
import lk.sportix.model.Video;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * VideoService — Service layer handling business logic and strict validation (Members 2 & 3).
 *
 * DESIGN PATTERN 1: Data Access Object (DAO) — all SQL lives in VideoDAO.
 * DESIGN PATTERN 2: Observer — when a video becomes publicly visible, the followers of its
 *                   teams are notified through NotificationService (VideoUploadSubject +
 *                   UserObserver, Member 5). The old version registered a hard-coded dummy
 *                   observer (user #1) and pushed a message on EVERY upload, even pending ones.
 */
public class VideoService {

    private static final Set<String> VIDEO_TYPES = Set.of("LIVE", "HIGHLIGHT", "SHORT", "INTERVIEW");

    private final VideoDAO videoDAO = new VideoDAO();
    private final NotificationService notificationService = new NotificationService();

    // ===================================================================================
    // BROWSING & SEARCH METHODS (Member 2)
    // ===================================================================================

    public Video getFeaturedVideo() {
        return videoDAO.getFeaturedVideo();
    }

    public List<Video> getTrendingVideos(int limit) {
        int safeLimit = (limit > 0) ? limit : 4;
        return videoDAO.getTrendingVideos(safeLimit);
    }

    public List<Video> getVideosByCategory(String categoryType, int limit) {
        int safeLimit = (limit > 0) ? limit : 4;
        if (categoryType == null || categoryType.trim().isEmpty()) {
            return videoDAO.getTrendingVideos(safeLimit);
        }
        return videoDAO.getVideosByType(categoryType.trim().toUpperCase(), safeLimit);
    }

    /** Public: APPROVED videos only. */
    public Video getVideoById(int videoId) {
        if (videoId <= 0) return null;
        return videoDAO.getVideoById(videoId);
    }

    /** Any status — used for the uploader's own preview and for admin moderation. */
    public Video getVideoByIdAnyStatus(int videoId) {
        if (videoId <= 0) return null;
        return videoDAO.getVideoByIdAnyStatus(videoId);
    }

    public List<Video> searchVideos(String keyword) {
        if (keyword == null) keyword = "";
        return videoDAO.searchVideos(keyword.trim());
    }

    public void incrementViewCount(int videoId) {
        if (videoId > 0) {
            videoDAO.incrementViewCount(videoId);
        }
    }

    public List<Team> getTeamsForVideo(int videoId) {
        return videoDAO.getTeamsForVideo(videoId);
    }

    public List<Video> getVideosBySportName(String sportName) {
        if (sportName == null || sportName.trim().isEmpty()) {
            return videoDAO.getTrendingVideos(20);
        }
        return videoDAO.getVideosBySportName(sportName);
    }

    // Lookup lists for the upload / edit forms
    public List<Sport> getAllSports() { return videoDAO.getAllSports(); }
    public List<Category> getAllCategories() { return videoDAO.getAllCategories(); }
    public List<Team> getAllTeams() { return videoDAO.getAllTeams(); }

    // ===================================================================================
    // IT25102093'S METHODS (Content Management CRUD)
    // ===================================================================================

    /**
     * IT25102093 - Content Management CRUD (Create + Validation + Observer Trigger).
     * Videos from normal users wait for admin approval (PENDING). Content managers and admins
     * are trusted, so their uploads are APPROVED immediately and followers are notified.
     *
     * @return the new video id
     */
    public int addVideo(Video video, String uploaderRole) {
        validate(video);
        boolean trusted = isTrusted(uploaderRole);
        int newId = videoDAO.addVideo(video, video.getUploadedBy(), trusted ? "APPROVED" : "PENDING");
        if (newId <= 0) {
            throw new IllegalStateException("Database error occurred while saving the video.");
        }
        if (trusted) {
            // Trigger Observer Pattern on a published video
            notificationService.notifyFollowersOfNewVideo(videoDAO.getVideoByIdAnyStatus(newId));
        }
        return newId;
    }

    // it25102093 -CRUD (Read)
    public List<Video> getVideosByUserId(int userId) {
        if (userId <= 0) return List.of();
        return videoDAO.getVideosByUserId(userId);
    }

    // it25102093 - CRUD (Read Single)
    public Video getVideoByIdAndUser(int videoId, int userId) {
        if (videoId <= 0 || userId <= 0) return null;
        return videoDAO.getVideoByIdAndUser(videoId, userId);
    }

    /**
     * it25102093 -  CRUD (Update).
     * A REJECTED video goes back to PENDING when it is edited (re-submission). When a normal user
     * changes the YouTube link of an approved video it must be re-approved as well.
     *
     * @return the submission status after the update
     */
    public String updateVideo(Video video, int userId, String role) {
        validate(video);
        Video existing = videoDAO.getVideoByIdAndUser(video.getVideoId(), userId);
        if (existing == null) {
            throw new IllegalArgumentException("Video not found or access denied.");
        }
        String newStatus = existing.getStatus();
        boolean linkChanged = !Video.extractVideoId(video.getSourceUrl()).equals(existing.getYoutubeVideoId());
        if ("REJECTED".equals(newStatus) || (!isTrusted(role) && linkChanged)) {
            newStatus = isTrusted(role) ? "APPROVED" : "PENDING";
        }
        if (!videoDAO.updateVideo(video, userId, newStatus)) {
            throw new IllegalStateException("Error updating video in the database.");
        }
        if ("APPROVED".equals(newStatus) && !"APPROVED".equals(existing.getStatus())) {
            // the video just became public -> Observer pattern alerts the followers
            notificationService.notifyFollowersOfNewVideo(videoDAO.getVideoByIdAnyStatus(video.getVideoId()));
        }
        return newStatus;
    }

    // it25102093 -  CRUD (Delete)
    public boolean deleteVideo(int videoId, int userId) {
        if (videoId <= 0 || userId <= 0) return false;
        return videoDAO.deleteVideo(videoId, userId);
    }

    // ===================================================================================
    // VALIDATION
    // ===================================================================================

    private boolean isTrusted(String role) {
        return "CONTENT_MANAGER".equals(role) || "ADMIN".equals(role);
    }

    /** Strict Defensive Validation (12 Marks Criteria) — also normalises the values. */
    private void validate(Video video) {
        if (video == null || video.getTitle() == null || video.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Video title cannot be empty.");
        }
        video.setTitle(video.getTitle().trim());
        if (video.getTitle().length() > 200) {   // videos.title is VARCHAR(200)
            throw new IllegalArgumentException("Video title must not exceed 200 characters.");
        }

        String ytUrl = video.getSourceUrl();
        if (ytUrl == null || ytUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("YouTube URL cannot be empty.");
        }
        video.setSourceUrl(ytUrl.trim());
        if (ytUrl.trim().length() > 500 || Video.extractVideoId(ytUrl).isEmpty()) {
            throw new IllegalArgumentException("Please enter a valid YouTube link (watch, youtu.be, shorts or live).");
        }

        if (video.getDescription() != null) {
            video.setDescription(video.getDescription().trim());
            if (video.getDescription().length() > 2000) {
                throw new IllegalArgumentException("Description must not exceed 2000 characters.");
            }
        }
        if (video.getMatchScoreText() != null) {
            String score = video.getMatchScoreText().trim();
            video.setMatchScoreText(score.isEmpty() ? null : score);
            if (score.length() > 100) {
                throw new IllegalArgumentException("Match score must not exceed 100 characters.");
            }
        }

        String type = video.getVideoType() == null ? "" : video.getVideoType().trim().toUpperCase();
        if (!VIDEO_TYPES.contains(type)) {
            throw new IllegalArgumentException("Video type must be Live, Highlight, Short or Interview.");
        }
        video.setVideoType(type);

        boolean sportExists = videoDAO.getAllSports().stream().anyMatch(s -> s.getSportId() == video.getSportId());
        if (!sportExists) {
            throw new IllegalArgumentException("Please select a valid sport.");
        }

        boolean categoryExists = videoDAO.getAllCategories().stream().anyMatch(c -> c.getCategoryId() == video.getCategoryId());
        if (!categoryExists) {
            video.setCategoryId(defaultCategoryFor(type));
        }

        // Keep only real teams that belong to the selected sport (and no duplicates)
        Set<Integer> wanted = new LinkedHashSet<>(video.getTeamIds());
        List<Integer> valid = new ArrayList<>();
        for (Team t : videoDAO.getAllTeams()) {
            if (wanted.contains(t.getTeamId()) && t.getSportId() == video.getSportId()) valid.add(t.getTeamId());
        }
        video.setTeamIds(valid);

        if (video.getUploadedBy() <= 0 && video.getVideoId() <= 0) {
            throw new IllegalArgumentException("Invalid User ID.");
        }
    }

    /** categories seed: 1 Live Matches, 2 Match Highlights, 3 Sports Shorts, 4 Interviews. */
    private int defaultCategoryFor(String type) {
        switch (type) {
            case "LIVE": return 1;
            case "SHORT": return 3;
            case "INTERVIEW": return 4;
            default: return 2;
        }
    }
}

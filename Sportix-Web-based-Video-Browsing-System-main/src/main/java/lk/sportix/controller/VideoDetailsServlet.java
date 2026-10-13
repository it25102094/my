package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Comment;
import lk.sportix.model.Video;
import lk.sportix.service.AdService;
import lk.sportix.service.EngagementService;
import lk.sportix.service.FollowTeamService;
import lk.sportix.service.VideoService;
import lk.sportix.service.WatchLaterService;
import lk.sportix.util.WebUtil;

import java.io.IOException;
import java.util.List;

/**
 * VideoDetailsServlet — the watch page.
 * FIX: video-details.jsp used ${videoTeams}, ${followedTeamIds} and ${isSaved}, but this servlet
 * never set them, so the Follow-team buttons never appeared (no follower -> no notifications)
 * and "Save to Watch Later" could never show its saved state.
 */
@WebServlet("/video")
public class VideoDetailsServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();
    private final EngagementService engagementService = new EngagementService();
    private final FollowTeamService followTeamService = new FollowTeamService();
    private final WatchLaterService watchLaterService = new WatchLaterService();
    private final AdService adService = new AdService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int videoId = WebUtil.parseInt(request.getParameter("id"), 0);
        if (videoId <= 0) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        int userId = WebUtil.currentUserId(request);
        Video video = videoService.getVideoById(videoId);

        // The uploader (and admins) may preview a PENDING / REJECTED video
        if (video == null && userId > 0) {
            Video any = videoService.getVideoByIdAnyStatus(videoId);
            if (any != null && (any.getUploadedBy() == userId || WebUtil.isAdmin(request))) {
                video = any;
                request.setAttribute("previewMode", true);
            }
        }

        if (video == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Video not found");
            return;
        }

        if ("APPROVED".equals(video.getStatus())) {
            videoService.incrementViewCount(videoId);
        }

        // Member 4: comments, reactions and ratings
        List<Comment> comments = engagementService.getCommentsForVideo(videoId);
        request.setAttribute("comments", comments);
        request.setAttribute("likesCount", engagementService.getLikes(videoId));
        request.setAttribute("dislikesCount", engagementService.getDislikes(videoId));
        request.setAttribute("avgRating", engagementService.getAverageRating(videoId));
        request.setAttribute("ratingCount", engagementService.getRatingCount(videoId));

        // Teams shown with Follow / Unfollow buttons (Member 1) - feeds the Observer pattern
        request.setAttribute("videoTeams", videoService.getTeamsForVideo(videoId));

        if (userId > 0) {
            request.setAttribute("userReaction", engagementService.getUserReaction(videoId, userId)); // "LIKE", "DISLIKE" or null
            request.setAttribute("userRating", engagementService.getUserRating(videoId, userId));
            request.setAttribute("followedTeamIds", followTeamService.getFollowedTeamIds(userId));
            request.setAttribute("isSaved", watchLaterService.isVideoSaved(userId, videoId));
        }

        // Member 5: sponsored cards next to the player (RotationAdSelectionStrategy)
        request.setAttribute("sidebarAds", adService.getAdsForPlacement(AdService.PLACEMENT_VIDEO_SIDEBAR, 2));

        request.setAttribute("video", video);
        request.getRequestDispatcher("/WEB-INF/views/video-details.jsp").forward(request, response);
    }
}

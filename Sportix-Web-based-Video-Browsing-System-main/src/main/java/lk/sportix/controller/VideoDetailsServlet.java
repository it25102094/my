package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.model.Comment;
import lk.sportix.model.Video;
import lk.sportix.service.EngagementService;
import lk.sportix.service.VideoService;

import java.io.IOException;
import java.util.List;

@WebServlet("/video")
public class VideoDetailsServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();
    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            int videoId = Integer.parseInt(idParam);
            Video video = videoService.getVideoById(videoId);

            if (video == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Video not found");
                return;
            }

            videoService.incrementViewCount(videoId);

            // Fetch Comments
            List<Comment> comments = engagementService.getCommentsForVideo(videoId);
            request.setAttribute("comments", comments);

            // Fetch Reaction Counts (Likes/Dislikes)
            int likes = engagementService.getLikes(videoId);
            int dislikes = engagementService.getDislikes(videoId);
            request.setAttribute("likesCount", likes);
            request.setAttribute("dislikesCount", dislikes);

            // Check if current user has already reacted
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                int userId = (Integer) session.getAttribute("userId");
                String userReaction = engagementService.getUserReaction(videoId, userId);
                request.setAttribute("userReaction", userReaction); // Will be "LIKE", "DISLIKE", or null
            }

            request.setAttribute("video", video);
            request.getRequestDispatcher("/WEB-INF/views/video-details.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
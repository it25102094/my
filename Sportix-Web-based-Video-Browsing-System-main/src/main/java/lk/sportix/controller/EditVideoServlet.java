package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.model.Video;
import lk.sportix.service.VideoService;

import java.io.IOException;

// it25102093 - Member 3: Content Posting Management (Update Video)
@WebServlet("/user/edit-video")
public class EditVideoServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        int videoId = 0;

        try {
            videoId = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException ignored) {
            // If the ID is tampered with in the URL, it defaults to 0
        }

        // MVC Fixed: Replace raw SQL SELECT with Service call.
        // Enforces ownership: only returns the video if it belongs to this userId.
        Video video = videoService.getVideoByIdAndUser(videoId, userId);

        if (video != null) {
            request.setAttribute("videoId", video.getVideoId());
            request.setAttribute("title", video.getTitle());
            request.setAttribute("description", video.getDescription());
            request.setAttribute("youtubeUrl", video.getYouTubeURL());
            request.setAttribute("videoType", video.isLive() ? "LIVE" : "HIGHLIGHT");
            request.setAttribute("sportId", video.getSportId());

            request.getRequestDispatcher("/WEB-INF/views/edit-video.jsp").forward(request, response);
        } else {
            // If video doesn't exist or doesn't belong to the user, redirect away
            session.setAttribute("flashMessage", "Error: Video not found or access denied.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        Video video = new Video();

        // 1. Defensive Parsing: Prevent 500 Server Errors from tampered form data
        try {
            video.setVideoId(Integer.parseInt(request.getParameter("videoId")));
            video.setSportId(Integer.parseInt(request.getParameter("sportId")));
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "Error: Invalid data submitted.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
            return;
        }

        // Map remaining form parameters to Model
        video.setTitle(request.getParameter("title"));
        video.setYouTubeURL(request.getParameter("youtubeUrl"));
        video.setDescription(request.getParameter("description"));
        String videoType = request.getParameter("videoType");

        // 2. MVC Fixed: Replace raw SQL UPDATE with Service call & Error Handling
        try {
            if (videoService.updateVideo(video, videoType, userId)) {
                session.setAttribute("flashMessage", "Success! Video updated successfully.");
            } else {
                session.setAttribute("flashMessage", "Error updating video in the database.");
            }
        } catch (IllegalArgumentException e) {
            // Catches strict backend validation rules (e.g. empty titles)
            session.setAttribute("flashMessage", "Validation Error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/user/my-videos");
    }
}
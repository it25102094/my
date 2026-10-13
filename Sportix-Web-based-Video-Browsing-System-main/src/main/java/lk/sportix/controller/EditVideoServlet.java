package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Video;
import lk.sportix.service.VideoService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

// it25102093 - Member 3: Content Posting Management (Update Video)
@WebServlet("/user/edit-video")
public class EditVideoServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // If the ID is tampered with in the URL, it defaults to 0
        int videoId = WebUtil.parseInt(request.getParameter("id"), 0);

        // Enforces ownership: only returns the video if it belongs to this userId (any status).
        Video video = videoService.getVideoByIdAndUser(videoId, userId);

        if (video != null) {
            showForm(request, response, video);
        } else {
            // If video doesn't exist or doesn't belong to the user, redirect away
            WebUtil.flash(request, "Error: Video not found or access denied.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 1. Defensive Parsing: Prevent 500 Server Errors from tampered form data
        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
        if (videoId <= 0) {
            WebUtil.flash(request, "Error: Invalid data submitted.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
            return;
        }

        // Map form parameters to Model (FIX: SHORT / INTERVIEW types and the category were lost)
        Video video = AddVideoServlet.readVideoForm(request);
        video.setVideoId(videoId);

        // 2. MVC: Service call & Error Handling
        try {
            String status = videoService.updateVideo(video, userId, WebUtil.currentRole(request));
            WebUtil.flash(request, "PENDING".equals(status)
                    ? "Success! Video updated and sent to the admin for (re-)approval."
                    : "Success! Video updated successfully.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Show the form again with the user's input and the validation message
            request.setAttribute("error", e.getMessage());
            Video existing = videoService.getVideoByIdAndUser(videoId, userId);
            if (existing == null) {
                WebUtil.flash(request, "Error: Video not found or access denied.");
                response.sendRedirect(request.getContextPath() + "/user/my-videos");
                return;
            }
            video.setStatus(existing.getStatus());
            showForm(request, response, video);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Video video) throws ServletException, IOException {
        request.setAttribute("video", video);
        request.setAttribute("sports", videoService.getAllSports());
        request.setAttribute("categories", videoService.getAllCategories());
        request.setAttribute("teams", videoService.getAllTeams());
        request.getRequestDispatcher("/WEB-INF/views/edit-video.jsp").forward(request, response);
    }
}

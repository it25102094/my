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
import java.util.List;

// it25102093 - Member 3: Content Posting Management (Read & Delete)
@WebServlet("/user/my-videos")
public class ManageVideosServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // MVC: Retrieve uploaded content exclusively for this user via Service Layer
        List<Video> myVideos = videoService.getVideosByUserId(userId);

        request.setAttribute("myVideos", myVideos);
        request.getRequestDispatcher("/WEB-INF/views/manage-videos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        // Action-based routing for forms on the manage videos page
        if ("delete".equalsIgnoreCase(action)) {
            int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
            // Secure Deletion: Service/DAO inherently verifies the userId owns the videoId
            // Prevents BOLA (Broken Object Level Authorization) attacks
            if (videoService.deleteVideo(videoId, userId)) {
                WebUtil.flash(request, "Video successfully deleted.");
            } else {
                WebUtil.flash(request, "Action denied: Unauthorized or video not found.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/user/my-videos");
    }
}

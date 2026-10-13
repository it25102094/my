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
import java.util.List;

// it25102093 - Member 3: Content Posting Management (Read & Delete)
@WebServlet("/user/my-videos")
public class ManageVideosServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // MVC Fixed: Retrieve uploaded content exclusively for this user via Service Layer
        List<Video> myVideos = videoService.getVideosByUserId(userId);

        request.setAttribute("myVideos", myVideos);
        request.getRequestDispatcher("/WEB-INF/views/manage-videos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");

        // Action-based routing for forms on the manage videos page
        if ("delete".equalsIgnoreCase(action)) {
            try {
                int videoId = Integer.parseInt(request.getParameter("videoId"));

                // Secure Deletion: Service/DAO inherently verifies the userId owns the videoId
                // Prevents BOLA (Broken Object Level Authorization) attacks
                if (videoService.deleteVideo(videoId, userId)) {
                    session.setAttribute("flashMessage", "Video successfully deleted.");
                } else {
                    session.setAttribute("flashMessage", "Action denied: Unauthorized or video not found.");
                }
            } catch (NumberFormatException e) {
                // Defensive exception handling for tampered form data
                session.setAttribute("flashMessage", "Invalid request format.");
            }
        }

        response.sendRedirect(request.getContextPath() + "/user/my-videos");
    }
}
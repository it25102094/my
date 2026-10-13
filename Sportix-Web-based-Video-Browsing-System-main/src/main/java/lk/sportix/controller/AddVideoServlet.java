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

// it25102093 -  Content Posting Management (Create Video)
@WebServlet("/user/add-video")
public class AddVideoServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/add-video.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // Map form to Model
        Video video = new Video();
        video.setTitle(request.getParameter("title"));
        video.setDescription(request.getParameter("description"));
        video.setYouTubeURL(request.getParameter("youtubeUrl"));

        String videoType = request.getParameter("videoType");

        // Improved Type Parsing: Avoid swallowing exceptions silently
        try {
            String sportIdStr = request.getParameter("sportId");
            if (sportIdStr != null && !sportIdStr.isEmpty()) {
                video.setSportId(Integer.parseInt(sportIdStr));
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Invalid Sport Selection.");
            request.getRequestDispatcher("/WEB-INF/views/add-video.jsp").forward(request, response);
            return;
        }

        // MVC Fixed: Call Service and Catch Validation Exceptions
        try {
            if (videoService.addVideo(video, videoType, null, userId)) {
                session.setAttribute("flashMessage", "Success! Video published and is pending approval.");
                response.sendRedirect(request.getContextPath() + "/user/my-videos");
            } else {
                request.setAttribute("error", "Database error occurred while saving the video.");
                request.getRequestDispatcher("/WEB-INF/views/add-video.jsp").forward(request, response);
            }
        } catch (IllegalArgumentException e) {
            // Catches title length/URL format errors from VideoService and displays them to the user
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/add-video.jsp").forward(request, response);
        }
    }
}
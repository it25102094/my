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
import java.util.ArrayList;
import java.util.List;

// it25102093 -  Content Posting Management (Create Video)
@WebServlet("/user/add-video")
public class AddVideoServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (WebUtil.currentUserId(request) <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        showForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Map form to Model (FIX: teams, category and match score were never read before)
        Video video = readVideoForm(request);
        video.setUploadedBy(userId);

        // MVC Fixed: Call Service and Catch Validation Exceptions
        try {
            videoService.addVideo(video, WebUtil.currentRole(request));
            boolean trusted = WebUtil.hasRole(request, "CONTENT_MANAGER", "ADMIN");
            WebUtil.flash(request, trusted
                    ? "Success! Video published. Followers of the selected teams have been notified."
                    : "Success! Video submitted and is pending admin approval.");
            response.sendRedirect(request.getContextPath() + "/user/my-videos");
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Catches title length/URL format errors from VideoService and displays them to the user
            request.setAttribute("error", e.getMessage());
            request.setAttribute("video", video);   // keep what the user typed
            showForm(request, response);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("sports", videoService.getAllSports());
        request.setAttribute("categories", videoService.getAllCategories());
        request.setAttribute("teams", videoService.getAllTeams());
        request.getRequestDispatcher("/WEB-INF/views/add-video.jsp").forward(request, response);
    }

    /** Shared by Add and Edit: reads the video form fields safely (no NumberFormatException -> 500). */
    static Video readVideoForm(HttpServletRequest request) {
        Video video = new Video();
        video.setTitle(request.getParameter("title"));
        video.setDescription(request.getParameter("description"));
        video.setSourceUrl(request.getParameter("youtubeUrl"));
        video.setVideoType(request.getParameter("videoType"));
        video.setMatchScoreText(request.getParameter("matchScore"));
        video.setSportId(WebUtil.parseInt(request.getParameter("sportId"), 0));
        video.setCategoryId(WebUtil.parseInt(request.getParameter("categoryId"), 0));
        List<Integer> teamIds = new ArrayList<>();
        String[] teams = request.getParameterValues("teamIds");
        if (teams != null) {
            for (String t : teams) {
                int id = WebUtil.parseInt(t, 0);
                if (id > 0) teamIds.add(id);
            }
        }
        video.setTeamIds(teamIds);
        return video;
    }
}

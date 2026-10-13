package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.EngagementService;

import java.io.IOException;

/**
 * ReactionServlet — Handles Like and Dislike actions for videos.
 */
@WebServlet("/reaction")
public class ReactionServlet extends HttpServlet {

    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action"); // Should be "LIKE" or "DISLIKE"
        String videoIdStr = request.getParameter("videoId");

        if (videoIdStr != null && !videoIdStr.trim().isEmpty() && action != null) {
            try {
                int videoId = Integer.parseInt(videoIdStr);
                engagementService.toggleReaction(videoId, userId, action);
            } catch (NumberFormatException e) {
                // Ignore invalid ID formats
            }
        }

        // Redirect back to the video details page
        response.sendRedirect(request.getContextPath() + "/video?id=" + videoIdStr);
    }
}
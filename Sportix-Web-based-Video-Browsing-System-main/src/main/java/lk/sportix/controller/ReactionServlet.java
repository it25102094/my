package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.EngagementService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

/**
 * ReactionServlet — Handles Like / Dislike actions and 1-5 star ratings for videos (Member 4).
 *   action=LIKE | DISLIKE        toggle reaction (likes table)
 *   action=RATE&rating=1..5      create / update rating (ratings table)
 *   action=UNRATE                delete rating
 */
@WebServlet("/reaction")
public class ReactionServlet extends HttpServlet {

    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);

        if (videoId > 0 && action != null) {
            try {
                if ("RATE".equalsIgnoreCase(action)) {
                    engagementService.rateVideo(videoId, userId, WebUtil.parseInt(request.getParameter("rating"), 0));
                    WebUtil.flash(request, "Thanks! Your rating was saved.");
                } else if ("UNRATE".equalsIgnoreCase(action)) {
                    engagementService.removeRating(videoId, userId);
                    WebUtil.flash(request, "Your rating was removed.");
                } else {
                    engagementService.toggleReaction(videoId, userId, action);
                }
            } catch (IllegalArgumentException e) {
                WebUtil.flash(request, e.getMessage());
            }
        }

        // Redirect back to the video details page
        response.sendRedirect(request.getContextPath() + (videoId > 0 ? "/video?id=" + videoId : "/home"));
    }
}

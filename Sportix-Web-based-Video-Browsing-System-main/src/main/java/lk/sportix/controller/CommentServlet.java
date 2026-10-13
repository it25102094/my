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
 * CommentServlet — Handles adding, editing and deleting comments (Member 4).
 * FIX: added the missing UPDATE (edit) action and safe number parsing (a bad videoId used to
 * throw NumberFormatException -> HTTP 500).
 */
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {

    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security check: User must be logged in
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
        if (videoId <= 0) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        int commentId = WebUtil.parseInt(request.getParameter("commentId"), 0);

        try {
            if ("add".equalsIgnoreCase(action)) {
                engagementService.addComment(videoId, userId, request.getParameter("commentText"));
                WebUtil.flash(request, "Comment posted.");

            } else if ("update".equalsIgnoreCase(action)) {
                boolean success = engagementService.updateComment(commentId, userId, request.getParameter("commentText"));
                WebUtil.flash(request, success ? "Comment updated." : "Action denied: Unauthorized or comment not found.");

            } else if ("delete".equalsIgnoreCase(action)) {
                boolean success = engagementService.deleteComment(commentId, userId);
                WebUtil.flash(request, success ? "Comment deleted." : "Action denied: Unauthorized or comment not found.");
            }
        } catch (IllegalArgumentException e) {
            // Catch validation errors (e.g., empty comment, too long)
            WebUtil.flash(request, e.getMessage());
        } catch (Exception e) {
            WebUtil.flash(request, "An unexpected error occurred.");
        }

        // Redirect back to the video details page to see the updated comments
        response.sendRedirect(request.getContextPath() + "/video?id=" + videoId + "#comments");
    }
}

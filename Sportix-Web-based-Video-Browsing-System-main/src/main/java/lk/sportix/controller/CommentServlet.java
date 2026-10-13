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
 * CommentServlet — Handles adding and deleting comments.
 */
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {

    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security check: User must be logged in
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String videoIdStr = request.getParameter("videoId");

        if (videoIdStr == null || videoIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        int videoId = Integer.parseInt(videoIdStr);

        try {
            if ("add".equalsIgnoreCase(action)) {
                String commentText = request.getParameter("commentText");
                engagementService.addComment(videoId, userId, commentText);

            } else if ("delete".equalsIgnoreCase(action)) {
                int commentId = Integer.parseInt(request.getParameter("commentId"));
                boolean success = engagementService.deleteComment(commentId, userId);

                if (!success) {
                    session.setAttribute("flashMessage", "Action denied: Unauthorized or comment not found.");
                }
            }
        } catch (IllegalArgumentException e) {
            // Catch validation errors (e.g., empty comment, too long)
            session.setAttribute("flashMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("flashMessage", "An unexpected error occurred.");
        }

        // Redirect back to the video details page to see the updated comments
        response.sendRedirect(request.getContextPath() + "/video?id=" + videoId);
    }
}
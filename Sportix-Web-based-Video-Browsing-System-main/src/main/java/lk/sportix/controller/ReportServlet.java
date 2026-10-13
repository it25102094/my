package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.EngagementService;

import java.io.IOException;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {

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
        String videoIdStr = request.getParameter("videoId");
        String reason = request.getParameter("reason");

        if (videoIdStr != null && !videoIdStr.trim().isEmpty() && reason != null) {
            try {
                int videoId = Integer.parseInt(videoIdStr);
                engagementService.reportVideo(videoId, userId, reason);
                session.setAttribute("flashMessage", "Thank you. This video has been reported to the admins for review.");
            } catch (NumberFormatException e) {
                session.setAttribute("flashMessage", "Invalid request format.");
            } catch (IllegalArgumentException e) {
                session.setAttribute("flashMessage", e.getMessage());
            }
        }

        response.sendRedirect(request.getContextPath() + "/video?id=" + videoIdStr);
    }
}
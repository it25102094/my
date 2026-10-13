package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.EngagementService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

@WebServlet("/report")
public class ReportServlet extends HttpServlet {

    private final EngagementService engagementService = new EngagementService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
        String reason = request.getParameter("reason");

        if (videoId <= 0) {
            WebUtil.flash(request, "Invalid request format.");
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        try {
            if (engagementService.reportVideo(videoId, userId, reason)) {
                WebUtil.flash(request, "Thank you. This video has been reported to the admins for review.");
            } else {
                WebUtil.flash(request, "The report could not be saved. Please try again.");
            }
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/video?id=" + videoId);
    }
}

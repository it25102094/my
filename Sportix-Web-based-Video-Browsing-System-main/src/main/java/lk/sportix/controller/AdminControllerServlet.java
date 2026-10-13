package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.AdminService;
import lk.sportix.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin")
public class AdminControllerServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        List<Map<String, Object>> pendingVideos = new ArrayList<>();
        List<Map<String, Object>> pendingReports = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection()) {
            // Fetch Pending Videos
            String videoSql = "SELECT video_id, title, youtube_url FROM videos WHERE submission_status = 'PENDING'";
            try (PreparedStatement stmt = conn.prepareStatement(videoSql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> v = new HashMap<>();
                    v.put("videoId", rs.getInt("video_id"));
                    v.put("title", rs.getString("title"));
                    v.put("youtubeUrl", rs.getString("youtube_url"));
                    pendingVideos.add(v);
                }
            }

            // Fetch Pending Reports
            String reportSql = "SELECT c.report_id, c.reason, v.title FROM content_reports c LEFT JOIN videos v ON c.video_id = v.video_id WHERE c.status = 'PENDING'";
            try (PreparedStatement stmt = conn.prepareStatement(reportSql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> rep = new HashMap<>();
                    rep.put("reportId", rs.getInt("report_id"));
                    rep.put("reason", rs.getString("reason"));
                    rep.put("videoTitle", rs.getString("title") != null ? rs.getString("title") : "Comment/User Report");
                    pendingReports.add(rep);
                }
            }
        } catch (Exception e) {
            System.err.println("AdminController GET error: " + e.getMessage());
        }

        request.setAttribute("pendingVideos", pendingVideos);
        request.setAttribute("pendingReports", pendingReports);
        request.setAttribute("systemStats", adminService.getSystemStatistics());

        request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int adminId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("updateVideoStatus".equals(action)) {
                int videoId = Integer.parseInt(request.getParameter("videoId"));
                String status = request.getParameter("status");
                adminService.processVideoStatusUpdate(videoId, status, adminId);
            } else if ("updateReportStatus".equals(action)) {
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                String status = request.getParameter("status");
                adminService.processReportStatusUpdate(reportId, status, adminId);
            }
        } catch (Exception e) {
            session.setAttribute("flashMessage", "Action failed: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
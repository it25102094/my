package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NotificationServlet — Handles user notification retrieval and status updates.
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        List<Map<String, Object>> notifications = new ArrayList<>();

        String sql = "SELECT notification_id, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("notificationId", rs.getInt("notification_id"));
                    notif.put("message", rs.getString("message"));
                    notif.put("isRead", rs.getBoolean("is_read"));
                    notif.put("createdAt", rs.getTimestamp("created_at"));
                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            System.err.println("NotificationServlet.doGet error: " + e.getMessage());
        }

        request.setAttribute("notifications", notifications);
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("markRead".equals(action)) {
            String notifIdStr = request.getParameter("notificationId");
            if (notifIdStr != null) {
                try {
                    int notificationId = Integer.parseInt(notifIdStr);
                    String updateSql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
                    try (Connection conn = DBConnection.getInstance().getConnection();
                         PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setInt(1, notificationId);
                        stmt.executeUpdate();
                    }
                } catch (Exception e) {
                    System.err.println("NotificationServlet.doPost error: " + e.getMessage());
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/notifications");
    }
}
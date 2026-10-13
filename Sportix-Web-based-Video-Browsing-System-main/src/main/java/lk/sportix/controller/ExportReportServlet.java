package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.AdminService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/admin/export-csv")
public class ExportReportServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int adminId = (Integer) session.getAttribute("userId");
        String csvContent = adminService.generateVideoAnalyticsCsv(adminId);

        String fileName = "sportix_video_analytics_" + LocalDate.now() + ".csv";

        // Set response headers to trigger file download
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write(csvContent);
            writer.flush();
        }
    }
}
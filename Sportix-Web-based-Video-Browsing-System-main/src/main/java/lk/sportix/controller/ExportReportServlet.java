package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.AdminService;
import lk.sportix.util.WebUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;

@WebServlet("/admin/export-csv")
public class ExportReportServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (WebUtil.currentUserId(request) <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!WebUtil.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access only.");
            return;
        }

        int adminId = WebUtil.currentUserId(request);
        String csvContent = adminService.generateVideoAnalyticsCsv(adminId);

        String fileName = "sportix_video_analytics_" + LocalDate.now() + ".csv";

        // Set response headers to trigger file download
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF'); // UTF-8 BOM so Excel shows Sinhala/Tamil text correctly
            writer.write(csvContent);
            writer.flush();
        }
    }
}

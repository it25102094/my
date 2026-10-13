package lk.sportix.service;

import lk.sportix.dao.AdminDAO;
import lk.sportix.model.AdminLog;

import java.util.List;
import java.util.Map;

public class AdminService {

    private final AdminDAO adminDAO = new AdminDAO();

    public boolean processVideoStatusUpdate(int videoId, String status, int adminId) {
        if (status == null || (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Invalid video status. Must be APPROVED or REJECTED.");
        }
        boolean success = adminDAO.updateVideoStatus(videoId, status.toUpperCase());
        if (success) {
            adminDAO.logAdminAction(new AdminLog(adminId, status.toUpperCase(), "videos", videoId, "Video " + status.toLowerCase() + " by Admin"));
        }
        return success;
    }

    public boolean processReportStatusUpdate(int reportId, String status, int adminId) {
        if (status == null || (!status.equalsIgnoreCase("RESOLVED") && !status.equalsIgnoreCase("DISMISSED"))) {
            throw new IllegalArgumentException("Invalid report status. Must be RESOLVED or DISMISSED.");
        }
        boolean success = adminDAO.updateReportStatus(reportId, status.toUpperCase(), adminId);
        if (success) {
            adminDAO.logAdminAction(new AdminLog(adminId, status.toUpperCase(), "content_reports", reportId, "Report " + status.toLowerCase() + " by Admin"));
        }
        return success;
    }

    public String generateVideoAnalyticsCsv(int adminId) {
        List<Map<String, Object>> records = adminDAO.getVideoAnalyticsReport();
        StringBuilder csv = new StringBuilder();

        // CSV Header row
        csv.append("Video ID,Title,Sport,Type,Status,Views,Created At\n");

        for (Map<String, Object> row : records) {
            // Escape quotes and commas in titles to avoid CSV parsing errors
            String rawTitle = row.get("title") != null ? row.get("title").toString() : "Untitled";
            String escapedTitle = "\"" + rawTitle.replace("\"", "\"\"") + "\"";

            csv.append(row.get("videoId")).append(",")
                    .append(escapedTitle).append(",")
                    .append(row.get("sportName")).append(",")
                    .append(row.get("videoType")).append(",")
                    .append(row.get("status")).append(",")
                    .append(row.get("views")).append(",")
                    .append(row.get("createdAt")).append("\n");
        }

        // Log the export action to admin_logs
        adminDAO.logAdminAction(new AdminLog(adminId, "EXPORT_CSV", "videos", 0, "Exported video analytics report to CSV"));

        return csv.toString();
    }

    public Map<String, Integer> getSystemStatistics() {
        return adminDAO.getSystemStatistics();
    }
}
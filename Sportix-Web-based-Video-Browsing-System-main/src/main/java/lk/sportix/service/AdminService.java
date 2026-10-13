package lk.sportix.service;

import lk.sportix.dao.AdminDAO;
import lk.sportix.dao.VideoDAO;
import lk.sportix.model.AdminLog;
import lk.sportix.model.SavedReport;
import lk.sportix.model.User;
import lk.sportix.model.Video;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AdminService — Member 6: Reporting, Analytics & Admin Management.
 *
 * Moderation results are pushed to users through Member 5's NotificationService:
 *  - approve / reject a video  -> uploader gets VIDEO_APPROVED / VIDEO_REJECTED (Factory pattern)
 *  - approve a video           -> followers of its teams get NEW_VIDEO (Observer pattern)
 *  - resolve / dismiss report  -> reporter gets REPORT_UPDATE (Factory pattern)
 */
public class AdminService {

    private static final Set<String> ROLES = Set.of("USER", "CONTENT_MANAGER", "ADVERTISER", "ADMIN");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "SUSPENDED");

    private final AdminDAO adminDAO = new AdminDAO();
    private final VideoDAO videoDAO = new VideoDAO();
    private final NotificationService notificationService = new NotificationService();

    // ===================================================================================
    // MODERATION
    // ===================================================================================

    public boolean processVideoStatusUpdate(int videoId, String status, int adminId) {
        if (status == null || (!status.equalsIgnoreCase("APPROVED") && !status.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Invalid video status. Must be APPROVED or REJECTED.");
        }
        String newStatus = status.toUpperCase();
        Video video = videoDAO.getVideoByIdAnyStatus(videoId);
        if (video == null) {
            throw new IllegalArgumentException("Video not found.");
        }
        boolean success = adminDAO.updateVideoStatus(videoId, newStatus);
        if (success) {
            adminDAO.logAdminAction(new AdminLog(adminId, newStatus, "videos", videoId, "Video \"" + video.getTitle() + "\" " + newStatus.toLowerCase() + " by Admin"));
            boolean approved = "APPROVED".equals(newStatus);
            notificationService.notifyVideoModeration(video, approved);              // Factory pattern
            if (approved && !"APPROVED".equals(video.getStatus())) {
                notificationService.notifyFollowersOfNewVideo(video);                 // Observer pattern
            }
        }
        return success;
    }

    public boolean processReportStatusUpdate(int reportId, String status, int adminId, String notes) {
        if (status == null || (!status.equalsIgnoreCase("RESOLVED") && !status.equalsIgnoreCase("DISMISSED"))) {
            throw new IllegalArgumentException("Invalid report status. Must be RESOLVED or DISMISSED.");
        }
        Map<String, Object> report = adminDAO.getReportById(reportId);
        boolean success = adminDAO.updateReportStatus(reportId, status.toUpperCase(), adminId, notes);
        if (success) {
            adminDAO.logAdminAction(new AdminLog(adminId, status.toUpperCase(), "content_reports", reportId, "Report " + status.toLowerCase() + " by Admin"));
            if (report != null) {
                notificationService.notifyReportUpdate((Integer) report.get("reporterId"), (String) report.get("videoTitle"), status.toUpperCase());
            }
        }
        return success;
    }

    /** Resolves a report by removing the reported video completely. */
    public boolean removeReportedVideo(int reportId, int adminId) {
        Map<String, Object> report = adminDAO.getReportById(reportId);
        if (report == null || (Integer) report.get("videoId") <= 0) {
            throw new IllegalArgumentException("Report or video not found.");
        }
        int videoId = (Integer) report.get("videoId");
        String title = (String) report.get("videoTitle");
        notificationService.notifyReportUpdate((Integer) report.get("reporterId"), title, "resolved (the video was removed)");
        boolean deleted = adminDAO.deleteVideo(videoId);   // the report rows cascade with the video
        if (deleted) {
            adminDAO.logAdminAction(new AdminLog(adminId, "DELETE_VIDEO", "videos", videoId, "Removed reported video \"" + title + "\""));
        }
        return deleted;
    }

    public List<Map<String, Object>> getPendingVideos() { return adminDAO.getPendingVideos(); }
    public List<Map<String, Object>> getPendingReports() { return adminDAO.getPendingReports(); }

    // ===================================================================================
    // USER MANAGEMENT
    // ===================================================================================

    public List<User> getAllUsers() { return adminDAO.getAllUsers(); }

    public boolean changeUserRole(int adminId, int userId, String role) {
        String r = role == null ? "" : role.trim().toUpperCase();
        if (!ROLES.contains(r)) throw new IllegalArgumentException("Invalid role.");
        if (userId == adminId) throw new IllegalArgumentException("You cannot change your own role.");
        boolean ok = adminDAO.updateUserRole(userId, r);
        if (ok) adminDAO.logAdminAction(new AdminLog(adminId, "CHANGE_ROLE", "users", userId, "Role changed to " + r));
        return ok;
    }

    public boolean changeUserStatus(int adminId, int userId, String status) {
        String s = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(s)) throw new IllegalArgumentException("Invalid account status.");
        if (userId == adminId) throw new IllegalArgumentException("You cannot suspend your own account.");
        boolean ok = adminDAO.updateUserStatus(userId, s);
        if (ok) adminDAO.logAdminAction(new AdminLog(adminId, s.equals("ACTIVE") ? "ACTIVATE_USER" : "SUSPEND_USER", "users", userId, "Account status changed to " + s));
        return ok;
    }

    public boolean deleteUser(int adminId, int userId) {
        if (userId == adminId) throw new IllegalArgumentException("You cannot delete your own account here.");
        boolean ok = adminDAO.deleteUser(userId);
        // The log row would be removed if it referenced the deleted user, so it references the admin.
        if (ok) adminDAO.logAdminAction(new AdminLog(adminId, "DELETE_USER", "users", userId, "User #" + userId + " deleted"));
        return ok;
    }

    // ===================================================================================
    // ANALYTICS, CSV & SAVED REPORTS
    // ===================================================================================

    public String generateVideoAnalyticsCsv(int adminId) {
        List<Map<String, Object>> records = adminDAO.getVideoAnalyticsReport();
        StringBuilder csv = new StringBuilder();

        // CSV Header row
        csv.append("Video ID,Title,Sport,Type,Status,Views,Likes,Comments,Created At\n");

        for (Map<String, Object> row : records) {
            csv.append(row.get("videoId")).append(",")
                    .append(csvCell(row.get("title"))).append(",")
                    .append(csvCell(row.get("sportName"))).append(",")
                    .append(row.get("videoType")).append(",")
                    .append(row.get("status")).append(",")
                    .append(row.get("views")).append(",")
                    .append(row.get("likes")).append(",")
                    .append(row.get("comments")).append(",")
                    .append(row.get("createdAt")).append("\n");
        }

        // Log the export action to admin_logs
        adminDAO.logAdminAction(new AdminLog(adminId, "EXPORT_CSV", "videos", 0, "Exported video analytics report to CSV"));

        return csv.toString();
    }

    /** Escape quotes and commas to avoid CSV parsing errors (and Excel formula injection). */
    private String csvCell(Object value) {
        String raw = value != null ? value.toString() : "";
        if (!raw.isEmpty() && "=+-@".indexOf(raw.charAt(0)) >= 0) raw = "'" + raw;
        return "\"" + raw.replace("\"", "\"\"") + "\"";
    }

    public Map<String, Integer> getSystemStatistics() {
        return adminDAO.getSystemStatistics();
    }

    public List<Map<String, Object>> getRecentLogs() {
        return adminDAO.getRecentLogs(15);
    }

    /** CREATE: saves a snapshot of the current statistics as a report. */
    public boolean saveStatisticsSnapshot(int adminId, String title) {
        String cleanTitle = (title == null || title.isBlank())
                ? "System snapshot " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : title.trim();
        if (cleanTitle.length() > 150) throw new IllegalArgumentException("Report title must be 150 characters or less.");

        Map<String, Integer> s = adminDAO.getSystemStatistics();
        String summary = "Users: " + s.getOrDefault("totalUsers", 0)
                + " | Approved videos: " + s.getOrDefault("totalVideos", 0)
                + " | Pending videos: " + s.getOrDefault("pendingVideos", 0)
                + " | Pending reports: " + s.getOrDefault("pendingReports", 0)
                + " | Total views: " + s.getOrDefault("totalViews", 0)
                + " | Comments: " + s.getOrDefault("totalComments", 0)
                + " | Running campaigns: " + s.getOrDefault("activeCampaigns", 0)
                + " | Ads: " + s.getOrDefault("totalAds", 0)
                + " | Ad impressions: " + s.getOrDefault("adImpressions", 0)
                + " | Ad clicks: " + s.getOrDefault("adClicks", 0);

        SavedReport report = new SavedReport();
        report.setAdminId(adminId);
        report.setReportTitle(cleanTitle);
        report.setReportType("SYSTEM_SNAPSHOT");
        report.setSummaryContent(summary);
        boolean ok = adminDAO.createSavedReport(report);
        if (ok) adminDAO.logAdminAction(new AdminLog(adminId, "SAVE_REPORT", "saved_reports", 0, "Saved report \"" + cleanTitle + "\""));
        return ok;
    }

    public List<SavedReport> getSavedReports() { return adminDAO.getSavedReports(); }

    public boolean renameSavedReport(int reportId, String newTitle) {
        if (newTitle == null || newTitle.isBlank() || newTitle.trim().length() > 150) {
            throw new IllegalArgumentException("Report title is required (maximum 150 characters).");
        }
        return adminDAO.renameSavedReport(reportId, newTitle.trim());
    }

    public boolean deleteSavedReport(int reportId) { return adminDAO.deleteSavedReport(reportId); }
}

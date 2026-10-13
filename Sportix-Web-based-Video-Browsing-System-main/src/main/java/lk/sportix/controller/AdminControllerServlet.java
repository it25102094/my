package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.AdService;
import lk.sportix.service.AdminService;
import lk.sportix.service.NotificationService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

/**
 * AdminControllerServlet — Member 6: Reporting, Analytics & Admin Management.
 *
 * FIXES:
 *  - SECURITY: only checked "is logged in", and AuthFilter treated "/admin" (no trailing slash)
 *    as public, so ANY user could open the admin dashboard and approve/reject videos.
 *    Now the ADMIN role is required here and in AuthFilter.
 *  - MVC: raw SQL moved from this servlet into AdminDAO.
 *  - New admin features: user management, saved report CRUD, advertising oversight,
 *    announcements (Member 5) and the admin activity log.
 */
@WebServlet("/admin")
public class AdminControllerServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final AdService adService = new AdService();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        request.setAttribute("pendingVideos", adminService.getPendingVideos());
        request.setAttribute("pendingReports", adminService.getPendingReports());
        request.setAttribute("systemStats", adminService.getSystemStatistics());
        request.setAttribute("users", adminService.getAllUsers());
        request.setAttribute("savedReports", adminService.getSavedReports());
        request.setAttribute("adminLogs", adminService.getRecentLogs());
        request.setAttribute("campaigns", adService.getAllCampaigns());
        request.setAttribute("announcements", notificationService.getRecentAnnouncements());

        request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkAdmin(request, response)) return;

        int adminId = WebUtil.currentUserId(request);
        String action = request.getParameter("action");
        String anchor = "";

        try {
            if ("updateVideoStatus".equals(action)) {
                int videoId = Integer.parseInt(request.getParameter("videoId"));
                String status = request.getParameter("status");
                adminService.processVideoStatusUpdate(videoId, status, adminId);
                WebUtil.flash(request, "Video " + status.toLowerCase() + ". The uploader (and, if approved, the team followers) were notified.");
                anchor = "#moderation";

            } else if ("updateReportStatus".equals(action)) {
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                String status = request.getParameter("status");
                adminService.processReportStatusUpdate(reportId, status, adminId, request.getParameter("notes"));
                WebUtil.flash(request, "Report " + status.toLowerCase() + ". The reporter was notified.");
                anchor = "#reports";

            } else if ("removeReportedVideo".equals(action)) {
                int reportId = Integer.parseInt(request.getParameter("reportId"));
                adminService.removeReportedVideo(reportId, adminId);
                WebUtil.flash(request, "The reported video was removed.");
                anchor = "#reports";

            } else if ("changeRole".equals(action)) {
                adminService.changeUserRole(adminId, Integer.parseInt(request.getParameter("userId")), request.getParameter("role"));
                WebUtil.flash(request, "User role updated.");
                anchor = "#users";

            } else if ("changeStatus".equals(action)) {
                adminService.changeUserStatus(adminId, Integer.parseInt(request.getParameter("userId")), request.getParameter("status"));
                WebUtil.flash(request, "User account status updated.");
                anchor = "#users";

            } else if ("deleteUser".equals(action)) {
                adminService.deleteUser(adminId, Integer.parseInt(request.getParameter("userId")));
                WebUtil.flash(request, "User deleted.");
                anchor = "#users";

            } else if ("saveReport".equals(action)) {
                adminService.saveStatisticsSnapshot(adminId, request.getParameter("reportTitle"));
                WebUtil.flash(request, "Analytics snapshot saved.");
                anchor = "#saved-reports";

            } else if ("renameReport".equals(action)) {
                adminService.renameSavedReport(Integer.parseInt(request.getParameter("reportId")), request.getParameter("reportTitle"));
                WebUtil.flash(request, "Saved report renamed.");
                anchor = "#saved-reports";

            } else if ("deleteReport".equals(action)) {
                adminService.deleteSavedReport(Integer.parseInt(request.getParameter("reportId")));
                WebUtil.flash(request, "Saved report deleted.");
                anchor = "#saved-reports";

            } else if ("campaignStatus".equals(action)) {
                adService.setCampaignStatus(Integer.parseInt(request.getParameter("campaignId")), request.getParameter("status"), adminId, true);
                WebUtil.flash(request, "Campaign status updated.");
                anchor = "#advertising";

            } else if ("deleteCampaign".equals(action)) {
                adService.deleteCampaign(Integer.parseInt(request.getParameter("campaignId")), adminId, true);
                WebUtil.flash(request, "Campaign deleted.");
                anchor = "#advertising";

            } else {
                WebUtil.flash(request, "Action failed: unknown action.");
            }
        } catch (NumberFormatException e) {
            WebUtil.flash(request, "Action failed: invalid request data.");
        } catch (Exception e) {
            WebUtil.flash(request, "Action failed: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin" + anchor);
    }

    private boolean checkAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (WebUtil.currentUserId(request) <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!WebUtil.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access only.");
            return false;
        }
        return true;
    }
}

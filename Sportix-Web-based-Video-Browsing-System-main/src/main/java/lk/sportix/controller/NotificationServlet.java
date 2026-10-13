package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Notification;
import lk.sportix.service.NotificationService;
import lk.sportix.util.WebUtil;

import java.io.IOException;
import java.util.List;

/**
 * NotificationServlet — Member 5 (IT25102094): Notification Management.
 *
 *   GET  /notifications[?filter=unread]                        READ   inbox
 *   POST /notifications  action=open | markRead | markUnread    UPDATE
 *                        action=markAllRead
 *                        action=delete | clearRead              DELETE
 *   POST /admin/notifications action=broadcast                  CREATE (admin announcement / promotion)
 *                             action=recall                     DELETE (remove an announcement from all inboxes)
 *
 * FIXES: the old servlet forwarded to a JSP that did not exist (/WEB-INF/views/notifications.jsp
 * -> HTTP 500), ran raw SQL in the controller and let any user mark ANY notification as read.
 */
@WebServlet(urlPatterns = {"/notifications", "/admin/notifications"})
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/admin/notifications".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/admin#notifications");
            return;
        }

        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean unreadOnly = "unread".equals(request.getParameter("filter"));
        List<Notification> notifications = notificationService.getNotifications(userId, unreadOnly);

        request.setAttribute("notifications", notifications);
        request.setAttribute("unreadOnly", unreadOnly);
        request.setAttribute("unreadTotal", notificationService.getUnreadCount(userId));
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");

        if ("/admin/notifications".equals(request.getServletPath())) {
            handleAdminAction(request, response, action);
            return;
        }

        int notificationId = WebUtil.parseInt(request.getParameter("notificationId"), 0);
        String back = request.getContextPath() + "/notifications" + ("unread".equals(request.getParameter("filter")) ? "?filter=unread" : "");

        switch (action == null ? "" : action) {
            case "open": {
                // mark as read, then jump to the page the notification points to
                notificationService.markAsRead(notificationId, userId);
                String link = request.getParameter("link");
                boolean safe = link != null && link.startsWith("/") && !link.startsWith("//");
                response.sendRedirect(safe ? request.getContextPath() + link : back);
                return;
            }
            case "markRead":
                notificationService.markAsRead(notificationId, userId);
                break;
            case "markUnread":
                notificationService.markAsUnread(notificationId, userId);
                break;
            case "markAllRead":
                WebUtil.flash(request, notificationService.markAllAsRead(userId) + " notification(s) marked as read.");
                break;
            case "delete":
                WebUtil.flash(request, notificationService.deleteNotification(notificationId, userId)
                        ? "Notification deleted." : "Error: Notification not found.");
                break;
            case "clearRead":
                WebUtil.flash(request, notificationService.clearRead(userId) + " read notification(s) deleted.");
                break;
            default:
                WebUtil.flash(request, "Error: Unknown action.");
        }
        response.sendRedirect(back);
    }

    private void handleAdminAction(HttpServletRequest request, HttpServletResponse response, String action) throws IOException {
        if (!WebUtil.isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can send announcements.");
            return;
        }
        try {
            if ("broadcast".equals(action)) {
                int sent = notificationService.broadcast(
                        request.getParameter("type"),
                        request.getParameter("title"),
                        request.getParameter("message"),
                        request.getParameter("audience"),
                        request.getParameter("link"));
                WebUtil.flash(request, "Notification sent to " + sent + " user(s).");
            } else if ("recall".equals(action)) {
                int removed = notificationService.recallAnnouncement(request.getParameter("title"), request.getParameter("message"));
                WebUtil.flash(request, "Announcement removed from " + removed + " inbox(es).");
            } else {
                WebUtil.flash(request, "Unknown notification action.");
            }
        } catch (IllegalArgumentException e) {
            WebUtil.flash(request, "Action failed: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin#notifications");
    }
}

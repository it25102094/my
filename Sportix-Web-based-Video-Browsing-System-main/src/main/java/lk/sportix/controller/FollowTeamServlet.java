package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.FollowTeamService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

/**
 * FollowTeamServlet — Member 1: follow_teams CRUD.
 *   action=follow (default) | unfollow | notifyOn | notifyOff
 * Followers with "notify on upload" switched on are the Observers of Member 5's notification
 * system (they get an alert when the team posts a new video).
 * FIX: the redirect checked request.getRequestURI().contains("profile") — the URI is always
 * /user/follow, so users were always sent to /home. A "from" parameter is used now.
 */
@WebServlet("/user/follow")
public class FollowTeamServlet extends HttpServlet {

    private final FollowTeamService followTeamService = new FollowTeamService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int teamId = WebUtil.parseInt(request.getParameter("teamId"), 0);
        int videoId = WebUtil.parseInt(request.getParameter("videoId"), 0);
        String action = request.getParameter("action");
        String teamName = request.getParameter("teamName");
        String label = (teamName != null && !teamName.isBlank()) ? teamName.trim() : "the team";

        if (teamId > 0) {
            // MVC: Delegating insert/update/delete to FollowTeamService
            if ("unfollow".equals(action)) {
                followTeamService.unfollowTeam(userId, teamId);
                WebUtil.flash(request, "You have unfollowed " + label + ".");
            } else if ("notifyOn".equals(action) || "notifyOff".equals(action)) {
                boolean on = "notifyOn".equals(action);
                followTeamService.updateNotificationPreference(userId, teamId, on);
                WebUtil.flash(request, on ? "Upload alerts switched ON for " + label + "."
                        : "Upload alerts switched OFF for " + label + ".");
            } else {
                followTeamService.followTeam(userId, teamId, true);
                WebUtil.flash(request, "Success! You are now following " + label
                        + ". You will get a notification when they post a new video.");
            }
        }

        // SMART REDIRECT
        if (videoId > 0) {
            response.sendRedirect(request.getContextPath() + "/video?id=" + videoId);
        } else if ("profile".equals(request.getParameter("from"))) {
            response.sendRedirect(request.getContextPath() + "/profile#teams");
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}

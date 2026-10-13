package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.FollowTeamService;

import java.io.IOException;

@WebServlet("/user/follow")
public class FollowTeamServlet extends HttpServlet {

    private final FollowTeamService followTeamService = new FollowTeamService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        int teamId = 0;
        int videoId = 0;
        String action = request.getParameter("action");
        String teamName = request.getParameter("teamName");

        try { teamId = Integer.parseInt(request.getParameter("teamId")); } catch (Exception ignored) {}
        try { videoId = Integer.parseInt(request.getParameter("videoId")); } catch (Exception ignored) {}

        if (teamId > 0) {
            // MVC Fixed: Delegating insert/delete to FollowTeamService
            if ("unfollow".equals(action)) {
                followTeamService.unfollowTeam(userId, teamId);
                session.setAttribute("flashMessage", "You have unfollowed " + (teamName != null ? teamName : "the team") + ".");
            } else {
                followTeamService.followTeam(userId, teamId, true);
                session.setAttribute("flashMessage", "Success! You are now following " + (teamName != null ? teamName : "the team") + ".");
            }
        }

        // SMART REDIRECT
        if (videoId > 0) {
            response.sendRedirect(request.getContextPath() + "/video?id=" + videoId);
        } else {
            // FIX: Use getRequestURI() instead of requestURI()
            response.sendRedirect(request.getRequestURI().contains("profile")
                    ? request.getContextPath() + "/profile"
                    : request.getContextPath() + "/home");
        }
    }
}
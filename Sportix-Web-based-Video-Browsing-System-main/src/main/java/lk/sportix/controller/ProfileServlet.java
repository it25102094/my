package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.sportix.model.User;
import lk.sportix.service.FollowTeamService;
import lk.sportix.service.UserService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

@WebServlet(urlPatterns = {"/profile", "/user/edit-profile", "/forgot-password"})
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserService();
    private final FollowTeamService followTeamService = new FollowTeamService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/forgot-password".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }

        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = userService.getUserById(userId);
        if (user == null) {               // account was deleted by an admin
            request.getSession().invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getSession().setAttribute("loggedUser", user);
        request.setAttribute("user", user);

        // Fetch followed teams + all teams (with follow status) via Service
        request.setAttribute("followedTeams", followTeamService.getFollowedTeams(userId));
        request.setAttribute("allTeams", followTeamService.getAllTeamsWithStatus(userId));

        if ("/user/edit-profile".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/edit-profile.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        // 1. Forgot Password Form Submission
        if ("/forgot-password".equals(path)) {
            String email = request.getParameter("email");
            String phone = request.getParameter("contactNumber");
            String newPass = request.getParameter("newPassword");

            // MVC: Controller calls Service (which handles BCrypt and DAO)
            if (userService.resetPassword(email, phone, newPass)) {
                response.sendRedirect(request.getContextPath() + "/login?resetSuccess=true");
                return;
            }
            request.setAttribute("error", "Invalid details or password shorter than 8 characters.");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }

        // Check login session for all other actions
        HttpSession session = request.getSession(false);
        int userId = WebUtil.currentUserId(request);
        if (userId <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        // (FIX: the dead "/toggle-spoiler" branch was removed — ToggleSpoilerServlet handles that URL.)

        String action = request.getParameter("action");

        if ("updateProfile".equals(action)) {
            User user = userService.getUserById(userId);
            user.setFirstName(request.getParameter("firstName"));
            user.setLastName(request.getParameter("lastName"));
            user.setContactNumber(request.getParameter("contactNumber"));
            user.setFavouriteSports(request.getParameter("favouriteSports"));
            user.setHideScores(request.getParameter("hideScores") != null);
            if ("ADVERTISER".equals(user.getRole())) {
                String company = request.getParameter("companyName");
                user.setCompanyName(company != null && !company.isBlank() ? company.trim() : user.getCompanyName());
            }

            if (userService.updateProfile(user)) {
                session.setAttribute("loggedUser", user);
                session.setAttribute("displayName", user.getFirstName());
                session.setAttribute("spoilerFree", user.isHideScores());
                response.sendRedirect(request.getContextPath() + "/profile?updated=true");
            } else {
                request.setAttribute("error", "Could not update profile. First name is required and the contact number must have 9-15 digits.");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/edit-profile.jsp").forward(request, response);
            }

        } else if ("changePassword".equals(action)) {
            String currentPass = request.getParameter("currentPassword");
            String newPass = request.getParameter("newPassword");

            if (userService.changePassword(userId, currentPass, newPass)) {
                response.sendRedirect(request.getContextPath() + "/profile?pwdChanged=true");
            } else {
                request.setAttribute("error", "Current password is wrong or new password is under 8 characters.");
                request.setAttribute("user", userService.getUserById(userId));
                request.getRequestDispatcher("/WEB-INF/views/edit-profile.jsp").forward(request, response);
            }

        } else if ("deleteAccount".equals(action)) {
            if (userService.deleteAccount(userId)) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?deleted=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/profile?error=deleteFailed");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}

package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.sportix.model.User;
import lk.sportix.service.UserService;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = userService.login(request.getParameter("username"), request.getParameter("password"));

        if (user == null) {
            request.setAttribute("error", "Invalid username/email or password, or account suspended.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        // Establish a secure session
        HttpSession session = request.getSession(true);
        request.changeSessionId(); // Prevents session fixation attacks

        // Bind user details to the session (matches includes/navbar.jsp and video-card.jsp)
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("loggedUser", user);
        session.setAttribute("displayName", user.getFirstName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("spoilerFree", user.isSpoilerFree());

        // Role-Based Redirection: Send ADMINs straight to the Admin Dashboard
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}



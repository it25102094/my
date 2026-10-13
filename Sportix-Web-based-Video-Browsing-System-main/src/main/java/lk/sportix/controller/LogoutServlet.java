package lk.sportix.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    // CHANGED from doPost to doGet
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Destroys the user session
        }
        // Redirects back to home
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
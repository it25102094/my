package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.UserService;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // FIX: contact number (needed by Forgot Password), account type and company name are captured,
        // and the user now sees the REAL validation problem instead of one generic message.
        String error = userService.register(
                request.getParameter("username"),
                request.getParameter("firstName"),
                request.getParameter("lastName"),
                request.getParameter("email"),
                request.getParameter("password"),
                request.getParameter("contactNumber"),
                request.getParameter("accountType"),
                request.getParameter("companyName"));

        if (error == null) {
            // Success: Redirect to login page
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } else {
            // Failure: Forward back to the form with the error message (form values are kept via ${param})
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}

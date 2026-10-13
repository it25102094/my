package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.UserService;

import java.io.IOException;

/**
 * ToggleSpoilerServlet — Flips Hide Scores (Spoiler-Free Mode) in the current session
 * and persists the preference in the 'users' database table.
 */
@WebServlet("/toggle-spoiler")
public class ToggleSpoilerServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        boolean spoilerFree = Boolean.parseBoolean(request.getParameter("spoilerFree"));
        session.setAttribute("spoilerFree", spoilerFree);

        // Also save to MySQL database if userId is present in session
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof Integer) {
            userService.updateHideScores((Integer) userIdObj, spoilerFree);
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
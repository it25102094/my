package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lk.sportix.model.WatchLater;
import lk.sportix.service.WatchLaterService;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/user/watchlater", "/watch-later"})
public class WatchLaterServlet extends HttpServlet {

    private final WatchLaterService watchLaterService = new WatchLaterService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");

        // MVC Fixed: Controller calls Service to read data
        List<WatchLater> items = watchLaterService.getWatchLaterItems(userId, null);
        request.setAttribute("watchLaterVideos", items);
        request.setAttribute("folders", watchLaterService.getUserFolders(userId));

        request.getRequestDispatcher("/WEB-INF/views/watch-later.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        int videoId = parseIntSafe(request.getParameter("videoId"));

        if ("remove".equals(action) && videoId > 0) {
            // MVC Fixed: Controller calls Service to delete data
            watchLaterService.removeByUserAndVideo(userId, videoId);
            session.setAttribute("flashMessage", "Removed from Watch Later.");
        } else if (("add".equals(action) || "addItem".equals(action) || action == null) && videoId > 0) {
            // MVC Fixed: Controller calls Service to create data
            watchLaterService.addVideoToWatchLater(userId, videoId, null, null);
            session.setAttribute("flashMessage", "✓ Saved to Watch Later!");
        }

        // SMART REDIRECT: Return to the referring page if clicked from video details
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/video")) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/user/watchlater");
        }
    }

    private int parseIntSafe(String val) {
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return 0; }
    }
}
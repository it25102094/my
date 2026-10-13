package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.dao.SearchDAO;
import lk.sportix.model.Video;
import lk.sportix.service.VideoService;
import lk.sportix.strategy.MostViewedSortStrategy;
import lk.sportix.strategy.NewestSortStrategy;
import lk.sportix.strategy.SearchStrategy;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * SearchServlet — Handles GET /search?q={keyword}&sort={VIEWS|NEWEST}
 * Applies Strategy Pattern sorting and handles Saved Search CRUD operations via POST.
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();
    private final SearchDAO searchDAO = new SearchDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Supports both ?q=... (from navbar.jsp) and ?keyword=...
        String keyword = request.getParameter("q");
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = request.getParameter("keyword");
        }
        if (keyword == null) {
            keyword = "";
        }

        String sortParam = request.getParameter("sort");
        if (sortParam == null || sortParam.trim().isEmpty()) {
            sortParam = "VIEWS";
        }

        // 1. Fetch matching videos from VideoService based on keyword search
        List<Video> results = videoService.searchVideos(keyword);

        // 2. Apply Strategy Design Pattern to sort results dynamically (Most Viewed vs Newest First)
        SearchStrategy strategy = "NEWEST".equalsIgnoreCase(sortParam)
                ? new NewestSortStrategy()
                : new MostViewedSortStrategy();
        results = strategy.sort(results);

        // 3. Load Saved Searches if user is logged in (READ CRUD)
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") instanceof Integer) {
            int userId = (Integer) session.getAttribute("userId");
            request.setAttribute("savedSearches", searchDAO.getSavedSearchesByUser(userId));
        }

        request.setAttribute("keyword", keyword.trim());
        request.setAttribute("currentSort", sortParam.toUpperCase());
        request.setAttribute("results", results);

        request.getRequestDispatcher("/WEB-INF/views/search-results.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Integer)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String action = request.getParameter("action");
        String keyword = request.getParameter("q");
        if (keyword == null) keyword = "";

        if ("save".equalsIgnoreCase(action)) {
            String label = request.getParameter("label");
            String sortPref = request.getParameter("sortPref");
            if (label != null && !label.trim().isEmpty()) {
                searchDAO.addSavedSearch(userId, label.trim(), keyword.trim(), sortPref);
            }
        } else if ("update".equalsIgnoreCase(action)) {
            int id = Integer.parseInt(request.getParameter("savedSearchId"));
            String newLabel = request.getParameter("label");
            String newSort = request.getParameter("sortPref");
            searchDAO.updateSavedSearch(id, userId, newLabel, newSort);
        } else if ("delete".equalsIgnoreCase(action)) {
            int id = Integer.parseInt(request.getParameter("savedSearchId"));
            searchDAO.deleteSavedSearch(id, userId);
        }

        response.sendRedirect(request.getContextPath() + "/search?q=" +
                URLEncoder.encode(keyword, StandardCharsets.UTF_8));
    }
}
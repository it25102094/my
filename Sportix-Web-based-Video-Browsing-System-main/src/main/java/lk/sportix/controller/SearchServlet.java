package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.dao.SearchDAO;
import lk.sportix.model.Video;
import lk.sportix.service.AdService;
import lk.sportix.service.VideoService;
import lk.sportix.strategy.MostViewedSortStrategy;
import lk.sportix.strategy.NewestSortStrategy;
import lk.sportix.strategy.SearchStrategy;
import lk.sportix.util.WebUtil;

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
    private final AdService adService = new AdService();

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

        String sortParam = normaliseSort(request.getParameter("sort"));

        // 1. Fetch matching videos from VideoService based on keyword search
        List<Video> results = videoService.searchVideos(keyword);

        // 2. Apply Strategy Design Pattern to sort results dynamically (Most Viewed vs Newest First)
        SearchStrategy strategy = "NEWEST".equals(sortParam)
                ? new NewestSortStrategy()
                : new MostViewedSortStrategy();
        results = strategy.sort(results);

        // 3. Load Saved Searches if user is logged in (READ CRUD)
        int userId = WebUtil.currentUserId(request);
        if (userId > 0) {
            request.setAttribute("savedSearches", searchDAO.getSavedSearchesByUser(userId));
        }

        request.setAttribute("keyword", keyword.trim());
        request.setAttribute("currentSort", sortParam);
        request.setAttribute("sortAction", "/search");
        request.setAttribute("results", results);
        request.setAttribute("searchAds", adService.getAdsForPlacement(AdService.PLACEMENT_SEARCH, 1)); // Member 5

        request.getRequestDispatcher("/WEB-INF/views/search-results.jsp").forward(request, response);
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
        String keyword = request.getParameter("q");
        if (keyword == null) keyword = "";
        int savedSearchId = WebUtil.parseInt(request.getParameter("savedSearchId"), 0);
        String label = request.getParameter("label");

        if ("save".equalsIgnoreCase(action)) {
            if (label != null && !label.trim().isEmpty() && label.trim().length() <= 100 && !keyword.trim().isEmpty()) {
                searchDAO.addSavedSearch(userId, label.trim(), keyword.trim(), normaliseSort(request.getParameter("sortPref")));
                WebUtil.flash(request, "Search saved.");
            } else {
                WebUtil.flash(request, "Please enter a label (max 100 characters) to save this search.");
            }
        } else if ("update".equalsIgnoreCase(action) && savedSearchId > 0) {
            if (label != null && !label.trim().isEmpty() && label.trim().length() <= 100) {
                searchDAO.updateSavedSearch(savedSearchId, userId, label.trim(), normaliseSort(request.getParameter("sortPref")));
                WebUtil.flash(request, "Saved search updated.");
            } else {
                WebUtil.flash(request, "Label cannot be empty.");
            }
        } else if ("delete".equalsIgnoreCase(action) && savedSearchId > 0) {
            searchDAO.deleteSavedSearch(savedSearchId, userId);
            WebUtil.flash(request, "Saved search deleted.");
        }

        response.sendRedirect(request.getContextPath() + "/search?q=" +
                URLEncoder.encode(keyword, StandardCharsets.UTF_8));
    }

    private String normaliseSort(String sort) {
        return "NEWEST".equalsIgnoreCase(sort) ? "NEWEST" : "VIEWS";
    }
}

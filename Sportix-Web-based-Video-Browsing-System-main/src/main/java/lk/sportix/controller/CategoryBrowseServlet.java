package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Video;
import lk.sportix.service.AdService;
import lk.sportix.service.VideoService;
import lk.sportix.strategy.MostViewedSortStrategy;
import lk.sportix.strategy.NewestSortStrategy;
import lk.sportix.strategy.SearchStrategy;

import java.io.IOException;
import java.util.List;

/**
 * CategoryBrowseServlet — Handles navigation tabs (/live, /highlights, /shorts, /interviews, /trending, /categories)
 * and applies the Strategy Pattern for sorting.
 */
@WebServlet(urlPatterns = {"/live", "/highlights", "/shorts", "/interviews", "/trending", "/categories"})
public class CategoryBrowseServlet extends HttpServlet {

    private final VideoService videoService = new VideoService();
    private final AdService adService = new AdService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String label;
        List<Video> results;

        switch (path) {
            case "/live":
                label = "Live Now";
                results = videoService.getVideosByCategory("LIVE", 20);
                break;
            case "/highlights":
                label = "Match Highlights";
                results = videoService.getVideosByCategory("HIGHLIGHT", 20);
                break;
            case "/shorts":
                label = "Sports Shorts";
                results = videoService.getVideosByCategory("SHORT", 20);
                break;
            case "/interviews":
                label = "Exclusive Interviews";
                results = videoService.getVideosByCategory("INTERVIEW", 20);
                break;
            case "/trending":
                label = "Top Trending";
                results = videoService.getTrendingVideos(20);
                break;
            default:
                String sport = request.getParameter("sport");
                if (sport != null && !sport.trim().isEmpty()) {
                    label = sport.trim();
                    results = videoService.getVideosBySportName(sport);
                } else {
                    label = "All Sports";
                    results = videoService.getTrendingVideos(20);
                }
                break;
        }

        // Apply Strategy Pattern Sorting based on ?sort= parameter
        String sortParam = "NEWEST".equalsIgnoreCase(request.getParameter("sort")) ? "NEWEST" : "VIEWS";
        SearchStrategy strategy = "NEWEST".equalsIgnoreCase(sortParam)
                ? new NewestSortStrategy()
                : new MostViewedSortStrategy();
        results = strategy.sort(results);

        request.setAttribute("keyword", label);
        request.setAttribute("currentSort", sortParam.toUpperCase());
        request.setAttribute("results", results);
        request.setAttribute("browseMode", true);
        request.setAttribute("sortAction", path);   // the sort drop-down must re-submit to the same tab
        request.setAttribute("searchAds", adService.getAdsForPlacement(AdService.PLACEMENT_SEARCH, 1)); // Member 5

        request.getRequestDispatcher("/WEB-INF/views/search-results.jsp").forward(request, response);
    }
}
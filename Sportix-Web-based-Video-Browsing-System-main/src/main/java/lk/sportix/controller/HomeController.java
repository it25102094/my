package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Video;
import lk.sportix.service.VideoService;

import java.io.IOException;
import java.util.List;

// Maps to both the root URL and /home explicitly
@WebServlet(urlPatterns = {"", "/home"})
public class HomeController extends HttpServlet {

    private final VideoService videoService = new VideoService();

    @Override
           protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Fetch data via the service layer
        Video featuredVideo = videoService.getFeaturedVideo();
        List<Video> trendingVideos = videoService.getTrendingVideos(4);

        // Fetch category rows
        List<Video> liveVideos = videoService.getVideosByCategory("LIVE", 4);
        List<Video> shortVideos = videoService.getVideosByCategory("SHORT", 6);
        List<Video> highlightVideos = videoService.getVideosByCategory("HIGHLIGHT", 4);

        // NEW: Fetch Interview videos!
        List<Video> interviewVideos = videoService.getVideosByCategory("INTERVIEW", 4);

        // Bind data to the request scope for the JSP
        request.setAttribute("featuredVideo", featuredVideo);
        request.setAttribute("trendingVideos", trendingVideos);
        request.setAttribute("liveVideos", liveVideos);
        request.setAttribute("shortVideos", shortVideos);
        request.setAttribute("highlightVideos", highlightVideos);

        // NEW: Send Interviews to the JSP
        request.setAttribute("interviewVideos", interviewVideos);

        // Forward to the view
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
        }


package lk.sportix.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.NotificationService;

import java.io.IOException;

/**
 * NotificationBadgeFilter — Member 5 (IT25102094).
 * For every page request of a logged-in user it puts the number of UNREAD notifications into
 * the request ("unreadNotificationCount"), so includes/navbar.jsp can show the red badge on
 * the bell icon on every page. Static files (css/js/images) are skipped.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class NotificationBadgeFilter implements Filter {

    private final NotificationService notificationService = new NotificationService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        boolean staticResource = path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.endsWith(".ico") || path.endsWith(".png") || path.endsWith(".jpg");

        if (!staticResource && "GET".equalsIgnoreCase(httpRequest.getMethod())) {
            HttpSession session = httpRequest.getSession(false);
            Object userId = (session != null) ? session.getAttribute("userId") : null;
            if (userId instanceof Integer) {
                try {
                    request.setAttribute("unreadNotificationCount", notificationService.getUnreadCount((Integer) userId));
                } catch (RuntimeException e) {
                    // never break a page because the badge could not be loaded
                    request.setAttribute("unreadNotificationCount", 0);
                }
            }
        }
        chain.doFilter(request, response);
    }
}

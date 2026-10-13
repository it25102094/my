package lk.sportix.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthFilter - Servlet Filter enforcing Role-Based Access Control (RBAC).
 * Intercepts requests to protected areas BEFORE they reach any Servlet.
 *
 * FIX (security): the mapping "/admin/*" also matches "/admin", but the role check only tested
 * path.startsWith("/admin/"), so "/admin" (the Admin Dashboard) was treated as PUBLIC.
 * Exact paths are now matched too, and the notifications inbox requires a login.
 */
@WebFilter(urlPatterns = {"/admin", "/admin/*", "/manager/*", "/content/*", "/advertiser/*", "/campaigns/*",
        "/campaign", "/user/*", "/profile", "/notifications"})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        HttpSession session = httpRequest.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        boolean authorized;

        if (matches(path, "/admin")) {
            authorized = "ADMIN".equals(role);

        } else if (matches(path, "/manager") || matches(path, "/content")) {
            authorized = "CONTENT_MANAGER".equals(role) || "ADMIN".equals(role);

        } else if (matches(path, "/advertiser") || matches(path, "/campaigns") || matches(path, "/campaign")) {
            authorized = "ADVERTISER".equals(role) || "ADMIN".equals(role);

        } else if (matches(path, "/user") || matches(path, "/profile") || matches(path, "/notifications")) {
            authorized = role != null;

        } else {
            authorized = true;
        }

        if (authorized) {
            chain.doFilter(request, response);
        } else if (role == null) {
            httpResponse.sendRedirect(contextPath + "/login");
        } else {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this resource.");
        }
    }

    /** True for the exact path ("/admin") and everything below it ("/admin/..."). */
    private static boolean matches(String path, String base) {
        return path.equals(base) || path.startsWith(base + "/");
    }

    @Override
    public void destroy() {
    }
}

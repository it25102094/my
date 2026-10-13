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
 */
@WebFilter(urlPatterns = {"/admin/*", "/manager/*", "/content/*", "/advertiser/*", "/campaigns/*", "/user/*", "/profile"})
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

        if (path.startsWith("/admin/")) {
            authorized = "ADMIN".equals(role);

        } else if (path.startsWith("/manager/") || path.startsWith("/content/")) {
            authorized = "CONTENT_MANAGER".equals(role) || "ADMIN".equals(role);

        } else if (path.startsWith("/advertiser/") || path.startsWith("/campaigns")) {
            authorized = "ADVERTISER".equals(role) || "ADMIN".equals(role);

        } else if (path.startsWith("/user/") || path.startsWith("/profile")) {
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

    @Override
    public void destroy() {
    }
}
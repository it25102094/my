package lk.sportix.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Small helper methods shared by the servlets (session user, role checks, safe number
 * parsing and one-time "flash" messages), so every controller does it the same way.
 */
public final class WebUtil {

    private WebUtil() {
    }

    /** Logged-in user id, or -1 when nobody is logged in. */
    public static int currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = (session != null) ? session.getAttribute("userId") : null;
        return (id instanceof Integer) ? (Integer) id : -1;
    }

    /** Role of the logged-in user (USER, CONTENT_MANAGER, ADVERTISER, ADMIN) or null. */
    public static String currentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object role = (session != null) ? session.getAttribute("role") : null;
        return (role instanceof String) ? (String) role : null;
    }

    public static boolean hasRole(HttpServletRequest request, String... roles) {
        String role = currentRole(request);
        if (role == null) return false;
        for (String r : roles) {
            if (r.equals(role)) return true;
        }
        return false;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }

    /** Parses an int request value; returns the fallback for null / invalid input (no 500 errors). */
    public static int parseInt(String value, int fallback) {
        try {
            return (value == null) ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Stores a one-time message that the next page shows (and removes) in its flash banner. */
    public static void flash(HttpServletRequest request, String message) {
        request.getSession(true).setAttribute("flashMessage", message);
    }
}

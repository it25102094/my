package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.service.AdService;
import lk.sportix.util.WebUtil;

import java.io.IOException;

/**
 * AdvertisementServlet — Member 5 (IT25102094): Advertisement CRUD + click tracking.
 *
 *   POST /advertiser/ad   action=create | update | status | delete   (ADVERTISER / ADMIN)
 *   GET  /ad/click?id=N   public: counts a click, then redirects the viewer to the advertiser's site
 *
 * The ads themselves are displayed by HomeController, VideoDetailsServlet and SearchServlet
 * through AdService.getAdsForPlacement(...) and the includes/ad-slot.jsp view.
 */
@WebServlet(urlPatterns = {"/advertiser/ad", "/ad/click"})
public class AdvertisementServlet extends HttpServlet {

    private final AdService adService = new AdService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if ("/ad/click".equals(request.getServletPath())) {
            String target = adService.registerClick(WebUtil.parseInt(request.getParameter("id"), 0));
            response.sendRedirect(target != null ? target : request.getContextPath() + "/home");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/advertiser/dashboard#ads");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (WebUtil.currentUserId(request) <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!WebUtil.hasRole(request, "ADVERTISER", "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only advertiser accounts can manage advertisements.");
            return;
        }

        int userId = WebUtil.currentUserId(request);
        boolean isAdmin = WebUtil.isAdmin(request);
        String action = request.getParameter("action");
        int adId = WebUtil.parseInt(request.getParameter("adId"), 0);
        String anchor = "#ads";

        try {
            if (action == null || "create".equals(action)) {
                adService.createAd(userId, isAdmin,
                        request.getParameter("campaignId"),
                        request.getParameter("adTitle"),
                        request.getParameter("adType"),
                        request.getParameter("adContent"),
                        request.getParameter("mediaUrl"),
                        request.getParameter("targetUrl"),
                        request.getParameter("status"));
                WebUtil.flash(request, "Success: Advertisement created. Active ads in a running campaign are shown to viewers immediately.");

            } else if ("update".equals(action)) {
                adService.updateAd(adId, userId, isAdmin,
                        request.getParameter("campaignId"),
                        request.getParameter("adTitle"),
                        request.getParameter("adType"),
                        request.getParameter("adContent"),
                        request.getParameter("mediaUrl"),
                        request.getParameter("targetUrl"),
                        request.getParameter("status"));
                WebUtil.flash(request, "Success: Advertisement updated.");

            } else if ("status".equals(action)) {
                String status = request.getParameter("status");
                if (adService.setAdStatus(adId, status, userId, isAdmin)) {
                    WebUtil.flash(request, "Success: Advertisement is now " + status.toUpperCase() + ".");
                } else {
                    WebUtil.flash(request, "Error: Advertisement not found or access denied.");
                }

            } else if ("delete".equals(action)) {
                if (adService.deleteAd(adId, userId, isAdmin)) {
                    WebUtil.flash(request, "Success: Advertisement deleted.");
                } else {
                    WebUtil.flash(request, "Error: Advertisement not found or access denied.");
                }
            } else {
                WebUtil.flash(request, "Error: Unknown action.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            WebUtil.flash(request, e.getMessage());
            if ("update".equals(action) && adId > 0) {
                anchor = "?editAd=" + adId + "#ad-form";
            }
        } catch (Exception e) {
            WebUtil.flash(request, "Error: An unexpected system error occurred.");
        }

        response.sendRedirect(request.getContextPath() + "/advertiser/dashboard" + anchor);
    }
}

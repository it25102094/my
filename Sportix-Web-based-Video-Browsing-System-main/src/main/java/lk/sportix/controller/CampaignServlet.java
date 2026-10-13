package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.sportix.model.Advertisement;
import lk.sportix.model.Campaign;
import lk.sportix.service.AdService;
import lk.sportix.util.WebUtil;

import java.io.IOException;
import java.util.List;

/**
 * CampaignServlet — Member 5 (IT25102094): Advertising Management.
 *
 *   GET  /advertiser/dashboard            -> READ: campaigns + ads + performance stats
 *        /advertiser/dashboard?editCampaign=ID / ?editAd=ID  -> pre-fills the edit forms
 *   POST /advertiser/campaign  action=create | update | status | delete   (campaign CRUD)
 *
 * FIXES: the old servlet only handled "create", had no role check (any logged-in viewer could
 * create campaigns), wrote to a non-existent table and redirected to a static JSP outside
 * WEB-INF that never loaded any data — so created campaigns/ads were never displayed.
 * Access: ADVERTISER (own campaigns) or ADMIN (all campaigns). Also protected by AuthFilter.
 */
@WebServlet(urlPatterns = {"/advertiser/dashboard", "/advertiser/campaign", "/campaign"})
public class CampaignServlet extends HttpServlet {

    private final AdService adService = new AdService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!"/advertiser/dashboard".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/advertiser/dashboard");
            return;
        }
        if (!checkAccess(request, response)) return;

        int userId = WebUtil.currentUserId(request);
        boolean isAdmin = WebUtil.isAdmin(request);

        List<Campaign> campaigns = isAdmin ? adService.getAllCampaigns() : adService.getCampaignsForAdvertiser(userId);
        List<Advertisement> ads = isAdmin ? adService.getAllAds() : adService.getAdsForAdvertiser(userId);

        request.setAttribute("campaigns", campaigns);
        request.setAttribute("ads", ads);
        request.setAttribute("stats", adService.buildStats(campaigns, ads));
        request.setAttribute("isAdminView", isAdmin);
        request.setAttribute("today", java.time.LocalDate.now().toString());

        // Edit mode (UPDATE forms are pre-filled with the selected row)
        int editCampaignId = WebUtil.parseInt(request.getParameter("editCampaign"), 0);
        if (editCampaignId > 0) {
            Campaign editCampaign = adService.getCampaign(editCampaignId, userId, isAdmin);
            if (editCampaign == null) {
                WebUtil.flash(request, "Error: Campaign not found or access denied.");
            }
            request.setAttribute("editCampaign", editCampaign);
        }
        int editAdId = WebUtil.parseInt(request.getParameter("editAd"), 0);
        if (editAdId > 0) {
            Advertisement editAd = adService.getAd(editAdId, userId, isAdmin);
            if (editAd == null) {
                WebUtil.flash(request, "Error: Advertisement not found or access denied.");
            }
            request.setAttribute("editAd", editAd);
        }

        request.getRequestDispatcher("/WEB-INF/views/advertiser-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAccess(request, response)) return;

        int userId = WebUtil.currentUserId(request);
        boolean isAdmin = WebUtil.isAdmin(request);
        String action = request.getParameter("action");
        int campaignId = WebUtil.parseInt(request.getParameter("campaignId"), 0);
        String anchor = "#campaigns";

        try {
            if (action == null || "create".equals(action)) {
                adService.createCampaign(userId,
                        request.getParameter("campaignName"),
                        request.getParameter("budget"),
                        request.getParameter("priority") != null ? request.getParameter("priority") : request.getParameter("priorityLevel"),
                        request.getParameter("startDate"),
                        request.getParameter("endDate"),
                        request.getParameter("status"));
                WebUtil.flash(request, "Success: Campaign '" + request.getParameter("campaignName").trim() + "' has been created. Now add ads to it below.");

            } else if ("update".equals(action)) {
                adService.updateCampaign(campaignId, userId, isAdmin,
                        request.getParameter("campaignName"),
                        request.getParameter("budget"),
                        request.getParameter("priority"),
                        request.getParameter("startDate"),
                        request.getParameter("endDate"),
                        request.getParameter("status"));
                WebUtil.flash(request, "Success: Campaign updated.");

            } else if ("status".equals(action)) {
                String status = request.getParameter("status");
                if (adService.setCampaignStatus(campaignId, status, userId, isAdmin)) {
                    WebUtil.flash(request, "Success: Campaign is now " + status.toUpperCase() + ".");
                } else {
                    WebUtil.flash(request, "Error: Campaign not found or access denied.");
                }

            } else if ("delete".equals(action)) {
                if (adService.deleteCampaign(campaignId, userId, isAdmin)) {
                    WebUtil.flash(request, "Success: Campaign and all of its ads were deleted.");
                } else {
                    WebUtil.flash(request, "Error: Campaign not found or access denied.");
                }
            } else {
                WebUtil.flash(request, "Error: Unknown action.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Validation messages from AdService (they already start with "Error:")
            WebUtil.flash(request, e.getMessage());
            if ("update".equals(action) && campaignId > 0) {
                anchor = "?editCampaign=" + campaignId + "#campaign-form";
            }
        } catch (Exception e) {
            WebUtil.flash(request, "Error: An unexpected system error occurred.");
        }

        // Redirect back to the dashboard (Post/Redirect/Get) to show the result
        response.sendRedirect(request.getContextPath() + "/advertiser/dashboard" + anchor);
    }

    /** Only advertisers and admins may manage campaigns. */
    private boolean checkAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (WebUtil.currentUserId(request) <= 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!WebUtil.hasRole(request, "ADVERTISER", "ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only advertiser accounts can manage campaigns.");
            return false;
        }
        return true;
    }
}

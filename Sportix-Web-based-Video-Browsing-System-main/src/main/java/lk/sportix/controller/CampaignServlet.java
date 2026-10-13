package lk.sportix.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lk.sportix.service.AdService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * CampaignServlet — Handles campaign creation requests for Member 5 (Advertising Management).
 */
@WebServlet("/campaign")
public class CampaignServlet extends HttpServlet {

    private final AdService adService = new AdService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Security Check: User must be logged in to create a campaign
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int advertiserId = (Integer) session.getAttribute("userId");
        String campaignName = request.getParameter("campaignName");
        String priorityLevel = request.getParameter("priorityLevel");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");

        try {
            // Parse HTML5 date strings (YYYY-MM-DD) into Java LocalDate objects
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            // Pass to service layer for business logic validation
            boolean success = adService.createCampaign(advertiserId, campaignName, priorityLevel, startDate, endDate);

            if (success) {
                session.setAttribute("flashMessage", "Success: Campaign '" + campaignName + "' has been scheduled.");
            } else {
                session.setAttribute("flashMessage", "Error: Database rejected the campaign.");
            }

        } catch (DateTimeParseException e) {
            session.setAttribute("flashMessage", "Error: Invalid date format. Please select valid calendar dates.");
        } catch (IllegalArgumentException e) {
            // Catches the strict date validation errors from AdService
            session.setAttribute("flashMessage", e.getMessage());
        } catch (Exception e) {
            session.setAttribute("flashMessage", "An unexpected system error occurred.");
        }

        // Redirect back to the dashboard to show the success or error message
        response.sendRedirect(request.getContextPath() + "/advertiser-dashboard.jsp");
    }
}
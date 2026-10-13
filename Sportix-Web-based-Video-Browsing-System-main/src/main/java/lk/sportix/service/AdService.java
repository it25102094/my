package lk.sportix.service;

import lk.sportix.dao.AdCampaignDAO;
import java.time.LocalDate;

/**
 * AdService — Handles business logic and validation for Member 5's Advertising Management.
 */
public class AdService {

    private final AdCampaignDAO adCampaignDAO = new AdCampaignDAO();

    /**
     * Validates and creates a new advertisement campaign.
     */
    public boolean createCampaign(int advertiserId, String campaignName, String priorityLevel, LocalDate startDate, LocalDate endDate) {

        // 1. Validation: Campaign name cannot be empty
        if (campaignName == null || campaignName.trim().isEmpty()) {
            throw new IllegalArgumentException("Campaign name cannot be empty.");
        }

        // 2. Validation: Dates must not be null
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are strictly required.");
        }

        // 3. Validation: End date must be AFTER the start date
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Validation Failed: The end date cannot be before the start date.");
        }

        if (endDate.isEqual(startDate)) {
            throw new IllegalArgumentException("Validation Failed: The campaign must run for at least one day.");
        }

        // 4. Default to STANDARD priority if invalid
        if (priorityLevel == null || (!priorityLevel.equalsIgnoreCase("PREMIUM") && !priorityLevel.equalsIgnoreCase("STANDARD"))) {
            priorityLevel = "STANDARD";
        }

        return adCampaignDAO.createCampaign(advertiserId, campaignName, priorityLevel, startDate, endDate);
    }
}
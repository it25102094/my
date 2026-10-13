package lk.sportix.dao;

import lk.sportix.util.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * AdCampaignDAO — Handles database operations for Member 5's Advertisement Campaigns.
 */
public class AdCampaignDAO {

    /**
     * Inserts a new advertisement campaign into the database.
     * The status defaults to 'SCHEDULED' automatically.
     */
    public boolean createCampaign(int advertiserId, String campaignName, String priorityLevel, LocalDate startDate, LocalDate endDate) {
        String sql = "INSERT INTO ad_campaigns (advertiser_id, campaign_name, priority_level, start_date, end_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, advertiserId);
            stmt.setString(2, campaignName);
            stmt.setString(3, priorityLevel.toUpperCase());
            stmt.setDate(4, Date.valueOf(startDate));
            stmt.setDate(5, Date.valueOf(endDate));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.createCampaign error: " + e.getMessage());
            return false;
        }
    }
}
package lk.sportix.dao;

import lk.sportix.model.Campaign;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdCampaignDAO — CRUD for Member 5's advertising campaigns (IT25102094).
 *
 * FIX: the old version inserted into a table called "ad_campaigns" with a "priority_level"
 * text column. Neither exists — the schema table is "campaigns" with an INT "priority" — so
 * EVERY campaign insert failed with "Error: Database rejected the campaign".
 *
 * Ownership rule: an ADVERTISER may only change/delete their own campaigns; an ADMIN may
 * change any campaign (isAdmin = true).
 */
public class AdCampaignDAO {

    private static final String SELECT_WITH_STATS =
            "SELECT c.campaign_id, c.advertiser_id, c.campaign_name, c.budget, c.priority, c.start_date, c.end_date, " +
            "       c.status, c.created_at, u.first_name, u.last_name, u.company_name, " +
            "       (SELECT COUNT(*) FROM advertisements a WHERE a.campaign_id = c.campaign_id) AS ad_count, " +
            "       (SELECT COALESCE(SUM(a.impressions), 0) FROM advertisements a WHERE a.campaign_id = c.campaign_id) AS total_impressions, " +
            "       (SELECT COALESCE(SUM(a.clicks), 0) FROM advertisements a WHERE a.campaign_id = c.campaign_id) AS total_clicks " +
            "FROM campaigns c JOIN users u ON c.advertiser_id = u.user_id ";

    // CREATE -------------------------------------------------------------------------------
    /** Inserts a campaign and returns the generated campaign_id (or -1 on failure). */
    public int createCampaign(Campaign campaign) {
        String sql = "INSERT INTO campaigns (advertiser_id, campaign_name, budget, priority, start_date, end_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, campaign.getAdvertiserId());
            stmt.setString(2, campaign.getCampaignName());
            stmt.setBigDecimal(3, campaign.getBudget());
            stmt.setInt(4, campaign.getPriority());
            stmt.setDate(5, campaign.getStartDate());
            stmt.setDate(6, campaign.getEndDate());
            stmt.setString(7, campaign.getStatus());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        campaign.setCampaignId(keys.getInt(1));
                        return campaign.getCampaignId();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.createCampaign error: " + e.getMessage());
        }
        return -1;
    }

    // READ ---------------------------------------------------------------------------------
    public List<Campaign> getCampaignsByAdvertiser(int advertiserId) {
        return query(SELECT_WITH_STATS + "WHERE c.advertiser_id = ? ORDER BY c.created_at DESC, c.campaign_id DESC", advertiserId);
    }

    /** Admin overview: every campaign of every advertiser. */
    public List<Campaign> getAllCampaigns() {
        return query(SELECT_WITH_STATS + "ORDER BY c.created_at DESC, c.campaign_id DESC", null);
    }

    public Campaign getCampaignById(int campaignId) {
        List<Campaign> list = query(SELECT_WITH_STATS + "WHERE c.campaign_id = ?", campaignId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** True when the campaign exists and belongs to the advertiser. */
    public boolean isOwner(int campaignId, int advertiserId) {
        String sql = "SELECT 1 FROM campaigns WHERE campaign_id = ? AND advertiser_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, campaignId);
            stmt.setInt(2, advertiserId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.isOwner error: " + e.getMessage());
            return false;
        }
    }

    // UPDATE -------------------------------------------------------------------------------
    public boolean updateCampaign(Campaign campaign, int requesterId, boolean isAdmin) {
        String sql = "UPDATE campaigns SET campaign_name = ?, budget = ?, priority = ?, start_date = ?, end_date = ?, status = ? " +
                     "WHERE campaign_id = ?" + (isAdmin ? "" : " AND advertiser_id = ?");
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, campaign.getCampaignName());
            stmt.setBigDecimal(2, campaign.getBudget());
            stmt.setInt(3, campaign.getPriority());
            stmt.setDate(4, campaign.getStartDate());
            stmt.setDate(5, campaign.getEndDate());
            stmt.setString(6, campaign.getStatus());
            stmt.setInt(7, campaign.getCampaignId());
            if (!isAdmin) stmt.setInt(8, requesterId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.updateCampaign error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int campaignId, String status, int requesterId, boolean isAdmin) {
        String sql = "UPDATE campaigns SET status = ? WHERE campaign_id = ?" + (isAdmin ? "" : " AND advertiser_id = ?");
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, campaignId);
            if (!isAdmin) stmt.setInt(3, requesterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.updateStatus error: " + e.getMessage());
            return false;
        }
    }

    // DELETE -------------------------------------------------------------------------------
    /** Deletes the campaign; its advertisements are removed by ON DELETE CASCADE. */
    public boolean deleteCampaign(int campaignId, int requesterId, boolean isAdmin) {
        String sql = "DELETE FROM campaigns WHERE campaign_id = ?" + (isAdmin ? "" : " AND advertiser_id = ?");
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, campaignId);
            if (!isAdmin) stmt.setInt(2, requesterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.deleteCampaign error: " + e.getMessage());
            return false;
        }
    }

    // HELPERS ------------------------------------------------------------------------------
    private List<Campaign> query(String sql, Integer param) {
        List<Campaign> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AdCampaignDAO.query error: " + e.getMessage());
        }
        return list;
    }

    private Campaign map(ResultSet rs) throws SQLException {
        Campaign c = new Campaign();
        c.setCampaignId(rs.getInt("campaign_id"));
        c.setAdvertiserId(rs.getInt("advertiser_id"));
        c.setCampaignName(rs.getString("campaign_name"));
        c.setBudget(rs.getBigDecimal("budget"));
        c.setPriority(rs.getInt("priority"));
        c.setStartDate(rs.getDate("start_date"));
        c.setEndDate(rs.getDate("end_date"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setAdvertiserName((rs.getString("first_name") + " " + rs.getString("last_name")).trim());
        c.setCompanyName(rs.getString("company_name"));
        c.setAdCount(rs.getInt("ad_count"));
        c.setTotalImpressions(rs.getInt("total_impressions"));
        c.setTotalClicks(rs.getInt("total_clicks"));
        return c;
    }
}

package lk.sportix.dao;

import lk.sportix.model.Advertisement;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AdvertisementDAO — CRUD + delivery tracking for Member 5's advertisements (IT25102094).
 *
 * FIX: the original project had NO data access code for the 'advertisements' table at all,
 * so ads could never be created, listed, edited, deleted or shown to viewers.
 *
 * Ownership: an ad belongs to the advertiser who owns its campaign. Updates/deletes from an
 * ADVERTISER are restricted with "campaign_id IN (SELECT ... WHERE advertiser_id = ?)".
 */
public class AdvertisementDAO {

    private static final String SELECT_JOINED =
            "SELECT a.ad_id, a.campaign_id, a.ad_title, a.ad_type, a.ad_content, a.media_url, a.target_url, " +
            "       a.impressions, a.clicks, a.status, a.created_at, " +
            "       c.campaign_name, c.priority, c.status AS campaign_status, c.start_date, c.end_date, " +
            "       c.advertiser_id, u.company_name " +
            "FROM advertisements a " +
            "JOIN campaigns c ON a.campaign_id = c.campaign_id " +
            "JOIN users u ON c.advertiser_id = u.user_id ";

    private static final String OWNED_BY = " AND campaign_id IN (SELECT campaign_id FROM campaigns WHERE advertiser_id = ?)";

    // CREATE -------------------------------------------------------------------------------
    public boolean createAd(Advertisement ad) {
        String sql = "INSERT INTO advertisements (campaign_id, ad_title, ad_type, ad_content, media_url, target_url, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ad.getCampaignId());
            stmt.setString(2, ad.getAdTitle());
            stmt.setString(3, ad.getAdType());
            stmt.setString(4, ad.getAdContent());
            stmt.setString(5, ad.getMediaUrl());
            stmt.setString(6, ad.getTargetUrl());
            stmt.setString(7, ad.getStatus());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) ad.setAdId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.createAd error: " + e.getMessage());
        }
        return false;
    }

    // READ ---------------------------------------------------------------------------------
    public List<Advertisement> getAdsByAdvertiser(int advertiserId) {
        return query(SELECT_JOINED + "WHERE c.advertiser_id = ? ORDER BY a.created_at DESC, a.ad_id DESC", advertiserId);
    }

    public List<Advertisement> getAllAds() {
        return query(SELECT_JOINED + "ORDER BY a.created_at DESC, a.ad_id DESC", null);
    }

    public Advertisement getAdById(int adId) {
        List<Advertisement> list = query(SELECT_JOINED + "WHERE a.ad_id = ?", adId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Ads that may be shown to viewers RIGHT NOW: the ad is ACTIVE, its campaign is ACTIVE,
     * today is inside the campaign dates and the advertiser account is not suspended.
     * (This is the query that was missing — the reason "created ads were never displayed".)
     */
    public List<Advertisement> getRunningAds(String adType) {
        String sql = SELECT_JOINED +
                "WHERE a.status = 'ACTIVE' AND c.status = 'ACTIVE' AND u.status = 'ACTIVE' " +
                "AND CURRENT_DATE BETWEEN c.start_date AND c.end_date " +
                (adType != null ? "AND a.ad_type = ? " : "") +
                "ORDER BY c.priority ASC, a.ad_id ASC";
        List<Advertisement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (adType != null) stmt.setString(1, adType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.getRunningAds error: " + e.getMessage());
        }
        return list;
    }

    // UPDATE -------------------------------------------------------------------------------
    public boolean updateAd(Advertisement ad, int requesterId, boolean isAdmin) {
        String sql = "UPDATE advertisements SET campaign_id = ?, ad_title = ?, ad_type = ?, ad_content = ?, media_url = ?, " +
                     "target_url = ?, status = ? WHERE ad_id = ?" + (isAdmin ? "" : OWNED_BY);
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ad.getCampaignId());
            stmt.setString(2, ad.getAdTitle());
            stmt.setString(3, ad.getAdType());
            stmt.setString(4, ad.getAdContent());
            stmt.setString(5, ad.getMediaUrl());
            stmt.setString(6, ad.getTargetUrl());
            stmt.setString(7, ad.getStatus());
            stmt.setInt(8, ad.getAdId());
            if (!isAdmin) stmt.setInt(9, requesterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.updateAd error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int adId, String status, int requesterId, boolean isAdmin) {
        String sql = "UPDATE advertisements SET status = ? WHERE ad_id = ?" + (isAdmin ? "" : OWNED_BY);
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, adId);
            if (!isAdmin) stmt.setInt(3, requesterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.updateStatus error: " + e.getMessage());
            return false;
        }
    }

    /** Counts one impression for every ad that was just rendered on a page. */
    public void recordImpressions(List<Advertisement> shownAds) {
        if (shownAds == null || shownAds.isEmpty()) return;
        String sql = "UPDATE advertisements SET impressions = impressions + 1 WHERE ad_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Advertisement ad : shownAds) {
                stmt.setInt(1, ad.getAdId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.recordImpressions error: " + e.getMessage());
        }
    }

    /** Counts a click and returns the ad's target URL (null when the ad does not exist). */
    public String recordClick(int adId) {
        String find = "SELECT target_url FROM advertisements WHERE ad_id = ?";
        String update = "UPDATE advertisements SET clicks = clicks + 1 WHERE ad_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement select = conn.prepareStatement(find)) {
            select.setInt(1, adId);
            try (ResultSet rs = select.executeQuery()) {
                if (!rs.next()) return null;
                String target = rs.getString("target_url");
                try (PreparedStatement upd = conn.prepareStatement(update)) {
                    upd.setInt(1, adId);
                    upd.executeUpdate();
                }
                return target;
            }
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.recordClick error: " + e.getMessage());
            return null;
        }
    }

    // DELETE -------------------------------------------------------------------------------
    public boolean deleteAd(int adId, int requesterId, boolean isAdmin) {
        String sql = "DELETE FROM advertisements WHERE ad_id = ?" + (isAdmin ? "" : OWNED_BY);
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adId);
            if (!isAdmin) stmt.setInt(2, requesterId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.deleteAd error: " + e.getMessage());
            return false;
        }
    }

    // HELPERS ------------------------------------------------------------------------------
    private List<Advertisement> query(String sql, Integer param) {
        List<Advertisement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param != null) stmt.setInt(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("AdvertisementDAO.query error: " + e.getMessage());
        }
        return list;
    }

    private Advertisement map(ResultSet rs) throws SQLException {
        Advertisement ad = new Advertisement();
        ad.setAdId(rs.getInt("ad_id"));
        ad.setCampaignId(rs.getInt("campaign_id"));
        ad.setAdTitle(rs.getString("ad_title"));
        ad.setAdType(rs.getString("ad_type"));
        ad.setAdContent(rs.getString("ad_content"));
        ad.setMediaUrl(rs.getString("media_url"));
        ad.setTargetUrl(rs.getString("target_url"));
        ad.setImpressions(rs.getInt("impressions"));
        ad.setClicks(rs.getInt("clicks"));
        ad.setStatus(rs.getString("status"));
        ad.setCreatedAt(rs.getTimestamp("created_at"));
        ad.setCampaignName(rs.getString("campaign_name"));
        ad.setCampaignPriority(rs.getInt("priority"));
        ad.setCampaignStatus(rs.getString("campaign_status"));
        ad.setCampaignStart(rs.getDate("start_date"));
        ad.setCampaignEnd(rs.getDate("end_date"));
        ad.setAdvertiserId(rs.getInt("advertiser_id"));
        ad.setCompanyName(rs.getString("company_name"));
        return ad;
    }
}

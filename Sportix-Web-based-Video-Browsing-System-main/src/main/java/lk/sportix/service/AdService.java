package lk.sportix.service;

import lk.sportix.dao.AdCampaignDAO;
import lk.sportix.dao.AdvertisementDAO;
import lk.sportix.model.Advertisement;
import lk.sportix.model.Campaign;
import lk.sportix.model.Video;
import lk.sportix.strategy.AdSelectionStrategy;
import lk.sportix.strategy.PriorityAdSelectionStrategy;
import lk.sportix.strategy.RotationAdSelectionStrategy;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AdService — business logic and validation for Member 5's Advertising Management (IT25102094).
 *
 *  - Campaign CRUD   (create / read / update / pause-resume / delete)
 *  - Advertisement CRUD (create / read / update / pause-resume / delete)
 *  - Ad delivery to viewers using the STRATEGY design pattern + impression & click tracking
 *
 * All validation errors are thrown as IllegalArgumentException with a user friendly message;
 * the servlets show that message in the dashboard flash banner.
 */
public class AdService {

    private static final Set<String> AD_TYPES = Set.of(Advertisement.TYPE_BANNER, Advertisement.TYPE_SPONSORED_VIDEO, Advertisement.TYPE_TEXT);
    private static final Set<String> CAMPAIGN_STATUSES = Set.of("ACTIVE", "PAUSED", "COMPLETED");
    private static final Set<String> AD_STATUSES = Set.of("ACTIVE", "PAUSED");
    private static final BigDecimal MAX_BUDGET = new BigDecimal("99999999.99"); // DECIMAL(10,2)

    /** Ad slots used by the JSP pages. */
    public static final String PLACEMENT_HOME_BANNER = "HOME_BANNER";
    public static final String PLACEMENT_VIDEO_SIDEBAR = "VIDEO_SIDEBAR";
    public static final String PLACEMENT_SEARCH = "SEARCH";

    private final AdCampaignDAO campaignDAO = new AdCampaignDAO();
    private final AdvertisementDAO adDAO = new AdvertisementDAO();

    // STRATEGY pattern: one concrete strategy per placement
    private final AdSelectionStrategy priorityStrategy = new PriorityAdSelectionStrategy();
    private final AdSelectionStrategy rotationStrategy = new RotationAdSelectionStrategy();

    // =====================================================================================
    // CAMPAIGNS
    // =====================================================================================

    /** CREATE. Returns the new campaign id. */
    public int createCampaign(int advertiserId, String name, String budget, String priority,
                              String startDate, String endDate, String status) {
        Campaign c = buildCampaign(name, budget, priority, startDate, endDate, status);
        if (c.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Error: The start date cannot be in the past.");
        }
        c.setAdvertiserId(advertiserId);
        int id = campaignDAO.createCampaign(c);
        if (id <= 0) {
            throw new IllegalStateException("Error: The database rejected the campaign. Please try again.");
        }
        return id;
    }

    /** READ: campaigns of one advertiser (with ad counts, impressions and clicks). */
    public List<Campaign> getCampaignsForAdvertiser(int advertiserId) {
        return campaignDAO.getCampaignsByAdvertiser(advertiserId);
    }

    /** READ: all campaigns (admin oversight). */
    public List<Campaign> getAllCampaigns() {
        return campaignDAO.getAllCampaigns();
    }

    /** READ single campaign, or null if it does not exist / is not owned by the requester. */
    public Campaign getCampaign(int campaignId, int requesterId, boolean isAdmin) {
        Campaign c = campaignDAO.getCampaignById(campaignId);
        if (c == null || (!isAdmin && c.getAdvertiserId() != requesterId)) return null;
        return c;
    }

    /** UPDATE every editable field. */
    public void updateCampaign(int campaignId, int requesterId, boolean isAdmin, String name, String budget,
                               String priority, String startDate, String endDate, String status) {
        if (getCampaign(campaignId, requesterId, isAdmin) == null) {
            throw new IllegalArgumentException("Error: Campaign not found or access denied.");
        }
        Campaign c = buildCampaign(name, budget, priority, startDate, endDate, status);
        c.setCampaignId(campaignId);
        if (!campaignDAO.updateCampaign(c, requesterId, isAdmin)) {
            throw new IllegalStateException("Error: The campaign could not be updated.");
        }
    }

    /** UPDATE: pause / resume / complete. */
    public boolean setCampaignStatus(int campaignId, String status, int requesterId, boolean isAdmin) {
        String s = normaliseUpper(status);
        if (!CAMPAIGN_STATUSES.contains(s)) {
            throw new IllegalArgumentException("Error: Invalid campaign status.");
        }
        return campaignDAO.updateStatus(campaignId, s, requesterId, isAdmin);
    }

    /** DELETE (the campaign's ads are deleted automatically by ON DELETE CASCADE). */
    public boolean deleteCampaign(int campaignId, int requesterId, boolean isAdmin) {
        return campaignDAO.deleteCampaign(campaignId, requesterId, isAdmin);
    }

    private Campaign buildCampaign(String name, String budget, String priority, String startDate, String endDate, String status) {
        String cleanName = trim(name);
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Error: Campaign name cannot be empty.");
        }
        if (cleanName.length() > 120) {
            throw new IllegalArgumentException("Error: Campaign name must be 120 characters or less.");
        }

        BigDecimal amount;
        try {
            amount = trim(budget).isEmpty() ? BigDecimal.ZERO : new BigDecimal(trim(budget));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error: Budget must be a number (e.g. 25000.00).");
        }
        if (amount.signum() < 0 || amount.compareTo(MAX_BUDGET) > 0) {
            throw new IllegalArgumentException("Error: Budget must be between 0 and 99,999,999.99.");
        }

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(trim(startDate));
            end = LocalDate.parse(trim(endDate));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Error: Invalid date format. Please select valid calendar dates.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Error: The end date cannot be before the start date.");
        }

        String cleanStatus = trim(status).isEmpty() ? "ACTIVE" : normaliseUpper(status);
        if (!CAMPAIGN_STATUSES.contains(cleanStatus)) {
            throw new IllegalArgumentException("Error: Invalid campaign status.");
        }

        Campaign c = new Campaign();
        c.setCampaignName(cleanName);
        c.setBudget(amount.setScale(2, java.math.RoundingMode.HALF_UP));
        c.setPriority(parsePriority(priority));
        c.setStartDate(Date.valueOf(start));
        c.setEndDate(Date.valueOf(end));
        c.setStatus(cleanStatus);
        return c;
    }

    /** Accepts 1/2/3, HIGH/NORMAL/LOW and the old PREMIUM/STANDARD values from the first UI. */
    private int parsePriority(String priority) {
        String p = normaliseUpper(priority);
        switch (p) {
            case "1": case "HIGH": case "PREMIUM": return Campaign.PRIORITY_HIGH;
            case "3": case "LOW": return Campaign.PRIORITY_LOW;
            case "": case "2": case "NORMAL": case "STANDARD": return Campaign.PRIORITY_NORMAL;
            default: throw new IllegalArgumentException("Error: Priority must be High, Normal or Low.");
        }
    }

    // =====================================================================================
    // ADVERTISEMENTS
    // =====================================================================================

    /** CREATE an ad inside one of the requester's campaigns. */
    public Advertisement createAd(int requesterId, boolean isAdmin, String campaignId, String title, String adType,
                                  String content, String mediaUrl, String targetUrl, String status) {
        Advertisement ad = buildAd(requesterId, isAdmin, campaignId, title, adType, content, mediaUrl, targetUrl, status);
        if (!adDAO.createAd(ad)) {
            throw new IllegalStateException("Error: The database rejected the advertisement.");
        }
        return ad;
    }

    public List<Advertisement> getAdsForAdvertiser(int advertiserId) {
        return adDAO.getAdsByAdvertiser(advertiserId);
    }

    public List<Advertisement> getAllAds() {
        return adDAO.getAllAds();
    }

    public Advertisement getAd(int adId, int requesterId, boolean isAdmin) {
        Advertisement ad = adDAO.getAdById(adId);
        if (ad == null || (!isAdmin && ad.getAdvertiserId() != requesterId)) return null;
        return ad;
    }

    /** UPDATE every editable field of an ad (it can also be moved to another own campaign). */
    public void updateAd(int adId, int requesterId, boolean isAdmin, String campaignId, String title, String adType,
                         String content, String mediaUrl, String targetUrl, String status) {
        if (getAd(adId, requesterId, isAdmin) == null) {
            throw new IllegalArgumentException("Error: Advertisement not found or access denied.");
        }
        Advertisement ad = buildAd(requesterId, isAdmin, campaignId, title, adType, content, mediaUrl, targetUrl, status);
        ad.setAdId(adId);
        if (!adDAO.updateAd(ad, requesterId, isAdmin)) {
            throw new IllegalStateException("Error: The advertisement could not be updated.");
        }
    }

    public boolean setAdStatus(int adId, String status, int requesterId, boolean isAdmin) {
        String s = normaliseUpper(status);
        if (!AD_STATUSES.contains(s)) {
            throw new IllegalArgumentException("Error: Invalid advertisement status.");
        }
        return adDAO.updateStatus(adId, s, requesterId, isAdmin);
    }

    public boolean deleteAd(int adId, int requesterId, boolean isAdmin) {
        return adDAO.deleteAd(adId, requesterId, isAdmin);
    }

    private Advertisement buildAd(int requesterId, boolean isAdmin, String campaignIdStr, String title, String adType,
                                  String content, String mediaUrl, String targetUrl, String status) {
        int campaignId;
        try {
            campaignId = Integer.parseInt(trim(campaignIdStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error: Please choose a campaign for this advertisement.");
        }
        if (isAdmin ? campaignDAO.getCampaignById(campaignId) == null : !campaignDAO.isOwner(campaignId, requesterId)) {
            throw new IllegalArgumentException("Error: The selected campaign does not exist or is not yours.");
        }

        String cleanTitle = trim(title);
        if (cleanTitle.isEmpty() || cleanTitle.length() > 150) {
            throw new IllegalArgumentException("Error: Ad title is required (maximum 150 characters).");
        }

        String type = normaliseUpper(adType);
        if (!AD_TYPES.contains(type)) {
            throw new IllegalArgumentException("Error: Ad type must be BANNER, SPONSORED_VIDEO or TEXT.");
        }

        String cleanContent = trim(content);
        if (cleanContent.length() > 500) {
            throw new IllegalArgumentException("Error: Ad text must be 500 characters or less.");
        }
        if (Advertisement.TYPE_TEXT.equals(type) && cleanContent.isEmpty()) {
            throw new IllegalArgumentException("Error: A TEXT ad needs some ad text.");
        }

        String media = trim(mediaUrl);
        if (Advertisement.TYPE_BANNER.equals(type) && !isHttpUrl(media)) {
            throw new IllegalArgumentException("Error: A BANNER ad needs an image URL starting with http:// or https://");
        }
        if (Advertisement.TYPE_SPONSORED_VIDEO.equals(type) && Video.extractVideoId(media).isEmpty()) {
            throw new IllegalArgumentException("Error: A SPONSORED_VIDEO ad needs a valid YouTube link.");
        }
        if (!media.isEmpty() && (!isHttpUrl(media) || media.length() > 500)) {
            throw new IllegalArgumentException("Error: Media URL must start with http:// or https:// (max 500 characters).");
        }

        String target = trim(targetUrl);
        if (!isHttpUrl(target) || target.length() > 500) {
            throw new IllegalArgumentException("Error: Target URL must start with http:// or https:// (max 500 characters).");
        }

        String cleanStatus = trim(status).isEmpty() ? "ACTIVE" : normaliseUpper(status);
        if (!AD_STATUSES.contains(cleanStatus)) {
            throw new IllegalArgumentException("Error: Invalid advertisement status.");
        }

        Advertisement ad = new Advertisement();
        ad.setCampaignId(campaignId);
        ad.setAdTitle(cleanTitle);
        ad.setAdType(type);
        ad.setAdContent(cleanContent.isEmpty() ? null : cleanContent);
        ad.setMediaUrl(media.isEmpty() ? null : media);
        ad.setTargetUrl(target);
        ad.setStatus(cleanStatus);
        return ad;
    }

    // =====================================================================================
    // AD DELIVERY (what viewers see) — STRATEGY pattern
    // =====================================================================================

    /**
     * Returns the ads to render in a page slot and counts an impression for each of them.
     *   HOME_BANNER   -> BANNER ads, PriorityAdSelectionStrategy (high priority campaigns first)
     *   VIDEO_SIDEBAR -> SPONSORED_VIDEO + TEXT ads, RotationAdSelectionStrategy (fair rotation)
     *   SEARCH        -> TEXT ads, RotationAdSelectionStrategy
     */
    public List<Advertisement> getAdsForPlacement(String placement, int limit) {
        List<Advertisement> candidates = new ArrayList<>();
        AdSelectionStrategy strategy;
        switch (placement) {
            case PLACEMENT_HOME_BANNER:
                candidates.addAll(adDAO.getRunningAds(Advertisement.TYPE_BANNER));
                strategy = priorityStrategy;
                break;
            case PLACEMENT_VIDEO_SIDEBAR:
                candidates.addAll(adDAO.getRunningAds(Advertisement.TYPE_SPONSORED_VIDEO));
                candidates.addAll(adDAO.getRunningAds(Advertisement.TYPE_TEXT));
                strategy = rotationStrategy;
                break;
            case PLACEMENT_SEARCH:
            default:
                candidates.addAll(adDAO.getRunningAds(Advertisement.TYPE_TEXT));
                strategy = rotationStrategy;
                break;
        }
        List<Advertisement> chosen = strategy.select(candidates, limit);
        adDAO.recordImpressions(chosen);
        return chosen;
    }

    /** Counts a click and returns the safe (http/https) target URL, or null. */
    public String registerClick(int adId) {
        if (adId <= 0) return null;
        String target = adDAO.recordClick(adId);
        return isHttpUrl(target) ? target : null;
    }

    // =====================================================================================
    // DASHBOARD STATISTICS
    // =====================================================================================

    public Map<String, Object> buildStats(List<Campaign> campaigns, List<Advertisement> ads) {
        int running = 0;
        int impressions = 0;
        int clicks = 0;
        BigDecimal budget = BigDecimal.ZERO;
        for (Campaign c : campaigns) {
            if ("RUNNING".equals(c.getRunState())) running++;
            budget = budget.add(c.getBudget());
        }
        int activeAds = 0;
        for (Advertisement a : ads) {
            impressions += a.getImpressions();
            clicks += a.getClicks();
            if ("ACTIVE".equals(a.getStatus())) activeAds++;
        }
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("campaignCount", campaigns.size());
        stats.put("runningCampaigns", running);
        stats.put("adCount", ads.size());
        stats.put("activeAds", activeAds);
        stats.put("impressions", impressions);
        stats.put("clicks", clicks);
        stats.put("ctr", impressions == 0 ? 0.0 : (clicks * 100.0) / impressions);
        stats.put("totalBudget", budget);
        return stats;
    }

    // =====================================================================================
    // HELPERS
    // =====================================================================================

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String normaliseUpper(String s) {
        return trim(s).toUpperCase();
    }

    static boolean isHttpUrl(String url) {
        if (url == null) return false;
        String u = url.trim().toLowerCase();
        return (u.startsWith("http://") || u.startsWith("https://")) && u.length() > 10 && !u.contains(" ");
    }
}

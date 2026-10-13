package lk.sportix.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Advertisement Model — POJO for Member 5's Advertising Management (IT25102094).
 * Maps to the 'advertisements' table (every column) plus a few campaign fields
 * that are JOINed in for display and for the ad-selection strategies.
 */
public class Advertisement {

    public static final String TYPE_BANNER = "BANNER";
    public static final String TYPE_SPONSORED_VIDEO = "SPONSORED_VIDEO";
    public static final String TYPE_TEXT = "TEXT";

    private int adId;
    private int campaignId;
    private String adTitle;
    private String adType;     // BANNER, SPONSORED_VIDEO, TEXT (matches chk_ad_type)
    private String adContent;
    private String mediaUrl;   // image URL (BANNER) or YouTube URL (SPONSORED_VIDEO)
    private String targetUrl;
    private int impressions;
    private int clicks;
    private String status = "ACTIVE";   // ACTIVE, PAUSED
    private Timestamp createdAt;

    // JOINed campaign data (read-only)
    private String campaignName;
    private int campaignPriority = Campaign.PRIORITY_NORMAL;
    private String campaignStatus;
    private Date campaignStart;
    private Date campaignEnd;
    private int advertiserId;
    private String companyName;

    public Advertisement() {}

    public int getAdId() { return adId; }
    public void setAdId(int adId) { this.adId = adId; }

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public String getAdTitle() { return adTitle; }
    public void setAdTitle(String adTitle) { this.adTitle = adTitle; }

    public String getAdType() { return adType; }
    public void setAdType(String adType) { this.adType = adType; }

    public String getAdContent() { return adContent; }
    public void setAdContent(String adContent) { this.adContent = adContent; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }

    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public int getCampaignPriority() { return campaignPriority; }
    public void setCampaignPriority(int campaignPriority) { this.campaignPriority = campaignPriority; }

    public String getCampaignStatus() { return campaignStatus; }
    public void setCampaignStatus(String campaignStatus) { this.campaignStatus = campaignStatus; }

    public Date getCampaignStart() { return campaignStart; }
    public void setCampaignStart(Date campaignStart) { this.campaignStart = campaignStart; }

    public Date getCampaignEnd() { return campaignEnd; }
    public void setCampaignEnd(Date campaignEnd) { this.campaignEnd = campaignEnd; }

    public int getAdvertiserId() { return advertiserId; }
    public void setAdvertiserId(int advertiserId) { this.advertiserId = advertiserId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    /**
     * Dynamically calculates the Click-Through Rate (CTR) percentage.
     */
    public double getClickThroughRate() {
        if (impressions == 0) return 0.0;
        return ((double) clicks / impressions) * 100.0;
    }

    /** Thumbnail for SPONSORED_VIDEO ads (derived from the YouTube link in media_url). */
    public String getVideoThumbnailUrl() {
        String id = Video.extractVideoId(mediaUrl);
        return id.isEmpty() ? "" : "https://img.youtube.com/vi/" + id + "/hqdefault.jpg";
    }

    /**
     * True when viewers can see this ad right now: ad ACTIVE + campaign ACTIVE + today inside the
     * campaign dates. Shown as the "LIVE on site" badge in the advertiser dashboard.
     */
    public boolean isRunningNow() {
        if (!"ACTIVE".equals(status) || !"ACTIVE".equals(campaignStatus) || campaignStart == null || campaignEnd == null) {
            return false;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        return !today.isBefore(campaignStart.toLocalDate()) && !today.isAfter(campaignEnd.toLocalDate());
    }

    public boolean isBanner() { return TYPE_BANNER.equals(adType); }
    public boolean isSponsoredVideo() { return TYPE_SPONSORED_VIDEO.equals(adType); }
    public boolean isText() { return TYPE_TEXT.equals(adType); }
}

package lk.sportix.model;

/**
 * Advertisement Model — POJO for Member 5's Advertising Management.
 */
public class Advertisement {
    private int adId;
    private int campaignId;
    private String adType; // BANNER, SPONSORED_VIDEO, AD_CARD
    private String targetUrl;
    private String mediaUrl;
    private int impressions;
    private int clicks;

    public Advertisement() {}

    public int getAdId() { return adId; }
    public void setAdId(int adId) { this.adId = adId; }

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public String getAdType() { return adType; }
    public void setAdType(String adType) { this.adType = adType; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public int getImpressions() { return impressions; }
    public void setImpressions(int impressions) { this.impressions = impressions; }

    public int getClicks() { return clicks; }
    public void setClicks(int clicks) { this.clicks = clicks; }

    /**
     * Dynamically calculates the Click-Through Rate (CTR) percentage.
     */
    public double getClickThroughRate() {
        if (impressions == 0) return 0.0;
        return ((double) clicks / impressions) * 100.0;
    }
}
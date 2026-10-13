package lk.sportix.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Campaign Model — POJO for Member 5 (IT25102094): Advertising & Notification Management.
 * Maps directly to the 'campaigns' table. A campaign owns many {@link Advertisement}s and
 * decides when (start/end date), how important (priority) and whether (status) they run.
 */
public class Campaign {

    /** Priority values stored in the database (1 = highest visibility). */
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_LOW = 3;

    private int campaignId;
    private int advertiserId;
    private String campaignName;
    private BigDecimal budget = BigDecimal.ZERO;
    private int priority = PRIORITY_NORMAL;
    private Date startDate;
    private Date endDate;
    private String status = "ACTIVE";      // ACTIVE, PAUSED, COMPLETED
    private Timestamp createdAt;

    // Read-only values filled by aggregate SQL queries (for the dashboard tables)
    private String advertiserName;
    private String companyName;
    private int adCount;
    private int totalImpressions;
    private int totalClicks;

    public Campaign() {}

    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }

    public int getAdvertiserId() { return advertiserId; }
    public void setAdvertiserId(int advertiserId) { this.advertiserId = advertiserId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = (budget != null) ? budget : BigDecimal.ZERO; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getAdvertiserName() { return advertiserName; }
    public void setAdvertiserName(String advertiserName) { this.advertiserName = advertiserName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getAdCount() { return adCount; }
    public void setAdCount(int adCount) { this.adCount = adCount; }

    public int getTotalImpressions() { return totalImpressions; }
    public void setTotalImpressions(int totalImpressions) { this.totalImpressions = totalImpressions; }

    public int getTotalClicks() { return totalClicks; }
    public void setTotalClicks(int totalClicks) { this.totalClicks = totalClicks; }

    /** Human readable priority label for the UI. */
    public String getPriorityLabel() {
        switch (priority) {
            case PRIORITY_HIGH: return "High";
            case PRIORITY_LOW: return "Low";
            default: return "Normal";
        }
    }

    /** Click-through rate across all ads of this campaign, as a percentage. */
    public double getClickThroughRate() {
        return totalImpressions == 0 ? 0.0 : (totalClicks * 100.0) / totalImpressions;
    }

    /**
     * Derived running state shown in the dashboard:
     * PAUSED / COMPLETED come from the status column, otherwise the dates decide
     * whether the campaign is SCHEDULED (future), RUNNING (today) or ENDED (past).
     */
    public String getRunState() {
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            return status;
        }
        LocalDate today = LocalDate.now();
        if (startDate != null && today.isBefore(startDate.toLocalDate())) return "SCHEDULED";
        if (endDate != null && today.isAfter(endDate.toLocalDate())) return "ENDED";
        return "RUNNING";
    }
}

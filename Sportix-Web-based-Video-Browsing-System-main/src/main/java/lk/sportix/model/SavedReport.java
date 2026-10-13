package lk.sportix.model;

import java.sql.Timestamp;

/**
 * SavedReport Model — Member 6 (Reporting, Analytics & Admin).
 * Maps to the 'saved_reports' table: an admin can save a snapshot of the system analytics,
 * read it later, rename it and delete it (full CRUD).
 */
public class SavedReport {
    private int savedReportId;
    private int adminId;
    private String adminName;
    private String reportTitle;
    private String reportType;
    private String summaryContent;
    private Timestamp createdAt;

    public SavedReport() {}

    public int getSavedReportId() { return savedReportId; }
    public void setSavedReportId(int savedReportId) { this.savedReportId = savedReportId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getSummaryContent() { return summaryContent; }
    public void setSummaryContent(String summaryContent) { this.summaryContent = summaryContent; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

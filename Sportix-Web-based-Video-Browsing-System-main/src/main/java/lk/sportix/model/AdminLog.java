package lk.sportix.model;

import java.sql.Timestamp;

public class AdminLog {
    private int logId;
    private int adminId;
    private String actionType;
    private String targetTable;
    private int targetId;
    private String description;
    private Timestamp createdAt;

    public AdminLog(int adminId, String actionType, String targetTable, int targetId, String description) {
        this.adminId = adminId;
        this.actionType = actionType;
        this.targetTable = targetTable;
        this.targetId = targetId;
        this.description = description;
    }

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }
    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
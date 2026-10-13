package lk.sportix.model;

import java.sql.Timestamp;

public class SavedSearch {
    private int savedSearchId;
    private int userId;
    private String searchLabel;
    private String searchQuery;
    private String sortPreference;
    private Timestamp createdAt;

    public SavedSearch() {}

    public int getSavedSearchId() { return savedSearchId; }
    public void setSavedSearchId(int savedSearchId) { this.savedSearchId = savedSearchId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSearchLabel() { return searchLabel; }
    public void setSearchLabel(String searchLabel) { this.searchLabel = searchLabel; }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public String getSortPreference() { return sortPreference; }
    public void setSortPreference(String sortPreference) { this.sortPreference = sortPreference; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
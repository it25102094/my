package lk.sportix.dao;

import lk.sportix.model.SavedSearch;
import lk.sportix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchDAO — Member 2's complete CRUD operations on saved_searches and search_history.
 * Corrected to match the exact column names in sportix_schema.sql (search_keyword, sort_strategy).
 */
public class SearchDAO {

    // 1. CREATE: Save a user's favourite search filter
    public boolean addSavedSearch(int userId, String label, String query, String sortPref) {
        String sql = "INSERT INTO saved_searches (user_id, search_label, search_keyword, sort_strategy) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, label);
            stmt.setString(3, query);
            stmt.setString(4, sortPref != null ? sortPref : "VIEWS");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SearchDAO.addSavedSearch error: " + e.getMessage());
            return false;
        }
    }

    // 2. READ: Get all saved searches for a logged-in user
    public List<SavedSearch> getSavedSearchesByUser(int userId) {
        List<SavedSearch> list = new ArrayList<>();
        String sql = "SELECT * FROM saved_searches WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SavedSearch ss = new SavedSearch();
                    ss.setSavedSearchId(rs.getInt("saved_search_id"));
                    ss.setUserId(rs.getInt("user_id"));
                    ss.setSearchLabel(rs.getString("search_label"));
                    ss.setSearchQuery(rs.getString("search_keyword"));   // Matched to DB column
                    ss.setSortPreference(rs.getString("sort_strategy")); // Matched to DB column
                    ss.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(ss);
                }
            }
        } catch (SQLException e) {
            System.err.println("SearchDAO.getSavedSearchesByUser error: " + e.getMessage());
        }
        return list;
    }

    // 3. UPDATE: Rename or change sort preference of a saved search
    public boolean updateSavedSearch(int savedSearchId, int userId, String newLabel, String newSortPref) {
        String sql = "UPDATE saved_searches SET search_label = ?, sort_strategy = ? WHERE saved_search_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newLabel);
            stmt.setString(2, newSortPref);
            stmt.setInt(3, savedSearchId);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SearchDAO.updateSavedSearch error: " + e.getMessage());
            return false;
        }
    }

    // 4. DELETE: Remove a saved search
    public boolean deleteSavedSearch(int savedSearchId, int userId) {
        String sql = "DELETE FROM saved_searches WHERE saved_search_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, savedSearchId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SearchDAO.deleteSavedSearch error: " + e.getMessage());
            return false;
        }
    }
}
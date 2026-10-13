package lk.sportix.dao;

import lk.sportix.model.User;
import lk.sportix.util.DBConnection;

import java.sql.*;

/**
 * Simplified DAO for 'users' table CRUD operations.
 * Owned by Member 1 (IT25102095).
 */
public class UserDAO {

    // CREATE: Register a new user
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, first_name, last_name, email, contact_number, " +
                "password_hash, role, status, favourite_sports, spoiler_free, company_name) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getContactNumber());
            ps.setString(6, user.getPasswordHash());
            ps.setString(7, user.getRole() != null ? user.getRole() : "USER");
            ps.setString(8, user.getFavouriteSports());
            ps.setBoolean(9, user.isHideScores());
            ps.setString(10, user.getCompanyName());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.createUser error: " + e.getMessage());
            return false;
        }
    }

    // Alias so UserService.registerUser() calls work
    public boolean registerUser(User user) {
        return createUser(user);
    }

    // READ: Find user by username or email (for Login)
    public User findByEmailOrUsername(String identifier) {
        String sql = "SELECT * FROM users WHERE email = ? OR username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.findByEmailOrUsername error: " + e.getMessage());
        }
        return null;
    }

    // Alias so UserService.getUserByUsername() calls work
    public User getUserByUsername(String username) {
        return findByEmailOrUsername(username);
    }

    // READ: Find user by user_id (for Profile page)
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.findById error: " + e.getMessage());
        }
        return null;
    }

    // READ: Check if email or username already exists
    public boolean existsByEmailOrUsername(String email, String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? OR username = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.existsByEmailOrUsername error: " + e.getMessage());
        }
        return false;
    }

    // UPDATE: Update profile details
    public boolean updateProfile(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, contact_number = ?, " +
                "favourite_sports = ?, spoiler_free = ?, company_name = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getContactNumber());
            ps.setString(4, user.getFavouriteSports());
            ps.setBoolean(5, user.isHideScores());
            ps.setString(6, user.getCompanyName());
            ps.setInt(7, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.updateProfile error: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Change password by user_id
    public boolean updatePasswordByUserId(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.updatePasswordByUserId error: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Reset password via Email + Contact Number (Forgot Password)
    public boolean resetPasswordByEmailAndPhone(String email, String contactNumber, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ? AND contact_number = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setString(2, email);
            ps.setString(3, contactNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.resetPasswordByEmailAndPhone error: " + e.getMessage());
            return false;
        }
    }

    // UPDATE: Toggle Hide Scores preference
    public boolean updateHideScores(int userId, boolean hideScores) {
        String sql = "UPDATE users SET spoiler_free = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, hideScores);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.updateHideScores error: " + e.getMessage());
            return false;
        }
    }

    // Alias so existing updateSpoilerFree() calls still compile
    public boolean updateSpoilerFree(int userId, boolean hideScores) {
        return updateHideScores(userId, hideScores);
    }

    // DELETE: Permanently delete user account
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("UserDAO.deleteUser error: " + e.getMessage());
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setContactNumber(rs.getString("contact_number"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setFavouriteSports(rs.getString("favourite_sports"));
        user.setHideScores(rs.getBoolean("spoiler_free"));
        user.setCompanyName(rs.getString("company_name"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
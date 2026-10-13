package lk.sportix.service;

import lk.sportix.dao.UserDAO;
import lk.sportix.model.User;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Simplified Service layer for User Account Management.
 * Handles validation and BCrypt password hashing before calling UserDAO.
 * Owned by Member 1 (IT25102095).
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // CREATE: Validate and register a new user
    public boolean register(String username, String firstName, String lastName, String email, String plainPassword) {
        if (username == null || username.trim().isEmpty() || firstName == null || firstName.trim().isEmpty()) {
            return false;
        }
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            return false;
        }
        if (plainPassword == null || plainPassword.length() < 8) {
            return false;
        }
        if (userDAO.existsByEmailOrUsername(email.trim(), username.trim())) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        User newUser = new User(username.trim(), firstName.trim(),
                lastName != null ? lastName.trim() : "", email.trim(), hashedPassword, "USER");
        return userDAO.createUser(newUser);
    }

    // READ: Authenticate login (checks password with BCrypt and ensures account is ACTIVE)
    public User login(String usernameOrEmail, String plainPassword) {
        if (usernameOrEmail == null || plainPassword == null) {
            return null;
        }
        User user = userDAO.findByEmailOrUsername(usernameOrEmail.trim());
        if (user == null || "SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            return null;
        }
        return BCrypt.checkpw(plainPassword, user.getPasswordHash()) ? user : null;
    }

    // READ: Get user by ID
    public User getUserById(int userId) {
        return userDAO.findById(userId);
    }

    // UPDATE: Edit profile details
    public boolean updateProfile(User user) {
        if (user == null || user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            return false;
        }
        return userDAO.updateProfile(user);
    }

    // UPDATE: Change password
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            return false;
        }
        User user = userDAO.findById(userId);
        if (user == null || !BCrypt.checkpw(currentPassword, user.getPasswordHash())) {
            return false;
        }
        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        return userDAO.updatePasswordByUserId(userId, newHash);
    }

    // UPDATE: Toggle Hide Scores preference
    public boolean updateHideScores(int userId, boolean hideScores) {
        return userDAO.updateHideScores(userId, hideScores);
    }

    // Alias so ProfileServlet calls to updateSpoilerFree() still compile cleanly
    public boolean updateSpoilerFree(int userId, boolean hideScores) {
        return updateHideScores(userId, hideScores);
    }
    // UPDATE: Reset password via Forgot Password feature
    public boolean resetPassword(String email, String phone, String newPassword) {
        if (email == null || phone == null || newPassword == null || newPassword.length() < 8) return false;
        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        return userDAO.resetPasswordByEmailAndPhone(email.trim(), phone.trim(), newHash);
    }
    // DELETE: Delete user account
    public boolean deleteAccount(int userId) {
        return userDAO.deleteUser(userId);
    }
}
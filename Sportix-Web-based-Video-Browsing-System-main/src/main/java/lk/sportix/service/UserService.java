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

    /**
     * FIX: the old pattern "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$" rejected every address with a
     * sub-domain, e.g. it25102094@my.sliit.lk, so SLIIT students could not register.
     */
    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[a-zA-Z]{2,}$";
    private static final String USERNAME_REGEX = "^[A-Za-z0-9_.]{3,50}$";
    private static final String PHONE_REGEX = "^[0-9+ ]{9,15}$";

    private final UserDAO userDAO = new UserDAO();
    private final NotificationService notificationService = new NotificationService();

    /**
     * CREATE: Validate and register a new user.
     * Self-registration may create a viewer (USER) or an ADVERTISER account (Member 5);
     * CONTENT_MANAGER / ADMIN accounts can only be granted by an admin.
     *
     * @return null when the account was created, otherwise a user friendly error message
     */
    public String register(String username, String firstName, String lastName, String email, String plainPassword,
                           String contactNumber, String accountType, String companyName) {
        String u = trim(username);
        String f = trim(firstName);
        String l = trim(lastName);
        String e = trim(email).toLowerCase();
        String phone = trim(contactNumber);
        String company = trim(companyName);
        String role = "ADVERTISER".equalsIgnoreCase(trim(accountType)) ? "ADVERTISER" : "USER";

        if (!u.matches(USERNAME_REGEX)) {
            return "Username must be 3-50 characters (letters, numbers, _ or . only).";
        }
        if (f.isEmpty() || f.length() > 50 || l.isEmpty() || l.length() > 50) {
            return "First name and last name are required (maximum 50 characters each).";
        }
        if (e.length() > 100 || !e.matches(EMAIL_REGEX)) {
            return "Please enter a valid email address.";
        }
        if (!phone.isEmpty() && !phone.matches(PHONE_REGEX)) {
            return "Contact number must contain 9-15 digits (it is used to reset your password).";
        }
        if (plainPassword == null || plainPassword.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        if ("ADVERTISER".equals(role) && (company.isEmpty() || company.length() > 120)) {
            return "Advertiser accounts need a company / brand name (maximum 120 characters).";
        }
        if (userDAO.existsByEmailOrUsername(e, u)) {
            return "Username or email is already in use.";
        }

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        User newUser = new User(u, f, l, e, hashedPassword, role);
        newUser.setContactNumber(phone.isEmpty() ? null : phone);
        newUser.setCompanyName("ADVERTISER".equals(role) ? company : null);
        if (!userDAO.createUser(newUser)) {
            return "Registration failed because of a database error. Please try again.";
        }

        // Welcome notification (Member 5 - Factory pattern via NotificationService)
        User created = userDAO.findByEmailOrUsername(u);
        if (created != null) {
            notificationService.sendSystemNotification(created.getUserId(), "Welcome to Sportix, " + f + "!",
                    "ADVERTISER".equals(role)
                            ? "Open your Advertiser Dashboard to create your first campaign and ads."
                            : "Follow your favourite teams to get an alert whenever they post a new video.",
                    "ADVERTISER".equals(role) ? "/advertiser/dashboard" : "/profile");
        }
        return null;
    }

    /** Old signature kept for compatibility (viewer account, no contact number). */
    public boolean register(String username, String firstName, String lastName, String email, String plainPassword) {
        return register(username, firstName, lastName, email, plainPassword, null, "USER", null) == null;
    }

    // READ: Authenticate login (checks password with BCrypt and ensures account is ACTIVE)
    public User login(String usernameOrEmail, String plainPassword) {
        if (usernameOrEmail == null || plainPassword == null || usernameOrEmail.isBlank()) {
            return null;
        }
        User user = userDAO.findByEmailOrUsername(usernameOrEmail.trim());
        if (user == null || "SUSPENDED".equalsIgnoreCase(user.getStatus())) {
            return null;
        }
        try {
            return BCrypt.checkpw(plainPassword, user.getPasswordHash()) ? user : null;
        } catch (IllegalArgumentException badHash) {
            // a malformed hash in the database must not crash the login page
            System.err.println("UserService.login: invalid password hash for user " + user.getUsername());
            return null;
        }
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
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(trim(user.getLastName()));
        String phone = trim(user.getContactNumber());
        if (!phone.isEmpty() && !phone.matches(PHONE_REGEX)) {
            return false;
        }
        user.setContactNumber(phone.isEmpty() ? null : phone);
        if (user.getFirstName().length() > 50 || user.getLastName().length() > 50) {
            return false;
        }
        return userDAO.updateProfile(user);
    }

    // UPDATE: Change password
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || currentPassword == null) {
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
        return userDAO.resetPasswordByEmailAndPhone(email.trim().toLowerCase(), phone.trim(), newHash);
    }

    // DELETE: Delete user account
    public boolean deleteAccount(int userId) {
        return userDAO.deleteUser(userId);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}

package lk.sportix.model;

/**
 * Simplified User Model for Member 1 (IT25102095).
 * Represents a user account in the 'users' table.
 */
public class User {

    private int userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String passwordHash;
    private String role;            // USER, CONTENT_MANAGER, ADVERTISER, ADMIN
    private String status;          // ACTIVE, SUSPENDED
    private String favouriteSports;
    private boolean hideScores;     // Simple name: Hide Scores

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, String passwordHash, String role) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = "ACTIVE";
        this.hideScores = false;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFavouriteSports() { return favouriteSports; }
    public void setFavouriteSports(String favouriteSports) { this.favouriteSports = favouriteSports; }

    public boolean isHideScores() { return hideScores; }
    public void setHideScores(boolean hideScores) { this.hideScores = hideScores; }

    // Alias so existing code calling isSpoilerFree() or setSpoilerFree() still compiles without errors
    public boolean isSpoilerFree() { return hideScores; }
    public void setSpoilerFree(boolean hideScores) { this.hideScores = hideScores; }
}
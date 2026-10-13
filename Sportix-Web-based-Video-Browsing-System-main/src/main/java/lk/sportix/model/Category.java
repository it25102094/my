package lk.sportix.model;

/**
 * Category Model — POJO representing video categories (e.g., Match Highlights, Live Matches, Shorts).
 * Maps directly to the 'categories' table in sportix_db.
 */
public class Category {
    private int categoryId;
    private String categoryName;
    private String description;

    // Default Constructor
    public Category() {}

    // Parameterized Constructor
    public Category(int categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
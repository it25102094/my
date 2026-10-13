package lk.sportix.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Database Connection Manager for Sportix (lk.sportix.util).
 * Automatically loads database credentials from src/main/resources/db.properties.
 */
public class DBConnection {

    private static DBConnection instance;
    private String url;
    private String username;
    private String password;

    private DBConnection() {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC Driver not found in classpath!", e);
        }
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                this.url = props.getProperty("db.url");
                this.username = props.getProperty("db.username");
                this.password = props.getProperty("db.password");
            } else {
                // Fallback defaults if db.properties is missing
                this.url = "jdbc:mysql://localhost:3306/sportix_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                this.username = "root";
                this.password = "root";
            }
        } catch (IOException e) {
            System.err.println("Failed to read db.properties: " + e.getMessage());
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Returns a fresh JDBC connection so DAO methods can safely use try-with-resources
     * without accidentally closing a shared connection across multiple concurrent users.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.username, this.password);
    }
}
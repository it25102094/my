package lk.sportix.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton Database Connection Manager for Sportix (lk.sportix.util).
 * DESIGN PATTERN: Singleton — exactly one DBConnection object exists for the whole web app.
 *
 * Automatically loads database credentials from src/main/resources/db.properties:
 *   db.driver   (optional, defaults to the MySQL Connector/J driver)
 *   db.url
 *   db.username
 *   db.password
 */
public class DBConnection {

    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/sportix_db?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&connectionTimeZone=LOCAL";

    private static DBConnection instance;
    private String driver = DEFAULT_DRIVER;
    private String url = DEFAULT_URL;
    private String username = "root";
    private String password = "root";

    private DBConnection() {
        loadProperties();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC Driver '" + driver + "' not found in classpath! "
                    + "Check that mysql-connector-j is listed in pom.xml.", e);
        }
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                this.driver = props.getProperty("db.driver", DEFAULT_DRIVER).trim();
                this.url = props.getProperty("db.url", DEFAULT_URL).trim();
                this.username = props.getProperty("db.username", "root").trim();
                this.password = props.getProperty("db.password", "");
            }
            // else: fall back to the defaults above (local MySQL, root/root)
        } catch (IOException e) {
            System.err.println("Failed to read db.properties, using defaults: " + e.getMessage());
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
        try {
            return DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException e) {
            System.err.println("DBConnection: cannot connect to " + url + " as '" + username
                    + "'. Is MySQL running and was database/sportix_schema.sql executed? -> " + e.getMessage());
            throw e;
        }
    }
}

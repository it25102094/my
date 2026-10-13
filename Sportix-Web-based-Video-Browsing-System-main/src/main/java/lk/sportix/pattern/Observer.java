package lk.sportix.pattern;

/**
 * Observer Interface — Part of the Observer Design Pattern.
 * Represents a RegisteredUser receiving an automated alert.
 */
public interface Observer {
    void update(String message);
}
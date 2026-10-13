package lk.sportix.pattern;

/**
 * Subject Interface — Part of the Observer Design Pattern.
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(String message);
}
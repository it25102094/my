package lk.sportix.pattern;

import lk.sportix.model.Video;

/**
 * Subject Interface — Part of the OBSERVER design pattern (Member 5: Notifications).
 * Keeps a list of {@link Observer}s and notifies all of them when its state changes
 * (here: when a video becomes publicly visible).
 */
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers(Video publishedVideo);
}

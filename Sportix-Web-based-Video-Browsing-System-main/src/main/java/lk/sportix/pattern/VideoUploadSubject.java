package lk.sportix.pattern;

import lk.sportix.model.Video;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete Subject — OBSERVER design pattern (Member 5: Notifications).
 *
 * Represents ONE "a new video was published" event. A fresh subject is created for every
 * event (see NotificationService#notifyFollowersOfNewVideo), so observers of different
 * uploads can never mix — this is thread-safe even though servlets serve many users at once.
 *
 * Flow:  VideoService / AdminService publishes a video
 *        -> NotificationService loads the followers of the video's teams
 *        -> registerObserver(new UserObserver(...)) for each follower
 *        -> notifyObservers(video)  -> every UserObserver.update(video) stores a notification
 */
public class VideoUploadSubject implements Subject {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void registerObserver(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Video publishedVideo) {
        if (publishedVideo == null) return;
        for (Observer observer : observers) {
            observer.update(publishedVideo);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }

    public List<Observer> getObservers() {
        return Collections.unmodifiableList(observers);
    }
}

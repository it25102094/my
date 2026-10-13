package lk.sportix.service;

import lk.sportix.pattern.Observer;
import lk.sportix.pattern.Subject;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationService — Acts as the Subject in the Observer Pattern.
 * Pushes automated alerts to RegisteredUsers (Observers).
 */
public class NotificationService implements Subject {

    private final List<Observer> observers;

    public NotificationService() {
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
        // Clear observers after notifying so it's ready for the next event
        observers.clear();
    }

    /**
     * Business Logic Method: Triggered when a new video is published by a team.
     *
     * @param teamName The name of the team that posted the video.
     * @param followerIds A list of User IDs who follow this team.
     */
    public void notifyFollowersOfNewVideo(String teamName, List<Integer> followerIds) {
        if (followerIds == null || followerIds.isEmpty()) return;

        // 1. Register all followers as Observers
        for (Integer userId : followerIds) {
            registerObserver(new lk.sportix.pattern.UserObserver(userId));
        }

        // 2. Formulate the alert message
        String alertMessage = teamName + " just published a new video! Check it out now.";

        // 3. Push the notification to all registered observers
        notifyObservers(alertMessage);
    }
}
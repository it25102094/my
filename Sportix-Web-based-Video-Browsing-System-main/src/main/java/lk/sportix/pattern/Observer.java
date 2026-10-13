package lk.sportix.pattern;

import lk.sportix.model.Video;

/**
 * Observer Interface — Part of the OBSERVER design pattern (Member 5: Notifications).
 *
 * An Observer is a registered user who follows a team. When the Subject
 * ({@link VideoUploadSubject}) announces that a new video of that team was published,
 * every attached Observer receives {@link #update(Video)} and reacts to it
 * (a {@link UserObserver} stores a notification for its user).
 */
public interface Observer {
    void update(Video publishedVideo);
}

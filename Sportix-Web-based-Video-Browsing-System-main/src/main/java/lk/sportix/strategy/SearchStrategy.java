package lk.sportix.strategy;

import lk.sportix.model.Video;
import java.util.List;

/**
 * Strategy Design Pattern Interface (Member 2)
 * Defines a common contract for sorting video search results dynamically.
 */
public interface SearchStrategy {
    List<Video> sort(List<Video> videos);
}
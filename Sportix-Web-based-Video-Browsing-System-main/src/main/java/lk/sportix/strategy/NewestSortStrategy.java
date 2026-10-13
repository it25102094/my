package lk.sportix.strategy;

import lk.sportix.model.Video;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy (Member 2): newest uploads first.
 * Sorts by the real upload time (videos.created_at). Videos with the same timestamp
 * (e.g. seed data inserted in the same second) fall back to the higher video id.
 */
public class NewestSortStrategy implements SearchStrategy {
    @Override
    public List<Video> sort(List<Video> videos) {
        List<Video> sorted = new ArrayList<>(videos);
        Comparator<Video> byDate = Comparator.comparing(Video::getCreatedAt,
                Comparator.nullsFirst(Comparator.<Timestamp>naturalOrder()));
        sorted.sort(byDate.thenComparingInt(Video::getVideoId).reversed());
        return sorted;
    }
}

package lk.sportix.strategy;

import lk.sportix.model.Video;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NewestSortStrategy implements SearchStrategy {
    @Override
    public List<Video> sort(List<Video> videos) {
        List<Video> sorted = new ArrayList<>(videos);
        sorted.sort(Comparator.comparingInt(Video::getVideoId).reversed());
        return sorted;
    }
}
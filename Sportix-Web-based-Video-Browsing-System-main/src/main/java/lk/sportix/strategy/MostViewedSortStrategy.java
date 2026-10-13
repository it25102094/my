package lk.sportix.strategy;

import lk.sportix.model.Video;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MostViewedSortStrategy implements SearchStrategy {
    @Override
    public List<Video> sort(List<Video> videos) {
        List<Video> sorted = new ArrayList<>(videos);
        sorted.sort(Comparator.comparingInt(Video::getViewCount).reversed());
        return sorted;
    }
}
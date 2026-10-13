package lk.sportix.strategy;

import lk.sportix.model.Advertisement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy (Member 5): fair rotation.
 * The ads with the FEWEST impressions so far are shown first, so every running ad gets
 * roughly the same exposure over time regardless of campaign priority.
 */
public class RotationAdSelectionStrategy implements AdSelectionStrategy {
    @Override
    public List<Advertisement> select(List<Advertisement> runningAds, int limit) {
        List<Advertisement> pool = new ArrayList<>(runningAds);
        pool.sort(Comparator.comparingInt(Advertisement::getImpressions)
                .thenComparingInt(Advertisement::getAdId));
        return pool.subList(0, Math.min(Math.max(limit, 0), pool.size()));
    }
}

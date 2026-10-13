package lk.sportix.strategy;

import lk.sportix.model.Advertisement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Concrete Strategy (Member 5): ads of HIGH priority campaigns (priority 1) are shown first,
 * then NORMAL (2), then LOW (3). Ads inside the same priority level are shuffled so that
 * advertisers with equal priority share the slot fairly.
 */
public class PriorityAdSelectionStrategy implements AdSelectionStrategy {
    @Override
    public List<Advertisement> select(List<Advertisement> runningAds, int limit) {
        List<Advertisement> pool = new ArrayList<>(runningAds);
        Collections.shuffle(pool);                                   // fairness inside one priority level
        pool.sort(Comparator.comparingInt(Advertisement::getCampaignPriority)); // stable sort keeps shuffle order
        return pool.subList(0, Math.min(Math.max(limit, 0), pool.size()));
    }
}

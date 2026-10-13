package lk.sportix.strategy;

import lk.sportix.model.Advertisement;

import java.util.List;

/**
 * STRATEGY design pattern interface (Member 5: Advertising Management, IT25102094).
 *
 * Decides WHICH of the currently running ads are shown in an ad slot.
 * AdService picks a concrete strategy per placement:
 *   - {@link PriorityAdSelectionStrategy}  -> home-page banner (high-priority campaigns first)
 *   - {@link RotationAdSelectionStrategy}  -> video / search page cards (fair rotation)
 * New selection rules can be added later without touching AdService or the servlets.
 */
public interface AdSelectionStrategy {
    List<Advertisement> select(List<Advertisement> runningAds, int limit);
}

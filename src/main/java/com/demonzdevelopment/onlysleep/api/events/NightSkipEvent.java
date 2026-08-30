package com.demonzdevelopment.onlysleep.api.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired just before Onlysleep skips the night in a world.
 * <p>
 * This is cancellable - cancel to prevent the skip. You can also modify the target time
 * or change whether weather should be cleared via setters if needed (future).
 * <p>
 * Called from {@link com.demonzdevelopment.onlysleep.manager.SleepManager#skipNight(World)}
 * and {@link com.demonzdevelopment.onlysleep.manager.SleepManager#forceSkipNight(World)}.
 */
public class NightSkipEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final Player initiator;
    private final int sleepingCount;
    private final int requiredCount;
    private final int totalEligible;
    private boolean cancelled = false;

    public NightSkipEvent(World world, Player initiator, int sleepingCount, int requiredCount, int totalEligible) {
        this.world = world;
        this.initiator = initiator;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
        this.totalEligible = totalEligible;
    }

    public World getWorld() { return world; }

    /**
     * @return the player who initiated the skip, or {@code null} for a forced skip
     */
    public Player getInitiator() { return initiator; }

    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }
    public int getTotalEligible() { return totalEligible; }

    @Override
    public boolean isCancelled() { return cancelled; }

    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override
    public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}

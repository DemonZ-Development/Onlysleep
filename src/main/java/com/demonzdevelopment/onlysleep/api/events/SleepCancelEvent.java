package com.demonzdevelopment.onlysleep.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player stops sleeping and causes a scheduled skip to be cancelled,
 * or when a player was removed from the sleeping set.
 */
public class SleepCancelEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Cause { BED_LEAVE, QUIT, CANCELLED }

    private final Player player;
    private final Cause cause;
    private final int sleepingCount;
    private final int requiredCount;

    public SleepCancelEvent(Player player, Cause cause, int sleepingCount, int requiredCount) {
        this.player = player;
        this.cause = cause;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
    }

    public Player getPlayer() { return player; }
    public Cause getCause() { return cause; }
    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

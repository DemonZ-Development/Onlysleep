package com.demonzdevelopment.onlysleep.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;








public class SleepStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int sleepingCount;
    private final int requiredCount;
    private boolean cancelled = false;

    public SleepStartEvent(Player player, int sleepingCount, int requiredCount) {
        this.player = player;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
    }

    public Player getPlayer() { return player; }

    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

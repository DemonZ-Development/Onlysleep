package com.demonzdevelopment.onlysleep.api.events;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;










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

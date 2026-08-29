package com.demonzdevelopment.onlysleep.fabric.api.events;

import net.minecraft.server.level.ServerPlayer;

public class SleepCancelEvent {
    public enum Cause { BED_LEAVE, QUIT }

    private final ServerPlayer player;
    private final Cause cause;
    private final int sleepingCount;
    private final int requiredCount;

    public SleepCancelEvent(ServerPlayer player, Cause cause, int sleepingCount, int requiredCount) {
        this.player = player;
        this.cause = cause;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
    }

    public ServerPlayer getPlayer() { return player; }
    public Cause getCause() { return cause; }
    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }
}

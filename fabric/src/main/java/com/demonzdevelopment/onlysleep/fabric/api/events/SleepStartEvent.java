package com.demonzdevelopment.onlysleep.fabric.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public class SleepStartEvent {

    public interface Listener {
        void onSleepStart(SleepStartEvent event);
    }

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(
        Listener.class,
        listeners -> event -> {
            for (Listener listener : listeners) listener.onSleepStart(event);
        }
    );

    private final ServerPlayer player;
    private final int sleepingCount;
    private final int requiredCount;
    private boolean cancelled = false;

    public SleepStartEvent(ServerPlayer player, int sleepingCount, int requiredCount) {
        this.player = player;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
    }

    public ServerPlayer getPlayer() { return player; }
    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}

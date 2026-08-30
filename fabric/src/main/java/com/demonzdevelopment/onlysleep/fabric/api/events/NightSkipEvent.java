package com.demonzdevelopment.onlysleep.fabric.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric equivalent of NightSkipEvent - cancellable before night is skipped.
 * Register a listener with {@code NightSkipEvent.EVENT.register(...)}.
 */
public class NightSkipEvent {
    public interface Listener {
        void onNightSkip(NightSkipEvent event);
    }

    public static final Event<Listener> EVENT = EventFactory.createArrayBacked(
        Listener.class,
        listeners -> event -> {
            for (Listener listener : listeners) listener.onNightSkip(event);
        }
    );

    private final ServerLevel level;
    private final ServerPlayer initiator;
    private final int sleepingCount;
    private final int requiredCount;
    private final int totalEligible;
    private boolean cancelled = false;

    public NightSkipEvent(ServerLevel level, ServerPlayer initiator, int sleepingCount, int requiredCount, int totalEligible) {
        this.level = level;
        this.initiator = initiator;
        this.sleepingCount = sleepingCount;
        this.requiredCount = requiredCount;
        this.totalEligible = totalEligible;
    }

    public ServerLevel getLevel() { return level; }
    public ServerPlayer getInitiator() { return initiator; }
    public int getSleepingCount() { return sleepingCount; }
    public int getRequiredCount() { return requiredCount; }
    public int getTotalEligible() { return totalEligible; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}

package com.demonzdevelopment.onlysleep.fabric.api.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric equivalent of NightSkipEvent - cancellable before night is skipped.
 * Fired from SleepManager.skipNight. For Fabric this is a plain object; listeners should use
 * a custom event bus or check via mixin. Currently cancellable via explicit API: if you need
 * cancellation, use OnlysleepFabricAPI and check before skip via your own logic, or request
 * Fabric's event API integration. This class is the data holder for parity.
 */
public class NightSkipEvent {
    private final ServerLevel level;
    private final ServerPlayer initiator; // may be null
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

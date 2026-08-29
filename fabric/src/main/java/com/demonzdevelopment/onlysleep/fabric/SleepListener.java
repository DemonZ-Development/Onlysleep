package com.demonzdevelopment.onlysleep.fabric;

import com.demonzdevelopment.onlysleep.fabric.tracker.AfkTracker;
import com.demonzdevelopment.onlysleep.fabric.tracker.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.fabric.util.NightMath;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class SleepListener {

    private final OnlysleepMod mod;
    private final SleepManager sleepManager;

    public SleepListener(OnlysleepMod mod, SleepManager sleepManager) {
        this.mod = mod;
        this.sleepManager = sleepManager;
    }

    public void register() {
        EntitySleepEvents.START_SLEEPING.register((entity, pos) -> {
            if (entity instanceof ServerPlayer player) {
                onBedEnter(player);
            }
        });

        EntitySleepEvents.STOP_SLEEPING.register((entity, pos) -> {
            if (entity instanceof ServerPlayer player) {
                sleepManager.onBedLeave(player);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            AfkTracker.onJoin(player);
            OfflinePlayerTracker.onJoin(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.player;
            AfkTracker.onQuit(player);
            sleepManager.onQuit(player);
        });
    }

    private void onBedEnter(ServerPlayer player) {
        if (mod.permissions().isExempt(player)) return;

        ServerLevel level = player.level();
        if (!mod.config().isWorldEnabled(SleepManager.worldKey(level))) {
            player.sendSystemMessage(mod.config().getMessage("sleep.world-disabled"));
            return;
        }

        long time = sleepManager.dayTimeOf(level);
        boolean storm = level.isRaining() || level.isThundering();
        if (!NightMath.isNight(time) && !storm) {
            player.sendSystemMessage(mod.config().getMessage("sleep.already-day"));
            return;
        }

        sleepManager.onBedEnter(player);
    }
}

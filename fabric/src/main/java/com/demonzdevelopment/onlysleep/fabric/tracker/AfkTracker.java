package com.demonzdevelopment.onlysleep.fabric.tracker;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkTracker {

    private static final long SAMPLE_INTERVAL_TICKS = 20;

    private static final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private static final Map<UUID, Snapshot> lastSnapshot = new ConcurrentHashMap<>();

    private static int timeSeconds = 300;
    private static boolean registered;

    public record Snapshot(BlockPos pos, float yaw, float pitch) {}

    public static void init(int configuredTimeoutSeconds) {
        timeSeconds = configuredTimeoutSeconds;

        if (!registered) {
            registered = true;
            AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
                touch(player);
                return InteractionResult.PASS;
            });
            UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                touch(player);
                return InteractionResult.PASS;
            });
            UseItemCallback.EVENT.register((player, world, hand) -> {
                touch(player);
                return InteractionResult.PASS;
            });
        }
        lastActivity.clear();
        lastSnapshot.clear();
    }

    public static void shutdown() {
        lastActivity.clear();
        lastSnapshot.clear();
    }

    public static void sample(MinecraftServer server, long currentTick) {
        if (currentTick % SAMPLE_INTERVAL_TICKS != 0) return;
        long now = System.currentTimeMillis();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            Snapshot cur = new Snapshot(player.blockPosition(), player.getYRot(), player.getXRot());
            Snapshot prev = lastSnapshot.put(id, cur);

            if (prev == null || !sameActivity(prev, cur)) {
                lastActivity.put(id, now);
            }
        }
    }

    public static void onJoin(ServerPlayer player) {
        lastActivity.put(player.getUUID(), System.currentTimeMillis());
        lastSnapshot.put(player.getUUID(),
            new Snapshot(player.blockPosition(), player.getYRot(), player.getXRot()));
    }

    public static void onQuit(ServerPlayer player) {
        lastActivity.remove(player.getUUID());
        lastSnapshot.remove(player.getUUID());
    }

    public static void touch(net.minecraft.world.entity.player.Player player) {
        lastActivity.put(player.getUUID(), System.currentTimeMillis());
    }

    public static boolean isAfk(ServerPlayer player) {
        if (timeSeconds <= 0) return false;

        Long last = lastActivity.get(player.getUUID());
        if (last == null) {
            lastActivity.put(player.getUUID(), System.currentTimeMillis());
            return false;
        }
        return (System.currentTimeMillis() - last) >= (timeSeconds * 1000L);
    }

    private static boolean sameActivity(Snapshot a, Snapshot b) {
        return a.pos().equals(b.pos())
            && Math.round(a.yaw()) == Math.round(b.yaw())
            && Math.round(a.pitch()) == Math.round(b.pitch());
    }
}

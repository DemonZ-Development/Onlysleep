package com.demonzdevelopment.onlysleep.fabric.tracker;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

public class OfflinePlayerTracker {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L;

    private static final AtomicInteger knownPlayerCount = new AtomicInteger(-1);
    private static final AtomicLong lastRefresh = new AtomicLong(0);

    private static MinecraftServer server;

    public static void init(MinecraftServer instance) {
        server = instance;
        knownPlayerCount.set(-1);
        lastRefresh.set(0);

        refresh();
    }

    public static void shutdown() {
        server = null;
        knownPlayerCount.set(-1);
        lastRefresh.set(0);
    }

    public static void tick(long currentTick) {
        if (server == null) return;
        if (currentTick % 6000 != 0) return;
        if (System.currentTimeMillis() - lastRefresh.get() >= REFRESH_INTERVAL_MS) {
            refresh();
        }
    }

    public static void onJoin(ServerPlayer player) {
        int cached = knownPlayerCount.get();
        if (cached < 0) {
            refresh();
            return;
        }

        if (!playerDataFile(server, player.getUUID()).exists()) {
            knownPlayerCount.incrementAndGet();
        }

        int online = server.getPlayerList().getPlayerCount();
        knownPlayerCount.updateAndGet(current -> Math.max(current, online));
    }

    public static boolean hasOfflinePlayers() {
        int cached = knownPlayerCount.get();
        if (cached < 0) return true;
        MinecraftServer srv = server;
        if (srv == null) return false;
        return cached > srv.getPlayerList().getPlayerCount();
    }

    private static void refresh() {
        MinecraftServer srv = server;
        if (srv == null) return;
        try {
            File dir = playerDataDir(srv);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
            knownPlayerCount.set(files == null
                ? srv.getPlayerList().getPlayerCount()
                : Math.max(files.length, srv.getPlayerList().getPlayerCount()));
            lastRefresh.set(System.currentTimeMillis());
        } catch (Exception ignored) {
        }
    }

    private static Path worldPlayerDataPath(MinecraftServer srv) {
        return srv.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR);
    }

    private static File playerDataDir(MinecraftServer srv) {
        return worldPlayerDataPath(srv).toFile();
    }

    private static File playerDataFile(MinecraftServer srv, UUID uuid) {
        return worldPlayerDataPath(srv).resolve(uuid + ".dat").toFile();
    }
}

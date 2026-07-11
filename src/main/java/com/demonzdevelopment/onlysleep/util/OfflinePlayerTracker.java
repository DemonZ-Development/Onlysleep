package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class OfflinePlayerTracker implements Listener {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L;
    private static final AtomicInteger knownPlayerCount = new AtomicInteger(-1);
    private static final AtomicLong lastRefresh = new AtomicLong(0);
    private static SchedulerAdapter.ScheduledTask refreshTask;
    private static Onlysleep plugin;
    private static Listener registeredListener;

    public static void init(Onlysleep instance) {
        plugin = instance;

        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }

        OfflinePlayerTracker listener = new OfflinePlayerTracker();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListener = listener;

        refreshAsync();

        refreshTask = SchedulerAdapter.runGlobalTaskTimer(plugin, () -> {
            if (System.currentTimeMillis() - lastRefresh.get() >= REFRESH_INTERVAL_MS) {
                refreshAsync();
            }
        }, 200L, 6000L);
    }

    public static void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }

        knownPlayerCount.set(-1);
        lastRefresh.set(0);
    }

    public static int getKnownPlayerCount() {
        int cached = knownPlayerCount.get();
        if (cached >= 0) {
            return cached;
        }

        refreshAsync();
        try {
            return Bukkit.getOnlinePlayers().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean hasOfflinePlayers() {
        int cached = knownPlayerCount.get();
        if (cached < 0) return true;
        return cached > Bukkit.getOnlinePlayers().size();
    }

    public static CompletableFuture<Void> refreshAsync() {
        if (plugin == null) {
            CompletableFuture<Void> f = new CompletableFuture<>();
            f.complete(null);
            return f;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        SchedulerAdapter.runGlobalTask(plugin, () -> {
            try {
                int count = Bukkit.getOfflinePlayers().length;
                knownPlayerCount.set(count);
                lastRefresh.set(System.currentTimeMillis());
            } catch (Exception ignored) {

            } finally {
                future.complete(null);
            }
        });
        return future;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null || event.getPlayer() == null) return;

        if (knownPlayerCount.get() < 0) {
            refreshAsync();
            return;
        }

        if (!event.getPlayer().hasPlayedBefore()) {
            knownPlayerCount.incrementAndGet();
        }

        int online = Bukkit.getOnlinePlayers().size();
        knownPlayerCount.updateAndGet(current -> Math.max(current, online));
    }
}

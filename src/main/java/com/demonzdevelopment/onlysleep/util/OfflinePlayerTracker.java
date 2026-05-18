package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Caches the count of known (offline) players to avoid repeated expensive calls
 * to {@link Bukkit#getOfflinePlayers()}.
 *
 * <p>Used by the {@code require-all-players-online} feature. The count is:
 * <ol>
 *   <li>Loaded once asynchronously on first access</li>
 *   <li>Incremented when new players join (via {@link PlayerJoinEvent})</li>
 *   <li>Refreshed from disk every 5 minutes via a repeating task</li>
 * </ol>
 *
 * <p>This prevents the slow {@code Bukkit.getOfflinePlayers()} call from running
 * on every player bed-enter event.
 */
public class OfflinePlayerTracker implements Listener {

    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000L; // 5 minutes
    private static final AtomicInteger knownPlayerCount = new AtomicInteger(-1);
    private static final AtomicLong lastRefresh = new AtomicLong(0);
    private static SchedulerAdapter.ScheduledTask refreshTask;

    /**
     * Initialises the tracker: starts the periodic refresh and registers events.
     */
    public static void init(Onlysleep plugin) {
        plugin.getServer().getPluginManager().registerEvents(new OfflinePlayerTracker(), plugin);

        // Force an initial async load of the offline player count
        refreshAsync();

        // Refresh every 5 minutes
        refreshTask = SchedulerAdapter.runGlobalTaskTimer(plugin, () -> {
            if (System.currentTimeMillis() - lastRefresh.get() >= REFRESH_INTERVAL_MS) {
                refreshAsync();
            }
        }, 200L, 6000L); // First tick after 10s, then every 5 min
    }

    /**
     * Shuts down the periodic refresh task.
     */
    public static void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    /**
     * Returns the cached count of known players on this server.
     * If not yet loaded, triggers an async load and returns an estimate
     * based on current online players.
     */
    public static int getKnownPlayerCount() {
        int cached = knownPlayerCount.get();
        if (cached >= 0) {
            return cached;
        }

        // Not loaded yet — return online count as a lower-bound estimate
        // and trigger the async load
        refreshAsync();
        try {
            return Bukkit.getOnlinePlayers().size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns {@code true} if there are known offline players who might be
     * eligible but aren't online. Uses the cached count.
     */
    public static boolean hasOfflinePlayers() {
        return getKnownPlayerCount() > Bukkit.getOnlinePlayers().size();
    }

    /**
     * Asynchronously loads the offline player count from disk.
     * This is the only place {@link Bukkit#getOfflinePlayers()} is called.
     */
    public static CompletableFuture<Void> refreshAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                int count = Bukkit.getOfflinePlayers().length;
                knownPlayerCount.set(count);
                lastRefresh.set(System.currentTimeMillis());
            } catch (Exception ignored) {
                // Best-effort, will retry on next refresh cycle
            }
        });
    }

    /**
     * Called when a player joins the server. Ensures the known-player count
     * is at least as high as the number of unique players seen.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event == null || event.getPlayer() == null) return;

        // If the count has never been loaded, trigger the full load
        if (knownPlayerCount.get() < 0) {
            refreshAsync();
        }

        // Ensure the known count is at least the current online count
        int online = Bukkit.getOnlinePlayers().size();
        knownPlayerCount.updateAndGet(current -> Math.max(current, online));
    }
}

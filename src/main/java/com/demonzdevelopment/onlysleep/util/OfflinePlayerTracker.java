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
    private static Onlysleep plugin;
    private static Listener registeredListener;

    /**
     * Initialises the tracker: starts the periodic refresh and registers events.
     */
    public static void init(Onlysleep instance) {
        plugin = instance;

        // Unregister any previously-registered listener (e.g. from a reload)
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }

        OfflinePlayerTracker listener = new OfflinePlayerTracker();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListener = listener;

        // Force an initial load of the offline player count
        refreshAsync();

        // Refresh every 5 minutes
        refreshTask = SchedulerAdapter.runGlobalTaskTimer(plugin, () -> {
            if (System.currentTimeMillis() - lastRefresh.get() >= REFRESH_INTERVAL_MS) {
                refreshAsync();
            }
        }, 200L, 6000L); // First tick after 10s, then every 5 min
    }

    /**
     * Shuts down the periodic refresh task and unregisters listeners.
     */
    public static void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }
        // Reset the cache so the next init forces a fresh load and the
        // "not loaded yet" sentinel logic works again after a reload.
        knownPlayerCount.set(-1);
        lastRefresh.set(0);
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
     * If the count hasn't been loaded yet, returns {@code true} to be safe.
     */
    public static boolean hasOfflinePlayers() {
        int cached = knownPlayerCount.get();
        if (cached < 0) return true; // Not loaded yet — assume there are offline players
        return cached > Bukkit.getOnlinePlayers().size();
    }

    /**
     * Loads the offline player count.
     * <p>Runs on the global scheduler thread (instead of ForkJoinPool) because
     * {@link Bukkit#getOfflinePlayers()} performs disk I/O that is not guaranteed
     * thread-safe on all server implementations and can throw on Folia's async pool.
     */
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
                // Best-effort, will retry on next refresh cycle
            } finally {
                future.complete(null);
            }
        });
        return future;
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
            return;
        }

        // If this is a new player who has never played before, increment the unique count
        if (!event.getPlayer().hasPlayedBefore()) {
            knownPlayerCount.incrementAndGet();
        }

        // Ensure the known count is at least the current online count
        int online = Bukkit.getOnlinePlayers().size();
        knownPlayerCount.updateAndGet(current -> Math.max(current, online));
    }
}

package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in AFK tracker that monitors player activity.
 *
 * <p>Players are marked AFK if they haven't moved or interacted for
 * {@code afk-time-seconds} seconds. Relies on {@link PlayerMoveEvent}
 * and {@link PlayerInteractEvent} to reset the activity timer.
 *
 * <p>AFK status can be retrieved via {@link #isAfk(Player)}.
 */
public class AfkTracker implements Listener {

    private static final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private static Onlysleep plugin;
    private static SchedulerAdapter.ScheduledTask cleanupTask;

    /**
     * Initialises the AFK tracker and registers event listeners.
     * Call this in {@code onEnable()}.
     */
    public static void init(Onlysleep instance) {
        plugin = instance;

        if (plugin.getConfigManager().getAfkTimeSeconds() <= 0) return; // Disabled

        plugin.getServer().getPluginManager().registerEvents(new AfkTracker(), plugin);

        // Periodic cleanup of stale entries (every 20 ticks / 1 second)
        cleanupTask = SchedulerAdapter.runGlobalTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            long timeout = plugin.getConfigManager().getAfkTimeSeconds() * 1000L;
            lastActivity.entrySet().removeIf(entry -> {
                Player p = org.bukkit.Bukkit.getPlayer(entry.getKey());
                return p == null || !p.isOnline();
            });
        }, 100L, 100L);
    }

    /**
     * Shuts down the AFK tracker and unregisters listeners.
     * Call this in {@code onDisable()}.
     */
    public static void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        lastActivity.clear();
    }

    /**
     * Updates the last activity timestamp for a player.
     */
    public static void updateActivity(Player player) {
        if (player == null) return;
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Checks whether a player is currently AFK based on inactivity timeout.
     *
     * @param player the player to check
     * @return {@code true} if the player has been inactive longer than the threshold
     */
    public static boolean isAfk(Player player) {
        if (plugin == null) return false;
        int timeout = plugin.getConfigManager().getAfkTimeSeconds();
        if (timeout <= 0) return false;

        Long last = lastActivity.get(player.getUniqueId());
        if (last == null) {
            // First time seeing this player — mark them as active now
            updateActivity(player);
            return false;
        }
        return (System.currentTimeMillis() - last) >= (timeout * 1000L);
    }

    /**
     * Resets the inactivity timer for the player who moved.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Ignore tiny movements (head-turning / mouse jitter)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }
        updateActivity(event.getPlayer());
    }

    /**
     * Resets the inactivity timer when the player interacts.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }
}

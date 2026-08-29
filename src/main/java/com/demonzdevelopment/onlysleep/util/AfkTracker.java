package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkTracker implements Listener {

    private static final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private static Onlysleep plugin;
    private static SchedulerAdapter.ScheduledTask cleanupTask;
    private static Listener registeredListener;

    public static void init(Onlysleep instance) {
        plugin = instance;

        if (plugin.getConfigManager().getAfkTimeSeconds() <= 0) return;

        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }

        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }

        AfkTracker listener = new AfkTracker();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListener = listener;

        cleanupTask = SchedulerAdapter.runGlobalTaskTimer(plugin, () ->
            lastActivity.entrySet().removeIf(entry -> {
                Player p = org.bukkit.Bukkit.getPlayer(entry.getKey());
                return p == null || !p.isOnline();
            }), 100L, 100L);
    }

    public static void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        if (registeredListener != null) {
            HandlerList.unregisterAll(registeredListener);
            registeredListener = null;
        }
        lastActivity.clear();
    }

    public static void updateActivity(Player player) {
        if (player == null) return;
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static boolean isAfk(Player player) {
        if (plugin == null) return false;
        int timeout = plugin.getConfigManager().getAfkTimeSeconds();
        if (timeout <= 0) return false;

        Long last = lastActivity.get(player.getUniqueId());
        if (last == null) {

            updateActivity(player);
            return false;
        }
        return (System.currentTimeMillis() - last) >= (timeout * 1000L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.getTo() == null) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != null) {
            lastActivity.remove(event.getPlayer().getUniqueId());
        }
    }
}

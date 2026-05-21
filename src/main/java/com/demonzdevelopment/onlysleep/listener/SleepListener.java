package com.demonzdevelopment.onlysleep.listener;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SleepListener implements Listener {

    private final Onlysleep plugin;
    private final SleepManager sleepManager;
    private final ConfigManager configManager;

    public SleepListener(Onlysleep plugin, SleepManager sleepManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.sleepManager = sleepManager;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Check permissions
        if (player.hasPermission("onlysleep.exempt")) return;

        // Check if monsters prevented sleep — vanilla already shows its own message,
        // so we don't add a duplicate.
        BedEnterResult result = event.getBedEnterResult();
        if (result == BedEnterResult.NOT_SAFE) {
            return;
        }

        // Only proceed if bed enter was successful
        if (result != BedEnterResult.OK) {
            return;
        }

        // Check world is enabled
        if (!configManager.isWorldEnabled(player.getWorld().getName())) {
            player.sendMessage(configManager.getMessage("sleep.world-disabled"));
            return;
        }

        // Check if sleeping is possible in this world (night or storm)
        if (!isSleepable(player.getWorld())) {
            player.sendMessage(configManager.getMessage("sleep.already-day"));
            return;
        }

        // Check disabled game modes
        if (configManager.isGameModeDisabled(player.getGameMode().name())) {
            return;
        }

        // Check creative mode
        if (player.getGameMode() == GameMode.CREATIVE && configManager.isIgnoreCreativeMode()) {
            return;
        }

        // Check if someone is already skipping the night
        if (sleepManager.isSkipScheduled(player.getWorld())) {
            player.sendMessage(configManager.getMessage("sleep.already-skipping"));
            return;
        }

        // Handle the bed enter event
        sleepManager.onPlayerBedEnter(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("onlysleep.exempt")) return;

        sleepManager.onPlayerBedLeave(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sleepManager.onPlayerQuit(event.getPlayer());
    }

    /**
     * Checks if the world is in a state where sleeping makes sense (night or storm).
     * Uses the current world time to determine if it's night.
     */
    private boolean isSleepable(World world) {
        long time = world.getTime();
        boolean isNight = time >= 12542 && time <= 23458;
        boolean isStorm = world.hasStorm() || world.isThundering();
        return isNight || isStorm;
    }
}

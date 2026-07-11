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

        if (player.hasPermission("onlysleep.exempt")) return;

        BedEnterResult result = event.getBedEnterResult();
        if (result == BedEnterResult.NOT_SAFE) {
            return;
        }

        if (result != BedEnterResult.OK) {
            return;
        }

        if (!configManager.isWorldEnabled(player.getWorld().getName())) {
            player.sendMessage(configManager.getMessage("sleep.world-disabled"));
            return;
        }

        if (!isSleepable(player.getWorld())) {
            player.sendMessage(configManager.getMessage("sleep.already-day"));
            return;
        }

        if (configManager.isGameModeDisabled(player.getGameMode().name())) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE && configManager.isIgnoreCreativeMode()) {
            return;
        }

        if (sleepManager.isSkipScheduled(player.getWorld())) {
            player.sendMessage(configManager.getMessage("sleep.already-skipping"));
            return;
        }

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

    private boolean isSleepable(World world) {
        long time = world.getTime();
        boolean isNight = SleepManager.isNight(time);
        boolean isStorm = world.hasStorm() || world.isThundering();
        return isNight || isStorm;
    }
}

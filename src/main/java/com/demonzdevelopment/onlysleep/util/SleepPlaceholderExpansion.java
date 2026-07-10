package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SleepPlaceholderExpansion extends PlaceholderExpansion {

    private final Onlysleep plugin;

    public SleepPlaceholderExpansion(Onlysleep plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "onlysleep";
    }

    @Override
    public @NotNull String getAuthor() {
        String author = plugin.getDescription().getAuthors().isEmpty()
            ? "Demonz Development" : plugin.getDescription().getAuthors().get(0);
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        SleepManager sleepManager = plugin.getSleepManager();
        String paramsLower = params.toLowerCase();

        switch (paramsLower) {
            case "version":
                return plugin.getDescription().getVersion();
            case "platform":
                return plugin.getPlatform().getDisplayName();
            case "percentage":
                return String.valueOf(plugin.getConfigManager().getSleepPercentage());
        }

        if (params.startsWith("world_sleeping_")) {
            String worldName = params.substring("world_sleeping_".length());
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) return "0";
            return String.valueOf(sleepManager.getSleepingCount(world));
        }
        if (params.startsWith("world_required_")) {
            String worldName = params.substring("world_required_".length());
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) return "0";
            return String.valueOf(sleepManager.getRequiredSleepingCount(world));
        }
        if (params.startsWith("world_total_")) {
            String worldName = params.substring("world_total_".length());
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) return "0";
            return String.valueOf(sleepManager.getTotalPlayerCount(world));
        }

        if (player == null) return "";

        switch (paramsLower) {
            case "sleeping":
                return String.valueOf(sleepManager.getSleepingCount(player.getWorld()));

            case "required":
                return String.valueOf(sleepManager.getRequiredSleepingCount(player.getWorld()));

            case "progress": {
                int required = sleepManager.getRequiredSleepingCount(player.getWorld());
                int current = sleepManager.getSleepingCount(player.getWorld());
                if (required <= 0) return "0";
                int pct = (int) ((double) current / required * 100);
                return String.valueOf(Math.min(100, pct));
            }

            case "progress_bar":
                return plugin.getConfigManager().buildProgressBar(
                    sleepManager.getSleepingCount(player.getWorld()),
                    sleepManager.getRequiredSleepingCount(player.getWorld())
                );

            case "sleeping_names": {
                Set<UUID> sleeping = sleepManager.getSleepingPlayers(player.getWorld());
                if (sleeping == null || sleeping.isEmpty()) return "None";
                return sleeping.stream()
                    .map(uuid -> {
                        org.bukkit.entity.Player p =
                            org.bukkit.Bukkit.getPlayer(uuid);
                        return p != null ? p.getName() : "Unknown";
                    })
                    .collect(Collectors.joining(", "));
            }

            case "skipping":
                return String.valueOf(sleepManager.isSkipScheduled(player.getWorld()));

            case "enabled":
                return String.valueOf(plugin.getConfigManager().isWorldEnabled(player.getWorld().getName()));

            case "is_sleepable": {
                long time = player.getWorld().getTime();
                boolean isNight = SleepManager.isNight(time);
                boolean isStorm = player.getWorld().hasStorm() || player.getWorld().isThundering();
                return String.valueOf(isNight || isStorm);
            }

            case "afk":
                return String.valueOf(AfkTracker.isAfk(player));

            case "is_sleeping":
                return String.valueOf(sleepManager.isPlayerSleeping(player));

            case "total":
                return String.valueOf(sleepManager.getTotalPlayerCount(player.getWorld()));

            case "status":
                if (sleepManager.isPlayerSleeping(player)) return "Sleeping";
                if (AfkTracker.isAfk(player)) return "AFK";
                return "Awake";

            case "is_night": {
                long time = player.getWorld().getTime();
                boolean isNight = SleepManager.isNight(time);
                return String.valueOf(isNight);
            }

            default:
                return null; 
        }
    }
}

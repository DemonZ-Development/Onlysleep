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

/**
 * PlaceholderAPI expansion providing sleep-related placeholders.
 *
 * <p>Registered automatically when PlaceholderAPI is detected on the server.
 *
 * <p><b>Placeholders:</b>
 * <ul>
 *   <li>{@code %onlysleep_sleeping%} — Number of sleeping players in the player's world</li>
 *   <li>{@code %onlysleep_required%} — Number of players required to skip night</li>
 *   <li>{@code %onlysleep_progress%} — Percentage of required sleepers achieved</li>
 *   <li>{@code %onlysleep_progress_bar%} — Visual progress bar of sleep progress</li>
 *   <li>{@code %onlysleep_sleeping_names%} — Comma-separated names of sleeping players</li>
 *   <li>{@code %onlysleep_skipping%} — Whether a night skip is currently scheduled (true/false)</li>
 *   <li>{@code %onlysleep_enabled%} — Whether sleeping is enabled in the player's world</li>
 *   <li>{@code %onlysleep_is_sleepable%} — Whether it's currently night/storm in the world</li>
 *   <li>{@code %onlysleep_version%} — Onlysleep plugin version</li>
 *   <li>{@code %onlysleep_afk%} — Whether the player is considered AFK (true/false)</li>
 *   <li>{@code %onlysleep_is_sleeping%} — Whether the player is currently counted as sleeping</li>
 *   <li>{@code %onlysleep_percentage%} — Configured sleep percentage value</li>
 *   <li>{@code %onlysleep_total%} — Total eligible player count in the world</li>
 *   <li>{@code %onlysleep_status%} — "Sleeping" or "Awake" based on player's state</li>
 *   <li>{@code %onlysleep_is_night%} — Whether it's currently night time in the player's world</li>
 *   <li>{@code %onlysleep_platform%} — Server platform name (Folia, Paper, Spigot, Bukkit)</li>
 * </ul>
 */
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
        if (player == null) return "";

        SleepManager sleepManager = plugin.getSleepManager();

        switch (params.toLowerCase()) {
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
                boolean isNight = time >= 12542 && time <= 23458;
                boolean isStorm = player.getWorld().hasStorm() || player.getWorld().isThundering();
                return String.valueOf(isNight || isStorm);
            }

            case "version":
                return plugin.getDescription().getVersion();

            case "afk":
                return String.valueOf(AfkTracker.isAfk(player));

            case "is_sleeping":
                return String.valueOf(sleepManager.isPlayerSleeping(player));

            case "percentage":
                return String.valueOf(plugin.getConfigManager().getSleepPercentage());

            case "total":
                return String.valueOf(sleepManager.getTotalPlayerCount(player.getWorld()));

            case "status":
                if (sleepManager.isPlayerSleeping(player)) return "Sleeping";
                if (AfkTracker.isAfk(player)) return "AFK";
                return "Awake";

            case "is_night": {
                long time = player.getWorld().getTime();
                boolean isNight = time >= 12542 && time <= 23458;
                return String.valueOf(isNight);
            }

            case "platform":
                return plugin.getPlatform().getDisplayName();

            default:
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
                return null; // Unknown placeholder
        }
    }
}

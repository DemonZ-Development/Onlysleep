package com.demonzdevelopment.onlysleep.manager;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.util.AfkTracker;
import com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter.ScheduledTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SleepManager {

    private final Onlysleep plugin;
    private final ConfigManager configManager;
    private final Map<World, Set<UUID>> sleepingPlayers = new HashMap<>();
    private final Map<World, ScheduledTask> skipTasks = new HashMap<>();
    private final Map<World, BossBar> worldBossBars = new HashMap<>();
    private final Map<World, ScheduledTask> bossBarTasks = new HashMap<>();

    public SleepManager(Onlysleep plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Called when a player enters a bed.
     */
    public void onPlayerBedEnter(Player player) {
        World world = player.getWorld();

        if (!configManager.isWorldEnabled(world.getName())) return;

        sleepingPlayers.computeIfAbsent(world, k -> new HashSet<>()).add(player.getUniqueId());

        // Broadcast sleep start message
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getDisplayName());
        placeholders.put("count", String.valueOf(getSleepingCount(world)));
        placeholders.put("required", String.valueOf(getRequiredSleepingCount(world)));

        String message = configManager.getMessage("sleep.start-sleep", placeholders);
        for (Player p : world.getPlayers()) {
            p.sendMessage(message);
        }

        checkSleepStatus(world);
    }

    /**
     * Called when a player leaves a bed.
     */
    public void onPlayerBedLeave(Player player) {
        World world = player.getWorld();
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null) {
            if (players.remove(player.getUniqueId())) {
                // Broadcast cancellation message
                if (skipTasks.containsKey(world)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", player.getDisplayName());
                    String message = configManager.getMessage("sleep.cancelled", placeholders);
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(message);
                    }
                }
            }
            if (players.isEmpty()) {
                sleepingPlayers.remove(world);
            }
        }
        cancelSkip(world);
    }

    /**
     * Called when a player disconnects.
     */
    public void onPlayerQuit(Player player) {
        World world = player.getWorld();
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null) {
            players.remove(player.getUniqueId());
            if (players.isEmpty()) {
                sleepingPlayers.remove(world);
                cancelSkip(world);
            }
        }
    }

    /**
     * Checks if a night skip is currently scheduled for a world.
     */
    public boolean isSkipScheduled(World world) {
        return skipTasks.containsKey(world);
    }

    /**
     * Checks if a player is currently counted as sleeping.
     */
    public boolean isPlayerSleeping(Player player) {
        Set<UUID> players = sleepingPlayers.get(player.getWorld());
        return players != null && players.contains(player.getUniqueId());
    }

    /**
     * Checks if enough players are sleeping to skip the night.
     */
    private void checkSleepStatus(World world) {
        if (skipTasks.containsKey(world)) return; // Already scheduled

        int required = getRequiredSleepingCount(world);
        int current = getSleepingCount(world);

        if (current >= required && required > 0) {
            scheduleSkip(world);
        }
    }

    /**
     * Schedules the night skip after the configured delay.
     */
    private void scheduleSkip(World world) {
        int delay = configManager.getSkipDelayTicks();

        // Show boss bar
        if (configManager.isShowBossBar()) {
            showBossBarForWorld(world);
        }

        // Schedule the skip task using the adapter (Folia-compatible)
        ScheduledTask task = SchedulerAdapter.runTaskLater(plugin, world, () -> {
            skipNight(world);
        }, delay);

        skipTasks.put(world, task);
    }

    /**
     * Cancels a pending night skip for a world.
     */
    private void cancelSkip(World world) {
        ScheduledTask task = skipTasks.remove(world);
        if (task != null) {
            task.cancel();
        }
        removeBossBar(world);
    }

    /**
     * Executes the night skip logic.
     *
     * <p><b>Folia-safe:</b> World operations (time, weather) run on the region scheduler.
     * Global broadcasts are delegated to the global scheduler.</p>
     */
    private void skipNight(World world) {
        // Handle weather clearing
        final boolean clearedWeather;
        if (configManager.isClearWeather() && (world.hasStorm() || world.isThundering())) {
            if (configManager.isResetWeather()) {
                world.setStorm(false);
            }
            if (configManager.isResetThunder() && world.isThundering()) {
                world.setThundering(false);
            }
            if (configManager.isResetWeatherCycle()) {
                world.setWeatherDuration(Integer.MAX_VALUE);
                world.setThunderDuration(Integer.MAX_VALUE);
            }
            clearedWeather = true;
        } else {
            clearedWeather = false;
        }

        // Handle time change based on skip type
        String skipType = configManager.getSkipType();
        long targetTime = configManager.isResetTime() ? 0 : configManager.getMorningTime();

        switch (skipType.toLowerCase()) {
            case "gradual":
                scheduleGradualSkip(world, targetTime);
                break;
            case "speed":
                world.setTime(targetTime + 24000);
                break;
            case "instant":
            default:
                world.setTime(targetTime);
                break;
        }

        // Play sounds (per-world, safe on region thread)
        if (configManager.isPlaySounds()) {
            playSkipSound(world);
        }

        // Show title (per-world, safe on region thread)
        String playerName = getSleepingPlayerName(world);
        if (configManager.isShowTitle()) {
            showSkipTitle(world, playerName);
        }

        // Broadcast messages via global scheduler (Folia-safe)
        final String finalPlayerName = playerName;
        SchedulerAdapter.runGlobalTask(plugin, () ->
            broadcastSkipMessages(finalPlayerName, clearedWeather)
        );

        // Cleanup
        sleepingPlayers.remove(world);
        skipTasks.remove(world);
        removeBossBar(world);
    }

    /**
     * Broadcasts skip-related messages on the global thread (Folia-safe).
     */
    private void broadcastSkipMessages(String playerName, boolean clearedWeather) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);

        if (clearedWeather) {
            Bukkit.broadcastMessage(configManager.getMessage("weather.clearing", placeholders));
        }
        Bukkit.broadcastMessage(configManager.getMessage("sleep.enough-sleeping", placeholders));
    }

    /**
     * Schedules a gradual night skip (slowly moves time forward).
     */
    private void scheduleGradualSkip(World world, long targetTime) {
        int speed = configManager.getGradualSkipSpeedTicks();

        ScheduledTask gradualTask = SchedulerAdapter.runTaskTimer(plugin, world, () -> {
            long currentTime = world.getTime();
            long newTime = currentTime + speed;

            if (newTime >= targetTime && currentTime < targetTime) {
                world.setTime(targetTime);
            } else if (newTime > 24000) {
                world.setTime(targetTime);
            } else {
                world.setTime(newTime);
            }
        }, 1L, 1L);

        // Cancel after a reasonable time (20 seconds max)
        SchedulerAdapter.runTaskLater(plugin, world, gradualTask::cancel, 400L);
    }

    /**
     * Updates boss bar progress and action bar messages for sleeping players.
     */
    public void updateSleepStatus(World world) {
        if (!skipTasks.containsKey(world)) return;

        int total = getRequiredSleepingCount(world);
        int current = getSleepingCount(world);

        // Update boss bar progress
        BossBar bossBar = worldBossBars.get(world);
        if (bossBar != null) {
            double progress = total > 0 ? Math.min(1.0, (double) current / total) : 1.0;
            bossBar.setProgress(progress);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", getSleepingPlayerName(world));
            bossBar.setTitle(configManager.getMessage("boss-bar.title", placeholders));
        }

        // Send action bar and progress bar to sleeping players
        if (configManager.isShowProgressBar() || configManager.isShowActionBar()) {
            String bar = "";
            if (configManager.isShowProgressBar()) {
                bar = configManager.buildProgressBar(current, total);
            }

            String actionMsg = configManager.getMessage("sleep.progress-bar", Map.of(
                "bar", bar,
                "count", String.valueOf(current),
                "required", String.valueOf(total)
            ));

            for (Player player : world.getPlayers()) {
                if (isPlayerSleeping(player)) {
                    if (configManager.isShowActionBar()) {
                        try {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionMsg));
                        } catch (NoSuchMethodError | NoClassDefFoundError e) {
                            // Fallback for servers without Spigot action bar API
                            player.sendMessage(actionMsg);
                        }
                    }
                }
            }
        }
    }

    /**
     * Plays sounds when night is skipped.
     */
    private void playSkipSound(World world) {
        try {
            Sound sound = Sound.valueOf(configManager.getSkipSound());
            float volume = configManager.getSkipSoundVolume();
            float pitch = configManager.getSkipSoundPitch();

            for (Player player : world.getPlayers()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + configManager.getSkipSound());
        }
    }

    /**
     * Shows title when night is skipped.
     */
    private void showSkipTitle(World world, String playerName) {
        String title = ChatColor.translateAlternateColorCodes('&',
            configManager.getTitleMessage().replace("%player%", playerName));
        String subtitle = ChatColor.translateAlternateColorCodes('&',
            configManager.getSubtitleMessage().replace("%player%", playerName));

        for (Player player : world.getPlayers()) {
            player.sendTitle(title, subtitle, configManager.getTitleFadeIn(),
                configManager.getTitleStay(), configManager.getTitleFadeOut());
        }
    }

    /**
     * Gets the set of currently sleeping player UUIDs in a world (public, for PAPI).
     */
    public Set<UUID> getSleepingPlayers(World world) {
        return sleepingPlayers.get(world);
    }

    /**
     * Gets the name of a sleeping player in the world.
     */
    private String getSleepingPlayerName(World world) {
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null && !players.isEmpty()) {
            Player p = Bukkit.getPlayer(players.iterator().next());
            if (p != null) return p.getDisplayName();
        }
        return "Unknown";
    }

    // --- Player counting ---

    /**
     * Gets the number of players needed to sleep to skip the night.
     */
    public int getRequiredSleepingCount(World world) {
        // If require-all-players-online is enabled, check that no eligible players are offline
        // Uses cached offline player count from OfflinePlayerTracker to avoid expensive I/O
        if (configManager.isRequireAllPlayersOnline()) {
            if (OfflinePlayerTracker.hasOfflinePlayers()) {
                return Integer.MAX_VALUE;
            }
        }

        int total = getTotalPlayerCount(world);

        // Apply percentage
        int percentage = configManager.getSleepPercentage();
        if (percentage <= 0) return 1; // One player sleep
        if (percentage >= 100) return Math.max(1, total); // All players

        return Math.max(1, (int) Math.ceil(total * percentage / 100.0));
    }

    /**
     * Gets the number of currently sleeping (counted) players in a world.
     * If {@code count-afk-as-sleeping} is enabled, AFK players are also counted.
     */
    public int getSleepingCount(World world) {
        Set<UUID> sleeping = sleepingPlayers.get(world);

        int count = 0;

        // Count actual players in bed
        if (sleeping != null) {
            for (UUID uuid : sleeping) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline() && player.isSleeping()) {
                    count++;
                }
            }
        }

        // If enabled, also count AFK players as sleeping (they count toward the threshold)
        if (configManager.isCountAfkAsSleeping()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (configManager.isPerWorldSleep() && !player.getWorld().equals(world)) continue;
                if (player.hasPermission("onlysleep.exempt")) continue;
                if (isAfk(player) && !player.isSleeping()) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Gets the total number of eligible players in a world.
     *
     * <p><b>Folia-safe:</b> Uses a snapshot of online players taken at the time
     * of the call. If called from a region thread, the player list may be slightly
     * stale, but this is fine for count purposes.</p>
     */
    public int getTotalPlayerCount(World world) {
        // Snapshot online players to avoid concurrent modification on Folia
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        int count = 0;
        for (Player player : onlinePlayers) {
            // Per-world check
            if (configManager.isPerWorldSleep() && !player.getWorld().equals(world)) continue;

            // Permission exemptions
            if (player.hasPermission("onlysleep.exempt")) continue;

            // Game mode checks
            if (player.getGameMode() == GameMode.SPECTATOR && !configManager.isCountSpectators()) continue;
            if (player.getGameMode() == GameMode.CREATIVE && configManager.isIgnoreCreativeMode()) continue;

            // Flying check
            if (player.isFlying() && !configManager.isCountFlying()) continue;

            // AFK check
            if (configManager.isExcludeAfkFromTotal() && isAfk(player)) continue;

            count++;
        }
        return count;
    }

    /**
     * Checks if a player is AFK using various detection methods.
     */
    private boolean isAfk(Player player) {
        // Check built-in AFK tracker (runs first — most reliable for our purposes)
        if (AfkTracker.isAfk(player)) {
            return true;
        }

        // Check EssentialsX AFK
        if (configManager.isUseEssentialsAfk()) {
            if (player.hasMetadata("afk") && !player.getMetadata("afk").isEmpty()
                    && player.getMetadata("afk").get(0).asBoolean()) {
                return true;
            }
        }

        // Check CMI AFK
        if (configManager.isUseCmiAfk()) {
            try {
                Object cmiPlayer = player.getClass().getMethod("getCMIPlayer").invoke(player);
                if (cmiPlayer != null) {
                    Object data = cmiPlayer.getClass().getMethod("getAfkData").invoke(cmiPlayer);
                    if (data != null) {
                        boolean isAfk = (boolean) data.getClass().getMethod("isAfk").invoke(data);
                        if (isAfk) return true;
                    }
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    // --- Boss bar ---

    private void showBossBarForWorld(World world) {
        removeBossBar(world);

        BossBar bossBar = Bukkit.createBossBar(
            configManager.getMessage("boss-bar.title", Map.of("player", getSleepingPlayerName(world))),
            configManager.getBossBarColor(),
            configManager.getBossBarStyle()
        );

        for (Player player : world.getPlayers()) {
            if (player.hasPermission("onlysleep.exempt")) continue;
            bossBar.addPlayer(player);
        }

        worldBossBars.put(world, bossBar);

        // Update boss bar periodically using the scheduler adapter
        ScheduledTask bossTask = SchedulerAdapter.runTaskTimer(plugin, world, () -> {
            if (!skipTasks.containsKey(world)) {
                removeBossBar(world);
                return;
            }
            updateSleepStatus(world);
        }, 0L, 10L);

        bossBarTasks.put(world, bossTask);
    }

    private void removeBossBar(World world) {
        BossBar bar = worldBossBars.remove(world);
        if (bar != null) bar.removeAll();

        ScheduledTask task = bossBarTasks.remove(world);
        if (task != null) task.cancel();
    }

    // --- Shutdown ---

    public void shutdown() {
        // Cancel all tasks
        skipTasks.values().forEach(ScheduledTask::cancel);
        skipTasks.clear();

        bossBarTasks.values().forEach(ScheduledTask::cancel);
        bossBarTasks.clear();

        // Remove all boss bars
        worldBossBars.values().forEach(BossBar::removeAll);
        worldBossBars.clear();

        sleepingPlayers.clear();
    }
}

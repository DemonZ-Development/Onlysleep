package com.demonzdevelopment.onlysleep.api;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;

/**
 * Public Developer API for Onlysleep.
 * <p>
 * Provides static access to core sleep logic, config and world state.
 * All methods delegate to the running {@link Onlysleep} instance.
 * If Onlysleep is not loaded, methods will throw {@link IllegalStateException}.
 * <p>
 * Example:
 * <pre>{@code
 * OnlysleepAPI.setSleepPercentage(0);
 * int required = OnlysleepAPI.getRequiredSleepingCount(world);
 * }</pre>
 * <p>
 * Also see {@code wiki/Developer-API.md} and {@code com.demonzdevelopment.onlysleep.api.events} for custom Bukkit events.
 */
public final class OnlysleepAPI {

    private OnlysleepAPI() {}

    /**
     * Get the main plugin instance. Null if not enabled.
     */
    public static Onlysleep getInstance() {
        return Onlysleep.getInstance();
    }

    private static Onlysleep requireInstance() {
        Onlysleep instance = Onlysleep.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Onlysleep is not enabled - API not available");
        }
        return instance;
    }

    public static SleepManager getSleepManager() {
        return requireInstance().getSleepManager();
    }

    public static ConfigManager getConfigManager() {
        return requireInstance().getConfigManager();
    }

    /** @return sleep percentage 0-100 (0 = one player) */
    public static int getSleepPercentage() {
        return getConfigManager().getSleepPercentage();
    }

    /**
     * Set sleep percentage 0-100 and persist to config.yml.
     * @return true if applied, false if out of range
     */
    public static boolean setSleepPercentage(int percentage) {
        return getConfigManager().setSleepPercentage(percentage);
    }

    /** @return skip type: instant, gradual or speed */
    public static String getSkipType() {
        return getConfigManager().getSkipType();
    }

    public static boolean setSkipType(String type) {
        return getConfigManager().setSkipType(type);
    }

    public static boolean isPerWorldSleep() {
        return getConfigManager().isPerWorldSleep();
    }

    public static void setPerWorldSleep(boolean value) {
        getConfigManager().setPerWorldSleep(value);
    }

    public static int getRequiredSleepingCount(World world) {
        return getSleepManager().getRequiredSleepingCount(world);
    }

    public static int getSleepingCount(World world) {
        return getSleepManager().getSleepingCount(world);
    }

    public static int getTotalPlayerCount(World world) {
        return getSleepManager().getTotalPlayerCount(world);
    }

    public static boolean isPlayerSleeping(Player player) {
        return getSleepManager().isPlayerSleeping(player);
    }

    public static boolean isSkipScheduled(World world) {
        return getSleepManager().isSkipScheduled(world);
    }

    public static Set<UUID> getSleepingPlayers(World world) {
        return getSleepManager().getSleepingPlayers(world);
    }

    public static boolean isWorldEnabled(String worldName) {
        return getConfigManager().isWorldEnabled(worldName);
    }

    public static boolean isWorldEnabled(World world) {
        return isWorldEnabled(world.getName());
    }

    /**
     * Force a night skip for a world using current config's skip logic.
     * Fires {@link com.demonzdevelopment.onlysleep.api.events.NightSkipEvent} (cancellable).
     */
    public static void forceSkipNight(World world) {
        getSleepManager().forceSkipNight(world);
    }

    /**
     * Get a formatted message from messages.yml with placeholders.
     * Includes prefix and color codes translated (& -> §).
     * For Discord, use {@link #stripColor(String)} or {@link #getDiscordMessage(String, java.util.Map)}.
     * @param path e.g. "sleep.start-sleep", "sleep.enough-sleeping", "sleep.cancelled", "weather.clearing", "boss-bar.title"
     */
    public static String getMessage(String path, java.util.Map<String, String> placeholders) {
        return getConfigManager().getMessage(path, placeholders);
    }

    public static String getMessage(String path) {
        return getConfigManager().getMessage(path);
    }

    public static String getRawMessage(String path, java.util.Map<String, String> placeholders) {
        return getConfigManager().getRawMessage(path, placeholders);
    }

    public static String getRawMessage(String path) {
        return getConfigManager().getRawMessage(path);
    }

    /** Strip Minecraft color codes (§/&). Use for Discord. */
    public static String stripColor(String minecraftMessage) {
        if (minecraftMessage == null) return null;
        String translated = org.bukkit.ChatColor.translateAlternateColorCodes('&', minecraftMessage);
        return org.bukkit.ChatColor.stripColor(translated);
    }

    /** Get message ready for Discord (prefix included, colors stripped). */
    public static String getDiscordMessage(String path, java.util.Map<String, String> placeholders) {
        return stripColor(getMessage(path, placeholders));
    }

    /** Helper: build sleep.start-sleep Discord message for a player. */
    public static String getSleepStartDiscordMessage(org.bukkit.entity.Player player) {
        World w = player.getWorld();
        java.util.Map<String,String> ph = new java.util.HashMap<>();
        ph.put("player", player.getName());
        ph.put("count", String.valueOf(getSleepingCount(w)));
        ph.put("required", String.valueOf(getRequiredSleepingCount(w)));
        return getDiscordMessage("sleep.start-sleep", ph);
    }

    /** Helper: build sleep.enough-sleeping Discord message. */
    public static String getEnoughSleepingDiscordMessage(String initiatorName) {
        return getDiscordMessage("sleep.enough-sleeping", java.util.Map.of("player", initiatorName));
    }

    /** Helper: build sleep.cancelled Discord message. */
    public static String getCancelledDiscordMessage(String playerName) {
        return getDiscordMessage("sleep.cancelled", java.util.Map.of("player", playerName));
    }

    /**
     * Check if this server has Onlysleep installed and enabled.
     */
    public static boolean isAvailable() {
        return Onlysleep.getInstance() != null;
    }

    /**
     * Utility to soft-depend: call from your plugin's onEnable().
     * Example: {@code OnlysleepAPI.hook(this, plugin -> getLogger().info("Hooked Onlysleep")); }
     */
    public static void hook(JavaPlugin requester, java.util.function.Consumer<Onlysleep> onHook) {
        if (isAvailable()) {
            onHook.accept(requireInstance());
            requester.getLogger().info("Hooked into Onlysleep v" + requireInstance().getDescription().getVersion());
        }
    }
}

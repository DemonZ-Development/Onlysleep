package com.demonzdevelopment.onlysleep.api;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
















public final class OnlysleepAPI {

    private OnlysleepAPI() {}




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


    public static int getSleepPercentage() {
        return getConfigManager().getSleepPercentage();
    }





    public static boolean setSleepPercentage(int percentage) {
        return getConfigManager().setSleepPercentage(percentage);
    }


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





    public static void forceSkipNight(World world) {
        getSleepManager().forceSkipNight(world);
    }







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


    public static String stripColor(String minecraftMessage) {
        if (minecraftMessage == null) return null;
        String translated = org.bukkit.ChatColor.translateAlternateColorCodes('&', minecraftMessage);
        return org.bukkit.ChatColor.stripColor(translated);
    }


    public static String getDiscordMessage(String path, java.util.Map<String, String> placeholders) {
        return stripColor(getMessage(path, placeholders));
    }


    public static String getSleepStartDiscordMessage(org.bukkit.entity.Player player) {
        World w = player.getWorld();
        java.util.Map<String,String> ph = new java.util.HashMap<>();
        ph.put("player", player.getName());
        ph.put("count", String.valueOf(getSleepingCount(w)));
        ph.put("required", String.valueOf(getRequiredSleepingCount(w)));
        return getDiscordMessage("sleep.start-sleep", ph);
    }


    public static String getEnoughSleepingDiscordMessage(String initiatorName) {
        return getDiscordMessage("sleep.enough-sleeping", java.util.Map.of("player", initiatorName));
    }


    public static String getCancelledDiscordMessage(String playerName) {
        return getDiscordMessage("sleep.cancelled", java.util.Map.of("player", playerName));
    }




    public static boolean isAvailable() {
        return Onlysleep.getInstance() != null;
    }





    public static void hook(JavaPlugin requester, java.util.function.Consumer<Onlysleep> onHook) {
        if (isAvailable()) {
            onHook.accept(requireInstance());
            requester.getLogger().info("Hooked into Onlysleep v" + requireInstance().getDescription().getVersion());
        }
    }
}

package com.demonzdevelopment.onlysleep.fabric.api;

import com.demonzdevelopment.onlysleep.fabric.OnlysleepMod;
import com.demonzdevelopment.onlysleep.fabric.config.FabricConfigManager;
import com.demonzdevelopment.onlysleep.fabric.SleepManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
import java.util.UUID;




public final class OnlysleepFabricAPI {

    private OnlysleepFabricAPI() {}

    public static OnlysleepMod getInstance() { return OnlysleepMod.getInstance(); }

    private static OnlysleepMod require() {
        OnlysleepMod m = OnlysleepMod.getInstance();
        if (m == null) throw new IllegalStateException("Onlysleep Fabric not initialized");
        return m;
    }

    public static SleepManager getSleepManager() { return require().sleepManager(); }
    public static FabricConfigManager getConfigManager() { return require().config(); }

    public static int getSleepPercentage() { return getConfigManager().getSleepPercentage(); }
    public static boolean setSleepPercentage(int pct) { return getConfigManager().setSleepPercentage(pct); }
    public static String getSkipType() { return getConfigManager().getSkipType(); }
    public static boolean setSkipType(String type) { return getConfigManager().setSkipType(type); }
    public static boolean isPerWorldSleep() { return getConfigManager().isPerWorldSleep(); }
    public static void setPerWorldSleep(boolean v) { getConfigManager().setPerWorldSleep(v); }

    public static int getRequiredSleepingCount(ServerLevel level) { return getSleepManager().getRequiredSleepingCount(level); }
    public static int getSleepingCount(ServerLevel level) { return getSleepManager().getSleepingCount(level); }
    public static int getTotalPlayerCount(ServerLevel level) { return getSleepManager().getTotalPlayerCount(level); }
    public static boolean isPlayerSleeping(ServerPlayer player) { return getSleepManager().isPlayerSleeping(player); }
    public static boolean isSkipScheduled(ServerLevel level) { return getSleepManager().isSkipScheduled(level); }
    public static Set<UUID> getSleepingPlayers(ServerLevel level) { return getSleepManager().getSleepingPlayers(level); }
    public static boolean isWorldEnabled(String worldKey) { return getConfigManager().isWorldEnabled(worldKey); }
    public static boolean isWorldEnabled(ServerLevel level) { return isWorldEnabled(SleepManager.worldKey(level)); }

    public static void forceSkipNight(ServerLevel level) { getSleepManager().forceSkipNight(level); }

    public static boolean isAvailable() { return OnlysleepMod.getInstance() != null; }

    public static String stripColor(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("(?i)§[0-9a-fk-or]", "").replaceAll("(?i)&[0-9a-fk-or]", "");
    }

    public static String getDiscordMessage(String path, java.util.Map<String,String> ph) {
        try {
            net.minecraft.network.chat.MutableComponent comp = getConfigManager().getMessage(path, ph);
            String raw = comp.getString();
            return stripColor(raw);
        } catch (Exception e) { return path; }
    }

    public static String getSleepStartDiscordMessage(ServerPlayer player) {
        ServerLevel lvl = player.level();
        java.util.Map<String,String> ph = new java.util.HashMap<>();
        ph.put("player", player.getName().getString());
        ph.put("count", String.valueOf(getSleepingCount(lvl)));
        ph.put("required", String.valueOf(getRequiredSleepingCount(lvl)));
        return getDiscordMessage("sleep.start-sleep", ph);
    }
}

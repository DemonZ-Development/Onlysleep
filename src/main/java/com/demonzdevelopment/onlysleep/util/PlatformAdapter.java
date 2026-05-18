package com.demonzdevelopment.onlysleep.util;

import org.bukkit.Bukkit;

/**
 * Detects the current server platform and provides platform-specific functionality.
 * Supports: Folia, Paper, Spigot, Bukkit (and derivatives like Purpur, Pufferfish, etc.)
 */
public final class PlatformAdapter {

    private static ServerPlatform platform = null;
    private static Boolean folia = null;
    private static Boolean paper = null;

    private PlatformAdapter() {}

    /**
     * Detects and returns the current server platform.
     */
    public static ServerPlatform getPlatform() {
        if (platform == null) {
            detect();
        }
        return platform;
    }

    /**
     * Checks if the server is running Folia (regionized threaded server).
     */
    public static boolean isFolia() {
        if (folia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                folia = true;
            } catch (ClassNotFoundException e) {
                folia = false;
            }
        }
        return folia;
    }

    /**
     * Checks if the server is running Paper or a Paper fork (e.g., Purpur, Pufferfish).
     */
    public static boolean isPaper() {
        if (paper == null) {
            try {
                Class.forName("com.destroystokyo.paper.ParticleBuilder");
                paper = true;
            } catch (ClassNotFoundException e) {
                paper = false;
            }
        }
        return paper;
    }

    /**
     * Checks if the server is running Spigot (not Paper).
     */
    public static boolean isSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return !isPaper();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Gets the server software name.
     */
    public static String getServerSoftware() {
        return Bukkit.getVersion();
    }

    /**
     * Gets the Bukkit API version.
     */
    public static String getAPIVersion() {
        return Bukkit.getBukkitVersion();
    }

    /**
     * Gets the Minecraft version (e.g., "1.21.4").
     */
    public static String getMinecraftVersion() {
        String version = Bukkit.getBukkitVersion();
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        return version;
    }

    /**
     * Gets the major Minecraft version number (e.g., 21 for 1.21.4).
     */
    public static int getMinecraftMajorVersion() {
        try {
            String full = getMinecraftVersion();
            // Format: 1.XX or 1.XX.Y
            String[] parts = full.split("\\.");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private static void detect() {
        if (isFolia()) {
            platform = ServerPlatform.FOLIA;
        } else if (isPaper()) {
            platform = ServerPlatform.PAPER;
        } else if (isSpigot()) {
            platform = ServerPlatform.SPIGOT;
        } else {
            platform = ServerPlatform.BUKKIT;
        }
    }

    public enum ServerPlatform {
        FOLIA("Folia"),
        PAPER("Paper"),
        SPIGOT("Spigot"),
        BUKKIT("Bukkit");

        private final String displayName;

        ServerPlatform(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}

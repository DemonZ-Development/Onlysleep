package com.demonzdevelopment.onlysleep.util;

import org.bukkit.Bukkit;

public final class PlatformAdapter {

    private static ServerPlatform platform = null;
    private static Boolean folia = null;
    private static Boolean paper = null;

    private PlatformAdapter() {}

    public static ServerPlatform getPlatform() {
        if (platform == null) {
            detect();
        }
        return platform;
    }

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

    public static boolean isSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return !isPaper();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String getMinecraftVersion() {
        String version = Bukkit.getBukkitVersion();
        if (version.contains("-")) {
            version = version.substring(0, version.indexOf('-'));
        }
        return version;
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

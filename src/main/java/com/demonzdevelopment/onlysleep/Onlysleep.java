package com.demonzdevelopment.onlysleep;

import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.listener.SleepListener;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import com.demonzdevelopment.onlysleep.command.OnlysleepCommand;
import com.demonzdevelopment.onlysleep.util.AfkTracker;
import com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;
import com.demonzdevelopment.onlysleep.util.SleepPlaceholderExpansion;
import com.demonzdevelopment.onlysleep.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Onlysleep extends JavaPlugin {

    private static Onlysleep instance;
    private ConfigManager configManager;
    private SleepManager sleepManager;
    private UpdateChecker updateChecker;
    private PlatformAdapter.ServerPlatform platform;

    @Override
    public void onEnable() {
        instance = this;

        // Detect server platform
        this.platform = PlatformAdapter.getPlatform();
        getLogger().info("Detected platform: " + platform.getDisplayName());

        // Initialize configuration
        this.configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize sleep manager (uses scheduler adapter for Folia compatibility)
        this.sleepManager = new SleepManager(this, configManager);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new SleepListener(this, sleepManager, configManager), this);

        // Register command
        OnlysleepCommand commandExecutor = new OnlysleepCommand(this, configManager);
        getCommand("onlysleep").setExecutor(commandExecutor);
        getCommand("onlysleep").setTabCompleter(commandExecutor);

        // Initialize bStats metrics
        initializeMetrics();

        // Initialize update checker
        if (configManager.isCheckForUpdates()) {
            this.updateChecker = new UpdateChecker(this);
            checkForUpdates();
        }

        // Register PlaceholderAPI expansion (if PAPI is installed)
        registerPlaceholderExpansion();

        // Initialise built-in AFK tracker
        if (configManager.getAfkTimeSeconds() > 0) {
            AfkTracker.init(this);
            getLogger().info("AFK tracker initialised (" + configManager.getAfkTimeSeconds() + "s timeout)");
        }

        // Initialise offline player tracker (for require-all-players-online feature)
        if (configManager.isRequireAllPlayersOnline()) {
            OfflinePlayerTracker.init(this);
            getLogger().info("Offline player tracker initialised for require-all-players-online");
        }

        getLogger().info("Onlysleep v" + getDescription().getVersion() + " by Demonz Development enabled!");
        getLogger().info("Running on " + platform.getDisplayName() + " " + PlatformAdapter.getMinecraftVersion());
    }

    @Override
    public void onDisable() {
        if (sleepManager != null) {
            sleepManager.shutdown();
        }
        OfflinePlayerTracker.shutdown();
        AfkTracker.shutdown();
        getLogger().info("Onlysleep v" + getDescription().getVersion() + " disabled!");
    }

    private void initializeMetrics() {
        try {
            Metrics metrics = new Metrics(this, 31415);

            // Custom charts
            metrics.addCustomChart(new SimplePie("server_platform", () -> platform.getDisplayName()));
            metrics.addCustomChart(new SimplePie("sleep_percentage", () -> String.valueOf(configManager.getSleepPercentage())));
            metrics.addCustomChart(new SimplePie("per_world_sleep", () -> String.valueOf(configManager.isPerWorldSleep())));
            metrics.addCustomChart(new SimplePie("boss_bar_enabled", () -> String.valueOf(configManager.isShowBossBar())));
            metrics.addCustomChart(new SimplePie("clear_weather", () -> String.valueOf(configManager.isClearWeather())));

            getLogger().info("bStats metrics initialized (ID: 31415)");
        } catch (Exception e) {
            getLogger().warning("Failed to initialize bStats: " + e.getMessage());
        }
    }

    /**
     * Registers the PlaceholderAPI expansion if PlaceholderAPI is installed.
     */
    private void registerPlaceholderExpansion() {
        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new SleepPlaceholderExpansion(this).register();
                getLogger().info("PlaceholderAPI expansion registered!");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
        }
    }

    private void checkForUpdates() {
        if (updateChecker == null) return;

        updateChecker.checkAsync().thenAccept(result -> {
            if (result.isUpdateAvailable()) {
                getLogger().info("Update available: " + result.getLatestVersion() +
                    " (Current: " + getDescription().getVersion() + ")");
                getLogger().info("Download at: https://modrinth.com/plugin/onlysleep");

                // Notify online ops
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("new", result.getLatestVersion());
                placeholders.put("current", getDescription().getVersion());

                String msg = configManager.getMessage("update.available", placeholders);
                Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("onlysleep.update"))
                    .forEach(p -> p.sendMessage(msg));
            } else {
                getLogger().info(result.getMessage());
            }
        }).exceptionally(throwable -> {
            getLogger().warning("Update check failed: " + throwable.getMessage());
            return null;
        });
    }

    public static Onlysleep getInstance() {
        return instance;
    }

    public SleepManager getSleepManager() {
        return sleepManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlatformAdapter.ServerPlatform getPlatform() {
        return platform;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}

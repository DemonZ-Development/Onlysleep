package com.demonzdevelopment.onlysleep;

import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.listener.SleepListener;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import com.demonzdevelopment.onlysleep.command.OnlysleepCommand;
import com.demonzdevelopment.onlysleep.util.AfkTracker;
import com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter;
import com.demonzdevelopment.onlysleep.util.SleepPlaceholderExpansion;
import com.demonzdevelopment.onlysleep.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class Onlysleep extends JavaPlugin {

    private static Onlysleep instance;
    private ConfigManager configManager;
    private SleepManager sleepManager;
    private UpdateChecker updateChecker;
    private SchedulerAdapter.ScheduledTask updateCheckerTask;
    private PlatformAdapter.ServerPlatform platform;

    @Override
    public void onEnable() {
        instance = this;

        this.platform = PlatformAdapter.getPlatform();
        getLogger().info("Detected platform: " + platform.getDisplayName());

        this.configManager = new ConfigManager(this);
        configManager.loadConfigs();

        this.sleepManager = new SleepManager(this, configManager);

        getServer().getPluginManager().registerEvents(new SleepListener(this, sleepManager, configManager), this);

        registerWorldLifecycleListener();

        sleepManager.applyGamerules();

        OnlysleepCommand commandExecutor = new OnlysleepCommand(this, configManager);
        getCommand("onlysleep").setExecutor(commandExecutor);
        getCommand("onlysleep").setTabCompleter(commandExecutor);

        initializeMetrics();

        this.updateChecker = new UpdateChecker(this);
        if (configManager.isCheckForUpdates()) {
            checkForUpdates();

            this.updateCheckerTask = SchedulerAdapter.runGlobalTaskTimer(this, this::checkForUpdates, 288000L, 288000L);
        }

        registerPlaceholderExpansion();

        if (configManager.getAfkTimeSeconds() > 0) {
            AfkTracker.init(this);
            getLogger().info("AFK tracker initialised (" + configManager.getAfkTimeSeconds() + "s timeout)");
        }

        if (configManager.isRequireAllPlayersOnline()) {
            OfflinePlayerTracker.init(this);
            getLogger().info("Offline player tracker initialised for require-all-players-online");
        }

        getLogger().info("Onlysleep v" + getDescription().getVersion() + " by Demonz Development enabled!");
        getLogger().info("Running on " + platform.getDisplayName() + " " + PlatformAdapter.getMinecraftVersion());
    }

    @Override
    public void onDisable() {
        if (updateCheckerTask != null) {
            updateCheckerTask.cancel();
            updateCheckerTask = null;
        }
        if (sleepManager != null) {
            sleepManager.restoreGamerules();
            sleepManager.shutdown();
        }
        OfflinePlayerTracker.shutdown();
        AfkTracker.shutdown();
        instance = null;
        getLogger().info("Onlysleep v" + getDescription().getVersion() + " disabled!");
    }

    private void initializeMetrics() {
        try {
            Metrics metrics = new Metrics(this, 31415);

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

    private void registerWorldLifecycleListener() {
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onWorldLoad(WorldLoadEvent event) {
                if (sleepManager != null) {
                    sleepManager.applyGamerule(event.getWorld());
                }
            }

            @EventHandler
            public void onWorldUnload(WorldUnloadEvent event) {
                if (sleepManager != null) {
                    sleepManager.cleanupWorld(event.getWorld());
                }
            }
        }, this);
    }

    private void checkForUpdates() {
        if (updateChecker == null) return;

        updateChecker.checkAsync().thenAccept(result -> {
            if (result.isUpdateAvailable()) {
                getLogger().info("Update available: " + result.getLatestVersion() +
                    " (Current: " + getDescription().getVersion() + ")");
                getLogger().info("Download at: https://modrinth.com/plugin/onlysleep");
                getLogger().info("GitHub: https://github.com/DemonZ-Development/Onlysleep");

                final String finalNew = result.getLatestVersion();
                SchedulerAdapter.runGlobalTask(this, () -> {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("new", finalNew);
                    placeholders.put("current", getDescription().getVersion());

                    String msg = configManager.getMessage("update.available", placeholders);
                    String links = configManager.getMessage("update.available-links");
                    Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("onlysleep.update"))
                        .forEach(p -> {
                            p.sendMessage(msg);
                            p.sendMessage(links);
                        });
                });
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

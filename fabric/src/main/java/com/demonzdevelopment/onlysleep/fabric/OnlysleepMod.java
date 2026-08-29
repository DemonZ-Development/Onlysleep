package com.demonzdevelopment.onlysleep.fabric;

import com.demonzdevelopment.onlysleep.fabric.config.FabricConfigManager;
import com.demonzdevelopment.onlysleep.fabric.scheduler.TaskScheduler;
import com.demonzdevelopment.onlysleep.fabric.tracker.AfkTracker;
import com.demonzdevelopment.onlysleep.fabric.tracker.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.fabric.util.UpdateChecker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class OnlysleepMod implements ModInitializer {

    public static final String MOD_ID = "onlysleep";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static OnlysleepMod instance;

    private final FabricConfigManager configManager =
        new FabricConfigManager(configDir());

    private final TaskScheduler scheduler = new TaskScheduler();
    private final PermissionHandler permissions = new PermissionHandler(this);
    private final SleepManager sleepManager = new SleepManager(this);
    private final SleepListener listener = new SleepListener(this, sleepManager);

    private UpdateChecker updateChecker;
    private TaskScheduler.Task updateCheckerTask;
    private long tickCounter = 0;

    private volatile MinecraftServer server;

    public static OnlysleepMod getInstance() { return instance; }

    @Override
    public void onInitialize() {
        instance = this;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            LOGGER.info("Onlysleep is server-side only, skipping client init");
            return;
        }

        configManager.load();
        LOGGER.info("Onlysleep {} for Fabric loaded, config ready", version());

        listener.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            scheduler.tickServer();
            AfkTracker.sample(server, tickCounter);
            OfflinePlayerTracker.tick(tickCounter);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
            sleepManager.applyGamerules(server);

            if (configManager.getAfkTimeSeconds() > 0) {
                AfkTracker.init(configManager.getAfkTimeSeconds());
                LOGGER.info("AFK tracker initialised ({}s timeout)", configManager.getAfkTimeSeconds());
            }
            if (configManager.isRequireAllPlayersOnline()) {
                OfflinePlayerTracker.init(server);
                LOGGER.info("Offline player tracker initialised for require-all-players-online");
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> shutdown());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            new OnlysleepCommands(this).register(dispatcher));

        if (configManager.isCheckForUpdates()) {
            updateChecker = new UpdateChecker(version());
            checkForUpdates();

            updateCheckerTask = scheduler.runTimer(288000L, 288000L, this::checkForUpdates);
        }

        LOGGER.info("Onlysleep v{} by Demonz Development initialised", version());
    }

    private void shutdown() {
        if (updateCheckerTask != null) {
            updateCheckerTask.cancel();
            updateCheckerTask = null;
        }
        sleepManager.restoreGamerules();
        sleepManager.shutdown();
        OfflinePlayerTracker.shutdown();
        AfkTracker.shutdown();
        scheduler.cancelAll();
        server = null;
        LOGGER.info("Onlysleep v{} disabled", version());
    }

    private void checkForUpdates() {
        if (updateChecker == null) return;

        updateChecker.checkAsync().thenAccept(result -> {
            if (result.updateAvailable()) {
                LOGGER.info("Update available: {} (Current: {})", result.latestVersion(), version());
                LOGGER.info("Download at: https://modrinth.com/mod/onlysleep");

                String latest = result.latestVersion();
                scheduler.runSync(() -> {
                    MinecraftServer srv = server;
                    if (srv == null) return;
                    srv.getPlayerList().getPlayers().stream()
                        .filter(p -> permissions.shouldNotifyUpdates(p))
                        .forEach(p -> {
                            java.util.Map<String, String> ph = new java.util.HashMap<>();
                            ph.put("new", latest);
                            ph.put("current", version());
                            p.sendSystemMessage(
                                configManager.getMessage("update.available", ph));
                        });
                });
            } else {
                LOGGER.info(result.message());
            }
        }).exceptionally(throwable -> {
            LOGGER.warn("Update check failed: {}", throwable.getMessage());
            return null;
        });
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    }

    public String version() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(c -> c.getMetadata().getVersion().toString())
            .orElse("?.?.?");
    }

    public String minecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft")
            .map(c -> c.getMetadata().getVersion().toString())
            .orElse("unknown");
    }

    public Logger logger() {
        return LOGGER;
    }

    public FabricConfigManager config() {
        return configManager;
    }

    public TaskScheduler scheduler() {
        return scheduler;
    }

    public PermissionHandler permissions() {
        return permissions;
    }

    public SleepManager sleepManager() {
        return sleepManager;
    }

    public UpdateChecker updateChecker() {
        return updateChecker;
    }

    public MinecraftServer server() {
        return server;
    }
}

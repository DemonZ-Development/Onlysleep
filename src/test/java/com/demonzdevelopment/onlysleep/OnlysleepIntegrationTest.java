package com.demonzdevelopment.onlysleep;

import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import com.demonzdevelopment.onlysleep.util.AfkTracker;
import com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration-style tests using pure Mockito, verifying that the plugin's
 * components work together correctly without needing a real server.
 *
 * <p>These tests verify end-to-end flows (e.g., player enters bed → count
 * increases → skip scheduled) using mocked Bukkit APIs.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OnlysleepIntegrationTest {

    @Mock
    private Server server;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private BukkitScheduler scheduler;

    private Onlysleep plugin;
    private ConfigManager configManager;
    private SleepManager sleepManager;
    private World world;
    private Player player;
    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        plugin = mock(Onlysleep.class);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("Test"));

        configManager = mock(ConfigManager.class);
        when(plugin.getConfigManager()).thenReturn(configManager);

        // Default config stubs
        lenient().when(configManager.isWorldEnabled(anyString())).thenReturn(true);
        lenient().when(configManager.isPerWorldSleep()).thenReturn(true);
        lenient().when(configManager.getSleepPercentage()).thenReturn(50);
        lenient().when(configManager.isRequireAllPlayersOnline()).thenReturn(false);
        lenient().when(configManager.isCountAfkAsSleeping()).thenReturn(false);
        lenient().when(configManager.isExcludeAfkFromTotal()).thenReturn(true);
        lenient().when(configManager.isCountSpectators()).thenReturn(false);
        lenient().when(configManager.isCountFlying()).thenReturn(true);
        lenient().when(configManager.isIgnoreCreativeMode()).thenReturn(false);
        lenient().when(configManager.getSkipDelayTicks()).thenReturn(60);
        lenient().when(configManager.getMessage(anyString())).thenReturn("test message");
        lenient().when(configManager.getMessage(anyString(), anyMap())).thenReturn("test message");

        sleepManager = new SleepManager(plugin, configManager);

        world = mock(World.class);
        lenient().when(world.getName()).thenReturn("world");

        playerUuid = UUID.randomUUID();
        player = mock(Player.class);
        lenient().when(player.getUniqueId()).thenReturn(playerUuid);
        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.getDisplayName()).thenReturn("TestPlayer");
        lenient().when(player.getName()).thenReturn("TestPlayer");
        lenient().when(player.isOnline()).thenReturn(true);
        lenient().when(player.isSleeping()).thenReturn(true);
        lenient().when(player.getGameMode()).thenReturn(org.bukkit.GameMode.SURVIVAL);

        // Mock scheduler for SchedulerAdapter
        lenient().when(scheduler.runTask(any(), any(Runnable.class))).thenReturn(mock(BukkitTask.class));
        lenient().when(scheduler.runTaskLater(any(), any(Runnable.class), anyLong())).thenReturn(mock(BukkitTask.class));
        lenient().when(scheduler.runTaskTimer(any(), any(Runnable.class), anyLong(), anyLong())).thenReturn(mock(BukkitTask.class));
    }

    @AfterEach
    void tearDown() {
        sleepManager.shutdown();
        AfkTracker.shutdown();
        OfflinePlayerTracker.shutdown();
    }

    // ========== Plugin lifecycle ==========

    @Test
    void configManager_Available() {
        assertNotNull(configManager);
    }

    @Test
    void sleepManager_Available() {
        assertNotNull(sleepManager);
    }

    @Test
    void platform_Detectable() {
        assertNotNull(PlatformAdapter.getPlatform());
    }

    // ========== Sleep flow ==========

    @Test
    void sleepingCount_Zero_WhenNoPlayersSleeping() {
        assertEquals(0, sleepManager.getSleepingCount(world));
    }

    @Test
    void player_BedEnter_IncreasesCount() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player));
            bukkit.when(() -> Bukkit.getPlayer(playerUuid)).thenReturn(player);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player);

            assertEquals(1, sleepManager.getSleepingCount(world));
            assertTrue(sleepManager.isPlayerSleeping(player));
        }
    }

    @Test
    void player_BedLeave_DecreasesCount() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player));
            bukkit.when(() -> Bukkit.getPlayer(playerUuid)).thenReturn(player);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player);
            assertEquals(1, sleepManager.getSleepingCount(world));

            sleepManager.onPlayerBedLeave(player);
            assertEquals(0, sleepManager.getSleepingCount(world));
            assertFalse(sleepManager.isPlayerSleeping(player));
        }
    }

    @Test
    void player_Quit_RemovesFromSleepingSet() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player));
            bukkit.when(() -> Bukkit.getPlayer(playerUuid)).thenReturn(player);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player);
            assertTrue(sleepManager.isPlayerSleeping(player));

            sleepManager.onPlayerQuit(player);
            assertFalse(sleepManager.isPlayerSleeping(player));
        }
    }

    @Test
    void skipScheduled_False_WhenNotScheduled() {
        assertFalse(sleepManager.isSkipScheduled(world));
    }

    @Test
    void totalPlayerCount_MatchesOnlinePlayers() {
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player p3 = mock(Player.class);
        for (Player p : new Player[]{p1, p2, p3}) {
            lenient().when(p.getWorld()).thenReturn(world);
            lenient().when(p.getGameMode()).thenReturn(org.bukkit.GameMode.SURVIVAL);
            lenient().when(p.isFlying()).thenReturn(false);
        }

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Arrays.asList(p1, p2, p3));
            int total = sleepManager.getTotalPlayerCount(world);
            assertEquals(3, total);
        }
    }

    @Test
    void playerEnteringBed_Twice_OnlyCountedOnce() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player));
            bukkit.when(() -> Bukkit.getPlayer(playerUuid)).thenReturn(player);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player);
            assertEquals(1, sleepManager.getSleepingCount(world));

            // Enter bed again (should be idempotent)
            sleepManager.onPlayerBedEnter(player);
            assertEquals(1, sleepManager.getSleepingCount(world));
        }
    }

    @Test
    void multiplePlayers_BedEnter_CountedCorrectly() {
        World world2 = mock(World.class);
        lenient().when(world2.getName()).thenReturn("world2");

        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        lenient().when(p1.getUniqueId()).thenReturn(u1);
        lenient().when(p1.getWorld()).thenReturn(world);
        lenient().when(p1.isOnline()).thenReturn(true);
        lenient().when(p1.isSleeping()).thenReturn(true);
        lenient().when(p2.getUniqueId()).thenReturn(u2);
        lenient().when(p2.getWorld()).thenReturn(world2);
        lenient().when(p2.isOnline()).thenReturn(true);
        lenient().when(p2.isSleeping()).thenReturn(true);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(u1)).thenReturn(p1);
            bukkit.when(() -> Bukkit.getPlayer(u2)).thenReturn(p2);
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Arrays.asList(p1, p2));
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(p1);
            sleepManager.onPlayerBedEnter(p2);

            assertEquals(1, sleepManager.getSleepingCount(world));
            assertEquals(1, sleepManager.getSleepingCount(world2));
        }
    }

    @Test
    void shutdown_CleansAllState() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player));
            bukkit.when(() -> Bukkit.getPlayer(playerUuid)).thenReturn(player);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player);
            assertTrue(sleepManager.isPlayerSleeping(player));

            sleepManager.shutdown();
            assertFalse(sleepManager.isPlayerSleeping(player));
            assertFalse(sleepManager.isSkipScheduled(world));
        }
    }

    @Test
    void shutdown_CanBeCalledMultipleTimes() {
        sleepManager.shutdown();
        assertDoesNotThrow(() -> sleepManager.shutdown());
    }
}

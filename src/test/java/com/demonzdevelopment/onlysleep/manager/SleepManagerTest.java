package com.demonzdevelopment.onlysleep.manager;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SleepManager}.
 * <p>
 * Uses {@link MockedStatic} to mock Bukkit static methods since
 * SleepManager calls {@code Bukkit.getOnlinePlayers()}, {@code Bukkit.getPlayer(UUID)},
 * and {@code Bukkit.getScheduler()} internally.
 */
@ExtendWith(MockitoExtension.class)
class SleepManagerTest {

    @Mock
    private Onlysleep plugin;

    @Mock
    private ConfigManager configManager;

    @Mock
    private World world;

    @Mock
    private Player player1;

    @Mock
    private Player player2;

    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private BukkitTask bukkitTask;

    private SleepManager sleepManager;

    private final UUID uuid1 = UUID.randomUUID();
    private final UUID uuid2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getConfigManager()).thenReturn(configManager);
        lenient().when(world.getName()).thenReturn("world");
        lenient().when(player1.getUniqueId()).thenReturn(uuid1);
        lenient().when(player2.getUniqueId()).thenReturn(uuid2);
        lenient().when(player1.getWorld()).thenReturn(world);
        lenient().when(player2.getWorld()).thenReturn(world);
        lenient().when(player1.getDisplayName()).thenReturn("Player1");
        lenient().when(player2.getDisplayName()).thenReturn("Player2");
        lenient().when(player1.getName()).thenReturn("Player1");
        lenient().when(player2.getName()).thenReturn("Player2");
        lenient().when(player1.isOnline()).thenReturn(true);
        lenient().when(player2.isOnline()).thenReturn(true);
        lenient().when(player1.isSleeping()).thenReturn(true);
        lenient().when(player2.isSleeping()).thenReturn(false);
        lenient().when(player1.getGameMode()).thenReturn(GameMode.SURVIVAL);
        lenient().when(player2.getGameMode()).thenReturn(GameMode.SURVIVAL);
        lenient().when(player1.isFlying()).thenReturn(false);
        lenient().when(player2.isFlying()).thenReturn(false);

        // Mock scheduler for SchedulerAdapter calls
        lenient().when(scheduler.runTask(any(Plugin.class), any(Runnable.class))).thenReturn(bukkitTask);
        lenient().when(scheduler.runTaskLater(any(Plugin.class), any(Runnable.class), anyLong())).thenReturn(bukkitTask);
        lenient().when(scheduler.runTaskTimer(any(Plugin.class), any(Runnable.class), anyLong(), anyLong())).thenReturn(bukkitTask);
        lenient().when(bukkitTask.isCancelled()).thenReturn(false);

        // Default config
        lenient().when(configManager.isPerWorldSleep()).thenReturn(true);
        lenient().when(configManager.getSleepPercentage()).thenReturn(50);
        lenient().when(configManager.isRequireAllPlayersOnline()).thenReturn(false);
        lenient().when(configManager.isCountAfkAsSleeping()).thenReturn(false);
        lenient().when(configManager.isExcludeAfkFromTotal()).thenReturn(true);
        lenient().when(configManager.isCountSpectators()).thenReturn(false);
        lenient().when(configManager.isCountFlying()).thenReturn(true);
        lenient().when(configManager.isIgnoreCreativeMode()).thenReturn(false);
        lenient().when(configManager.isWorldEnabled(anyString())).thenReturn(true);
        lenient().when(configManager.getMessage(anyString(), any())).thenReturn("message");

        sleepManager = new SleepManager(plugin, configManager);
    }

    @AfterEach
    void tearDown() {
        sleepManager.shutdown();
    }

    // ========== Helpers ==========

    /** Sets up Bukkit mocks for tests that involve calling Bukkit static methods. */
    private MockedStatic<Bukkit> mockBukkitForTwoPlayers() {
        MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        bukkit.when(() -> Bukkit.getPlayer(uuid1)).thenReturn(player1);
        bukkit.when(() -> Bukkit.getPlayer(uuid2)).thenReturn(player2);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Arrays.asList(player1, player2));
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        return bukkit;
    }

    // ========== isSkipScheduled ==========

    @Test
    void isSkipScheduled_ReturnsFalse_WhenNoSkipScheduled() {
        assertFalse(sleepManager.isSkipScheduled(world));
    }

    // ========== isPlayerSleeping ==========

    @Test
    void isPlayerSleeping_ReturnsFalse_WhenPlayerNotSleeping() {
        assertFalse(sleepManager.isPlayerSleeping(player1));
    }

    @Test
    void isPlayerSleeping_ReturnsTrue_AfterBedEnter() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            assertTrue(sleepManager.isPlayerSleeping(player1));
        }
    }

    @Test
    void isPlayerSleeping_ReturnsFalse_AfterBedLeave() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.onPlayerBedLeave(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));
        }
    }

    @Test
    void isPlayerSleeping_ReturnsFalse_AfterQuit() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.onPlayerQuit(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));
        }
    }

    // ========== getSleepingCount ==========

    @Test
    void getSleepingCount_ReturnsZero_WhenNoSleepingPlayers() {
        assertEquals(0, sleepManager.getSleepingCount(world));
    }

    @Test
    void getSleepingCount_ReturnsOne_WhenOnePlayerSleeping() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            assertEquals(1, sleepManager.getSleepingCount(world));
        }
    }

    @Test
    void getSleepingCount_Decrements_WhenPlayerLeaves() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            assertEquals(1, sleepManager.getSleepingCount(world));
            sleepManager.onPlayerBedLeave(player1);
            assertEquals(0, sleepManager.getSleepingCount(world));
        }
    }

    @Test
    void getSleepingCount_ReturnsZero_WhenPlayerNotActuallySleeping() {
        // player2 is set up with isSleeping() returning false
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player2);
            assertEquals(0, sleepManager.getSleepingCount(world));
        }
    }

    // ========== getTotalPlayerCount ==========

    @Test
    void getTotalPlayerCount_ReturnsZero_WhenNoPlayersOnline() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.emptyList());
            assertEquals(0, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_ReturnsOne_WhenOneEligiblePlayer() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(1, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_ExcludesSpectators_WhenConfigSaysNo() {
        when(configManager.isCountSpectators()).thenReturn(false);
        when(player1.getGameMode()).thenReturn(GameMode.SPECTATOR);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(0, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_IncludesSpectators_WhenConfigSaysYes() {
        when(configManager.isCountSpectators()).thenReturn(true);
        when(player1.getGameMode()).thenReturn(GameMode.SPECTATOR);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(1, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_ExcludesCreative_WhenIgnoreCreativeEnabled() {
        when(configManager.isIgnoreCreativeMode()).thenReturn(true);
        when(player1.getGameMode()).thenReturn(GameMode.CREATIVE);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(0, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_RespectsPerWorld_WhenEnabled() {
        World otherWorld = mock(World.class);
        lenient().when(otherWorld.getName()).thenReturn("other_world");
        lenient().when(player1.getWorld()).thenReturn(otherWorld);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(0, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_CountsAll_WhenPerWorldDisabled() {
        when(configManager.isPerWorldSleep()).thenReturn(false);

        World otherWorld = mock(World.class);
        lenient().when(otherWorld.getName()).thenReturn("other_world");
        lenient().when(player1.getWorld()).thenReturn(otherWorld);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(1, sleepManager.getTotalPlayerCount(world));
        }
    }

    @Test
    void getTotalPlayerCount_ExcludesExemptPlayers() {
        lenient().when(player1.hasPermission("onlysleep.exempt")).thenReturn(true);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(0, sleepManager.getTotalPlayerCount(world));
        }
    }

    // ========== getRequiredSleepingCount ==========

    @Test
    void getRequiredSleepingCount_ReturnsOne_WhenPercentageIsZero() {
        when(configManager.getSleepPercentage()).thenReturn(0);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.emptyList());
            assertEquals(1, sleepManager.getRequiredSleepingCount(world));
        }
    }

    @Test
    void getRequiredSleepingCount_ReturnsOne_WhenOnePlayerAndFiftyPercent() {
        when(configManager.getSleepPercentage()).thenReturn(50);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.singletonList(player1));
            assertEquals(1, sleepManager.getRequiredSleepingCount(world));
        }
    }

    @Test
    void getRequiredSleepingCount_ReturnsAllPlayers_WhenHundredPercent() {
        when(configManager.getSleepPercentage()).thenReturn(100);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Arrays.asList(player1, player2));
            assertEquals(2, sleepManager.getRequiredSleepingCount(world));
        }
    }

    @Test
    void getRequiredSleepingCount_ReturnsMaxValue_WhenRequireAllOnlineDisabled() {
        when(configManager.isRequireAllPlayersOnline()).thenReturn(false);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.emptyList());
            int result = sleepManager.getRequiredSleepingCount(world);
            assertNotEquals(Integer.MAX_VALUE, result, "Should not return MAX_VALUE when disabled");
        }
    }

    // ========== onPlayerBedEnter ==========

    @Test
    void onPlayerBedEnter_AddsPlayerToSleepingSet() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            assertTrue(sleepManager.isPlayerSleeping(player1));
        }
    }

    @Test
    void onPlayerBedEnter_IgnoresDisabledWorld() {
        when(configManager.isWorldEnabled("world")).thenReturn(false);
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));
        }
    }

    // ========== onPlayerBedLeave ==========

    @Test
    void onPlayerBedLeave_RemovesPlayerFromSleepingSet() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.onPlayerBedLeave(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));
        }
    }

    @Test
    void onPlayerBedLeave_DoesNotThrow_WhenPlayerNotInBed() {
        assertDoesNotThrow(() -> sleepManager.onPlayerBedLeave(player1));
    }

    // ========== onPlayerQuit ==========

    @Test
    void onPlayerQuit_DoesNotThrow_WhenPlayerNotSleeping() {
        assertDoesNotThrow(() -> sleepManager.onPlayerQuit(player1));
    }

    @Test
    void onPlayerQuit_RemovesSleepingPlayer() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.onPlayerQuit(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));
        }
    }

    // ========== shutdown ==========

    @Test
    void shutdown_ClearsAllState() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.shutdown();
            assertFalse(sleepManager.isPlayerSleeping(player1));
            assertFalse(sleepManager.isSkipScheduled(world));
        }
    }

    @Test
    void shutdown_CanBeCalledMultipleTimes() {
        sleepManager.shutdown();
        assertDoesNotThrow(() -> sleepManager.shutdown());
    }

    // ========== getSleepingPlayers ==========

    @Test
    void getSleepingPlayers_ReturnsNull_WhenNoneSleeping() {
        assertNull(sleepManager.getSleepingPlayers(world));
    }

    @Test
    void getSleepingPlayers_ReturnsSet_AfterSleeping() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            Set<UUID> sleeping = sleepManager.getSleepingPlayers(world);
            assertNotNull(sleeping);
            assertTrue(sleeping.contains(uuid1));
        }
    }

    // ========== Edge cases ==========

    @Test
    void multipleWorlds_TrackSeparately() {
        World world2 = mock(World.class);
        lenient().when(world2.getName()).thenReturn("world2");

        Player playerInWorld2 = mock(Player.class);
        UUID uuid3 = UUID.randomUUID();
        lenient().when(playerInWorld2.getUniqueId()).thenReturn(uuid3);
        lenient().when(playerInWorld2.getWorld()).thenReturn(world2);
        lenient().when(playerInWorld2.isOnline()).thenReturn(true);
        lenient().when(playerInWorld2.isSleeping()).thenReturn(true);
        lenient().when(playerInWorld2.getDisplayName()).thenReturn("Player3");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(uuid1)).thenReturn(player1);
            bukkit.when(() -> Bukkit.getPlayer(uuid3)).thenReturn(playerInWorld2);
            bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Arrays.asList(player1, playerInWorld2));
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);

            sleepManager.onPlayerBedEnter(player1); // world
            sleepManager.onPlayerBedEnter(playerInWorld2); // world2

            assertTrue(sleepManager.isPlayerSleeping(player1));
            assertTrue(sleepManager.isPlayerSleeping(playerInWorld2));
            assertEquals(1, sleepManager.getSleepingCount(world));
            assertEquals(1, sleepManager.getSleepingCount(world2));
        }
    }

    @Test
    void samePlayer_CanEnterAndLeaveMultipleTimes() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForTwoPlayers()) {
            sleepManager.onPlayerBedEnter(player1);
            sleepManager.onPlayerBedLeave(player1);
            assertFalse(sleepManager.isPlayerSleeping(player1));

            sleepManager.onPlayerBedEnter(player1);
            assertTrue(sleepManager.isPlayerSleeping(player1));
        }
    }
}

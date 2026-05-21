package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link SleepPlaceholderExpansion}.
 * <p>
 * Verifies each placeholder returns the expected value based on mocked state.
 * Uses MockedStatic for Bukkit static methods where needed.
 */
@ExtendWith(MockitoExtension.class)
class SleepPlaceholderExpansionTest {

    @Mock
    private Onlysleep plugin;

    @Mock
    private SleepManager sleepManager;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private PlatformAdapter.ServerPlatform platform;

    @Mock
    private PluginDescriptionFile description;

    private SleepPlaceholderExpansion expansion;

    private final UUID playerUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getSleepManager()).thenReturn(sleepManager);
        lenient().when(plugin.getConfigManager()).thenReturn(configManager);
        lenient().when(plugin.getPlatform()).thenReturn(platform);
        lenient().when(plugin.getDescription()).thenReturn(description);
        lenient().when(description.getVersion()).thenReturn("1.0.0");
        lenient().when(description.getAuthors()).thenReturn(java.util.Collections.singletonList("Demonz Development"));
        lenient().when(platform.getDisplayName()).thenReturn("Paper");

        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.getUniqueId()).thenReturn(playerUuid);

        lenient().when(world.getName()).thenReturn("world");
        lenient().when(world.getTime()).thenReturn(14000L); // Night time

        lenient().when(configManager.isWorldEnabled("world")).thenReturn(true);

        expansion = new SleepPlaceholderExpansion(plugin);
    }

    @Test
    void getIdentifier_ReturnsOnlysleep() {
        assertEquals("onlysleep", expansion.getIdentifier());
    }

    @Test
    void getVersion_ReturnsPluginVersion() {
        assertEquals("1.0.0", expansion.getVersion());
    }

    @Test
    void canRegister_ReturnsTrue() {
        assertTrue(expansion.canRegister());
    }

    @Test
    void persist_ReturnsTrue() {
        assertTrue(expansion.persist());
    }

    @Test
    void onPlaceholderRequest_ReturnsEmpty_WhenPlayerIsNull() {
        String result = expansion.onPlaceholderRequest(null, "sleeping");
        assertEquals("", result);
    }

    @Test
    void onPlaceholderRequest_Works_WhenPlayerIsNull_ForPlayerIndependent() {
        // version
        assertEquals("1.0.0", expansion.onPlaceholderRequest(null, "version"));

        // platform
        assertEquals("Paper", expansion.onPlaceholderRequest(null, "platform"));

        // percentage
        when(configManager.getSleepPercentage()).thenReturn(50);
        assertEquals("50", expansion.onPlaceholderRequest(null, "percentage"));

        // world_sleeping
        when(sleepManager.getSleepingCount(world)).thenReturn(2);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            assertEquals("2", expansion.onPlaceholderRequest(null, "world_sleeping_world"));
        }
    }

    @Test
    void placeholder_Sleeping_ReturnsCount() {
        when(sleepManager.getSleepingCount(world)).thenReturn(3);
        String result = expansion.onPlaceholderRequest(player, "sleeping");
        assertEquals("3", result);
    }

    @Test
    void placeholder_Required_ReturnsRequiredCount() {
        when(sleepManager.getRequiredSleepingCount(world)).thenReturn(5);
        String result = expansion.onPlaceholderRequest(player, "required");
        assertEquals("5", result);
    }

    @Test
    void placeholder_Progress_ReturnsZero_WhenRequiredIsZero() {
        when(sleepManager.getRequiredSleepingCount(world)).thenReturn(0);
        when(sleepManager.getSleepingCount(world)).thenReturn(0);
        String result = expansion.onPlaceholderRequest(player, "progress");
        assertEquals("0", result);
    }

    @Test
    void placeholder_Progress_ReturnsPercentage() {
        when(sleepManager.getRequiredSleepingCount(world)).thenReturn(10);
        when(sleepManager.getSleepingCount(world)).thenReturn(5);
        String result = expansion.onPlaceholderRequest(player, "progress");
        assertEquals("50", result);
    }

    @Test
    void placeholder_Skipping_ReturnsTrue() {
        when(sleepManager.isSkipScheduled(world)).thenReturn(true);
        String result = expansion.onPlaceholderRequest(player, "skipping");
        assertEquals("true", result);
    }

    @Test
    void placeholder_Skipping_ReturnsFalse() {
        when(sleepManager.isSkipScheduled(world)).thenReturn(false);
        String result = expansion.onPlaceholderRequest(player, "skipping");
        assertEquals("false", result);
    }

    @Test
    void placeholder_Enabled_ReturnsTrue() {
        String result = expansion.onPlaceholderRequest(player, "enabled");
        assertEquals("true", result);
    }

    @Test
    void placeholder_IsSleeping_ReturnsTrue() {
        when(sleepManager.isPlayerSleeping(player)).thenReturn(true);
        String result = expansion.onPlaceholderRequest(player, "is_sleeping");
        assertEquals("true", result);
    }

    @Test
    void placeholder_IsNight_ReturnsTrue_AtNightTime() {
        when(world.getTime()).thenReturn(14000L);
        String result = expansion.onPlaceholderRequest(player, "is_night");
        assertEquals("true", result);
    }

    @Test
    void placeholder_IsNight_ReturnsFalse_AtDayTime() {
        when(world.getTime()).thenReturn(1000L);
        String result = expansion.onPlaceholderRequest(player, "is_night");
        assertEquals("false", result);
    }

    @Test
    void placeholder_Percentage_ReturnsConfiguredValue() {
        when(configManager.getSleepPercentage()).thenReturn(75);
        String result = expansion.onPlaceholderRequest(player, "percentage");
        assertEquals("75", result);
    }

    @Test
    void placeholder_Platform_ReturnsDisplayName() {
        String result = expansion.onPlaceholderRequest(player, "platform");
        assertEquals("Paper", result);
    }

    @Test
    void placeholder_Status_ReturnsSleeping() {
        when(sleepManager.isPlayerSleeping(player)).thenReturn(true);
        String result = expansion.onPlaceholderRequest(player, "status");
        assertEquals("Sleeping", result);
    }

    @Test
    void placeholder_Status_ReturnsAwake() {
        when(sleepManager.isPlayerSleeping(player)).thenReturn(false);
        String result = expansion.onPlaceholderRequest(player, "status");
        assertEquals("Awake", result);
    }

    @Test
    void placeholder_Version_ReturnsVersion() {
        String result = expansion.onPlaceholderRequest(player, "version");
        assertEquals("1.0.0", result);
    }

    @Test
    void placeholder_Afk_ReturnsFalse_WhenActive() {
        String result = expansion.onPlaceholderRequest(player, "afk");
        assertEquals("false", result);
    }

    @Test
    void placeholder_Total_ReturnsCount() {
        when(sleepManager.getTotalPlayerCount(world)).thenReturn(10);
        String result = expansion.onPlaceholderRequest(player, "total");
        assertEquals("10", result);
    }

    @Test
    void placeholder_WorldSleeping_ReturnsCount() {
        when(sleepManager.getSleepingCount(world)).thenReturn(3);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            String result = expansion.onPlaceholderRequest(player, "world_sleeping_world");
            assertEquals("3", result);
        }
    }

    @Test
    void placeholder_WorldSleeping_ReturnsZero_WhenWorldNotFound() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("nonexistent")).thenReturn(null);
            String result = expansion.onPlaceholderRequest(player, "world_sleeping_nonexistent");
            assertEquals("0", result);
        }
    }

    @Test
    void placeholder_WorldRequired_ReturnsCount() {
        when(sleepManager.getRequiredSleepingCount(world)).thenReturn(5);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            String result = expansion.onPlaceholderRequest(player, "world_required_world");
            assertEquals("5", result);
        }
    }

    @Test
    void placeholder_WorldTotal_ReturnsCount() {
        when(sleepManager.getTotalPlayerCount(world)).thenReturn(10);

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            String result = expansion.onPlaceholderRequest(player, "world_total_world");
            assertEquals("10", result);
        }
    }

    @Test
    void placeholder_SleepingNames_ReturnsNone_WhenNoSleepers() {
        when(sleepManager.getSleepingPlayers(world)).thenReturn(null);
        String result = expansion.onPlaceholderRequest(player, "sleeping_names");
        assertEquals("None", result);
    }

    @Test
    void placeholder_SleepingNames_ReturnsCommaSeparated_WhenPlayersSleeping() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        when(sleepManager.getSleepingPlayers(world)).thenReturn(java.util.Set.of(uuid1, uuid2));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getPlayer(uuid1)).thenReturn(null);
            bukkit.when(() -> Bukkit.getPlayer(uuid2)).thenReturn(null);
            String result = expansion.onPlaceholderRequest(player, "sleeping_names");
            assertNotNull(result);
            assertTrue(result.contains("Unknown"), "Should contain 'Unknown' for unresolved UUIDs");
        }
    }

    @Test
    void placeholder_IsSleepable_ReturnsTrue_AtNight() {
        when(world.getTime()).thenReturn(14000L);
        String result = expansion.onPlaceholderRequest(player, "is_sleepable");
        assertEquals("true", result);
    }

    @Test
    void placeholder_IsSleepable_ReturnsFalse_DuringDay() {
        when(world.getTime()).thenReturn(1000L);
        when(world.hasStorm()).thenReturn(false);
        when(world.isThundering()).thenReturn(false);
        String result = expansion.onPlaceholderRequest(player, "is_sleepable");
        assertEquals("false", result);
    }

    @Test
    void placeholder_IsSleepable_ReturnsTrue_DuringStorm() {
        when(world.getTime()).thenReturn(1000L);
        when(world.hasStorm()).thenReturn(true);
        String result = expansion.onPlaceholderRequest(player, "is_sleepable");
        assertEquals("true", result);
    }

    @Test
    void placeholder_ProgressBar_CallsBuildProgressBar() {
        when(sleepManager.getSleepingCount(world)).thenReturn(3);
        when(sleepManager.getRequiredSleepingCount(world)).thenReturn(10);
        when(configManager.buildProgressBar(3, 10)).thenReturn("[=====     ]");
        String result = expansion.onPlaceholderRequest(player, "progress_bar");
        assertEquals("[=====     ]", result);
    }

    @Test
    void placeholder_Unknown_ReturnsNull() {
        String result = expansion.onPlaceholderRequest(player, "nonexistent_placeholder");
        assertNull(result);
    }
}

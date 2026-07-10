package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfkTrackerTest {

    @Mock
    private Onlysleep plugin;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Server server;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Player player;

    private java.util.UUID playerUuid;

    @BeforeEach
    void setUp() {
        playerUuid = java.util.UUID.randomUUID();
        lenient().when(plugin.getConfigManager()).thenReturn(configManager);
        lenient().when(plugin.getServer()).thenReturn(server);
        lenient().when(server.getPluginManager()).thenReturn(pluginManager);
        lenient().when(configManager.getAfkTimeSeconds()).thenReturn(300); 
        lenient().when(player.getUniqueId()).thenReturn(playerUuid);
    }

    @AfterEach
    void tearDown() {
        AfkTracker.shutdown();
    }

    private MockedStatic<Bukkit> mockBukkitForInit() {
        MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(scheduler.runTaskTimer(any(), any(Runnable.class), anyLong(), anyLong()))
            .thenReturn(mock(BukkitTask.class));
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        bukkit.when(Bukkit::getOnlinePlayers).thenReturn(java.util.Collections.emptyList());
        bukkit.when(() -> Bukkit.getPlayer(any(java.util.UUID.class))).thenReturn(null);
        return bukkit;
    }

    @Test
    void isAfk_ReturnsFalse_WhenTrackerNotInitialized() {
        assertFalse(AfkTracker.isAfk(player));
    }

    @Test
    void isAfk_ReturnsFalse_ForActivePlayer() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            AfkTracker.init(plugin);
            AfkTracker.updateActivity(player);
            assertFalse(AfkTracker.isAfk(player));
        }
    }

    @Test
    void isAfk_ReturnsTrue_ForInactivePlayer() throws Exception {

        when(configManager.getAfkTimeSeconds()).thenReturn(1);
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            AfkTracker.init(plugin);

            AfkTracker.updateActivity(player);

            setLastActivityTimestamp(playerUuid, System.currentTimeMillis() - 2500);
            assertTrue(AfkTracker.isAfk(player), "Player should be AFK after 2.5s of inactivity with 1s timeout");
        }
    }

    private void setLastActivityTimestamp(java.util.UUID uuid, long timestamp) throws Exception {
        java.lang.reflect.Field field = AfkTracker.class.getDeclaredField("lastActivity");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<java.util.UUID, Long> map = (java.util.Map<java.util.UUID, Long>) field.get(null);
        map.put(uuid, timestamp);
    }

    @Test
    void isAfk_ReturnsFalse_WhenTimeoutIsZero() {
        when(configManager.getAfkTimeSeconds()).thenReturn(0);

        AfkTracker.init(plugin);
        assertFalse(AfkTracker.isAfk(player));
    }

    @Test
    void isAfk_ReturnsFalse_WhenTimeoutIsNegative() {
        when(configManager.getAfkTimeSeconds()).thenReturn(-1);

        AfkTracker.init(plugin);
        assertFalse(AfkTracker.isAfk(player));
    }

    @Test
    void updateActivity_DoesNotThrowForNullPlayer() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            AfkTracker.init(plugin);
            assertDoesNotThrow(() -> AfkTracker.updateActivity(null));
        }
    }

    @Test
    void shutdown_ClearsState() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            AfkTracker.init(plugin);
            AfkTracker.updateActivity(player);
            AfkTracker.shutdown();

            assertFalse(AfkTracker.isAfk(player));
        }
    }

    @Test
    void init_DoesNotRegister_WhenTimeoutIsZero() {
        when(configManager.getAfkTimeSeconds()).thenReturn(0);
        AfkTracker.init(plugin);
        assertDoesNotThrow(() -> AfkTracker.isAfk(player));
    }

    @Test
    void isAfk_UpdatesActivity_OnFirstCheck() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            AfkTracker.init(plugin);

            assertFalse(AfkTracker.isAfk(player));

            assertFalse(AfkTracker.isAfk(player));
        }
    }

    @Test
    void init_WithValidTimeout_DoesNotThrow() {
        try (MockedStatic<Bukkit> bukkit = mockBukkitForInit()) {
            assertDoesNotThrow(() -> AfkTracker.init(plugin));
        }
    }
}

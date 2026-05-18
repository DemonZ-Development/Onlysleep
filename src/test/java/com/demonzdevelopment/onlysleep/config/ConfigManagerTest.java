package com.demonzdevelopment.onlysleep.config;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ConfigManager}.
 * <p>
 * Uses reflection to set internal state since {@code loadSettings()}
 * is private and requires a running server.
 */
@ExtendWith(MockitoExtension.class)
class ConfigManagerTest {

    @Mock
    private Onlysleep plugin;

    private ConfigManager configManager;

    @BeforeEach
    void setUp() throws Exception {
        configManager = new ConfigManager(plugin);

        // Use reflection to set internal fields for pure-logic tests
        setField("progressBarLength", 20);
        setField("progressBarSymbol", "\u25A0");
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ConfigManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(configManager, value);
    }

    /** Counts occurrences of a symbol character in a string (ignoring color codes). */
    private int countSymbols(String bar, String symbol) {
        String stripped = ChatColor.stripColor(bar);
        int count = 0;
        int idx = 0;
        while ((idx = stripped.indexOf(symbol, idx)) != -1) {
            count++;
            idx += symbol.length();
        }
        return count;
    }

    // --- buildProgressBar ---

    @Test
    void buildProgressBar_ReturnsEmptyString_WhenMaxIsZero() {
        String bar = configManager.buildProgressBar(5, 0);
        assertEquals("", bar);
    }

    @Test
    void buildProgressBar_ReturnsEmptyString_WhenMaxIsNegative() {
        String bar = configManager.buildProgressBar(5, -1);
        assertEquals("", bar);
    }

    @Test
    void buildProgressBar_ReturnsAllGreen_WhenCurrentEqualsMax() {
        String bar = configManager.buildProgressBar(20, 20);
        assertNotNull(bar);
        // All 20 symbols should be present
        assertEquals(20, countSymbols(bar, "\u25A0"), "All 20 symbols should be present");
        // Should contain green color code
        assertTrue(bar.contains(ChatColor.COLOR_CHAR + "a"), "Should contain green color code");
        // Should NOT contain gray color code (remaining = 0)
        assertFalse(bar.contains("§7"), "Should not contain gray section when nothing remains");
    }

    @Test
    void buildProgressBar_ReturnsHalfGreenHalfGray_WhenCurrentIsHalfOfMax() {
        String bar = configManager.buildProgressBar(10, 20);
        assertNotNull(bar);
        assertTrue(bar.contains(ChatColor.COLOR_CHAR + "a"), "Should contain green section");
        assertTrue(bar.contains(ChatColor.COLOR_CHAR + "7"), "Should contain gray section");
        assertEquals(20, countSymbols(bar, "\u25A0"), "Should have 20 symbols total");
    }

    @Test
    void buildProgressBar_ReturnsAllGray_WhenCurrentIsZero() {
        String bar = configManager.buildProgressBar(0, 20);
        assertNotNull(bar);
        // When completed = 0, the green section is empty but the color code &a is still in the string
        // The important thing is that ALL 20 symbols are gray
        assertTrue(bar.contains(ChatColor.COLOR_CHAR + "7"), "Should contain gray section");
        assertEquals(20, countSymbols(bar, "\u25A0"), "All 20 symbols should be present");
    }

    @Test
    void buildProgressBar_HasCorrectTotalSymbolCount() {
        String bar = configManager.buildProgressBar(7, 20);
        assertNotNull(bar);
        assertEquals(20, countSymbols(bar, "\u25A0"), "Progress bar should have exactly 20 symbols total");
    }

    @Test
    void buildProgressBar_DoesNotExceedMax() {
        String bar = configManager.buildProgressBar(30, 20);
        assertNotNull(bar);
        assertEquals(20, countSymbols(bar, "\u25A0"), "Should not exceed 20 even when current > max");
    }

    @Test
    void buildProgressBar_WithCustomSymbol() throws Exception {
        setField("progressBarSymbol", "#");
        String bar = configManager.buildProgressBar(10, 20);
        assertNotNull(bar);
        assertEquals(20, countSymbols(bar, "#"), "Should have 20 symbols with custom character");
    }

    @Test
    void buildProgressBar_WithCustomLength() throws Exception {
        setField("progressBarLength", 10);
        String bar = configManager.buildProgressBar(5, 10);
        assertNotNull(bar);
        assertEquals(10, countSymbols(bar, "\u25A0"), "Should have exactly 10 symbols with custom length");
    }

    // --- isGameModeDisabled ---

    @Test
    void isGameModeDisabled_ReturnsTrue_WhenGameModeInList() throws Exception {
        setField("disabledGameModes", Arrays.asList("CREATIVE", "SPECTATOR"));
        assertTrue(configManager.isGameModeDisabled("creative"));
        assertTrue(configManager.isGameModeDisabled("CREATIVE"));
        assertTrue(configManager.isGameModeDisabled("spectator"));
        assertFalse(configManager.isGameModeDisabled("survival"));
        assertFalse(configManager.isGameModeDisabled("adventure"));
    }

    @Test
    void isGameModeDisabled_ReturnsFalse_WhenListIsEmpty() throws Exception {
        setField("disabledGameModes", List.of());
        assertFalse(configManager.isGameModeDisabled("CREATIVE"));
    }

    // --- isWorldEnabled ---

    @Test
    void isWorldEnabled_ReturnsTrue_WhenWorldNotInDisabledList() throws Exception {
        setField("disabledWorlds", Arrays.asList("world_nether", "world_the_end"));
        assertTrue(configManager.isWorldEnabled("world"));
        assertFalse(configManager.isWorldEnabled("world_nether"), "Should return false for disabled world");
        assertFalse(configManager.isWorldEnabled("world_the_end"), "Should return false for disabled world");
    }

    @Test
    void getDisabledGameModes_ReturnsList() throws Exception {
        setField("disabledGameModes", Arrays.asList("CREATIVE", "SPECTATOR"));
        assertNotNull(configManager.getDisabledGameModes());
        assertEquals(2, configManager.getDisabledGameModes().size());
    }
}

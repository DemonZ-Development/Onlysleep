package com.demonzdevelopment.onlysleep.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigUpdaterTest {

    private JavaPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void update_SavesDefaultResource_WhenFileDoesNotExist() {
        File destination = new File("nonexistent_test_file.yml");
        destination.deleteOnExit();

        ConfigUpdater.update(plugin, "config.yml", destination);

        // Verify that saveResource was called since destination didn't exist
        verify(plugin, times(1)).saveResource("config.yml", false);
    }

    @Test
    void update_MergesNewKeys_AndPreservesCommentsAndUserValues(@TempDir Path tempDir) throws Exception {
        File destination = tempDir.resolve("config.yml").toFile();

        // 1. Create a simulated user config file with customized values
        List<String> userLines = List.of(
            "# Custom Header Comment",
            "sleep-percentage: 75 # User custom percentage",
            "skip-delay-ticks: 20",
            "# Nested structures",
            "afk-detection:",
            "  use-essentials: false",
            "  time-seconds: 150",
            "disabled-worlds:",
            "- custom_world"
        );
        Files.write(destination.toPath(), userLines, StandardCharsets.UTF_8);

        // 2. Create a simulated default config inside the jar (new keys added in a new update)
        String defaultJarContent = 
            "# Default Config Header\n" +
            "sleep-percentage: 50\n" +
            "skip-delay-ticks: 60\n" +
            "# A brand new key added in the update!\n" +
            "brand-new-setting: true\n" +
            "afk-detection:\n" +
            "  use-essentials: true\n" +
            "  use-cmi: true\n" +
            "  time-seconds: 300\n" +
            "disabled-worlds: []\n";

        when(plugin.getResource("config.yml")).thenReturn(new ByteArrayInputStream(defaultJarContent.getBytes(StandardCharsets.UTF_8)));

        // 3. Run updater
        ConfigUpdater.update(plugin, "config.yml", destination);

        // 4. Verify the updated file contains the correct elements
        List<String> updatedLines = Files.readAllLines(destination.toPath(), StandardCharsets.UTF_8);

        // Verify structure & comments
        boolean foundComment = false;
        boolean foundNewSetting = false;
        boolean foundUserValue = false;
        boolean foundNestedNewSetting = false;

        for (String line : updatedLines) {
            if (line.contains("# A brand new key added in the update!")) {
                foundComment = true;
            }
            if (line.contains("brand-new-setting: true")) {
                foundNewSetting = true;
            }
            if (line.contains("sleep-percentage: 75")) {
                foundUserValue = true;
            }
            if (line.contains("use-cmi: true")) {
                foundNestedNewSetting = true;
            }
        }

        assertTrue(foundComment, "Should contain the new comments from the jar resource");
        assertTrue(foundNewSetting, "Should merge new config keys with default values");
        assertTrue(foundUserValue, "Should preserve the user's customized values");
        assertTrue(foundNestedNewSetting, "Should merge new nested keys (like afk-detection.use-cmi)");

        // 5. Verify it's a valid YAML file via YamlConfiguration loader
        FileConfiguration resultConfig = YamlConfiguration.loadConfiguration(destination);
        assertEquals(75, resultConfig.getInt("sleep-percentage"));
        assertEquals(20, resultConfig.getInt("skip-delay-ticks"));
        assertTrue(resultConfig.getBoolean("brand-new-setting"));
        assertFalse(resultConfig.getBoolean("afk-detection.use-essentials"));
        assertTrue(resultConfig.getBoolean("afk-detection.use-cmi"));
        assertEquals(150, resultConfig.getInt("afk-detection.time-seconds"));
        assertEquals(List.of("custom_world"), resultConfig.getStringList("disabled-worlds"));
    }
}

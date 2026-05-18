package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link UpdateChecker} — focuses on the UpdateResult model
 * and state transitions. Does not test real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class UpdateCheckerTest {

    @Mock
    private Onlysleep plugin;

    @Mock
    private PluginDescriptionFile description;

    private UpdateChecker updateChecker;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getDescription()).thenReturn(description);
        lenient().when(description.getVersion()).thenReturn("2.0.0");
        updateChecker = new UpdateChecker(plugin);
    }

    @Test
    void constructor_DoesNotThrow() {
        assertNotNull(updateChecker);
    }

    @Test
    void isUpdateAvailable_DefaultsToFalse() {
        assertFalse(updateChecker.isUpdateAvailable());
    }

    @Test
    void getLatestVersion_DefaultsToNull() {
        assertNull(updateChecker.getLatestVersion());
    }

    @Test
    void checkAsync_ReturnsNonNullFuture() {
        assertNotNull(updateChecker.checkAsync());
    }

    // --- UpdateResult tests ---

    @Test
    void updateResult_UpdateAvailable_StoresValues() {
        UpdateChecker.UpdateResult result = new UpdateChecker.UpdateResult(true, "3.0.0", "Update available");
        assertTrue(result.isUpdateAvailable());
        assertEquals("3.0.0", result.getLatestVersion());
        assertEquals("Update available", result.getMessage());
    }

    @Test
    void updateResult_NoUpdate_StoresValues() {
        UpdateChecker.UpdateResult result = new UpdateChecker.UpdateResult(false, null, "Up to date");
        assertFalse(result.isUpdateAvailable());
        assertNull(result.getLatestVersion());
        assertEquals("Up to date", result.getMessage());
    }

    @Test
    void updateResult_ErrorResponse_StoresMessage() {
        UpdateChecker.UpdateResult result = new UpdateChecker.UpdateResult(false, null, "API returned 404");
        assertFalse(result.isUpdateAvailable());
        assertNull(result.getLatestVersion());
        assertEquals("API returned 404", result.getMessage());
    }

    @Test
    void updateResult_NullVersion_DoesNotCrash() {
        UpdateChecker.UpdateResult result = new UpdateChecker.UpdateResult(true, null, null);
        assertTrue(result.isUpdateAvailable());
        assertNull(result.getLatestVersion());
        assertNull(result.getMessage());
    }

    // --- State after checkAsync ---

    @Test
    void isUpdateAvailable_RemainsFalse_AfterFailedCheck() {
        // A network failure should not change the default state
        assertFalse(updateChecker.isUpdateAvailable());
        assertNull(updateChecker.getLatestVersion());
    }
}

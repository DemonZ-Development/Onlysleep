package com.demonzdevelopment.onlysleep.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PlatformAdapter}.
 * <p>
 * Resets the internal static caches before each test so detection runs fresh.
 * Tests verify that methods don't throw and return sensible defaults,
 * without assuming a specific server type is present on the classpath.
 */
class PlatformAdapterTest {

    @BeforeEach
    void setUp() throws Exception {
        resetPlatformAdapterCache();
    }

    /** Resets {@link PlatformAdapter}'s cached detection fields via reflection. */
    private void resetPlatformAdapterCache() throws Exception {
        setStaticField(PlatformAdapter.class, "platform", null);
        setStaticField(PlatformAdapter.class, "folia", null);
        setStaticField(PlatformAdapter.class, "paper", null);
    }

    private void setStaticField(Class<?> clazz, String name, Object value) throws Exception {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    void serverPlatform_EnumValues() {
        assertEquals(4, PlatformAdapter.ServerPlatform.values().length);
    }

    @Test
    void serverPlatform_ValueOf_Folia() {
        assertNotNull(PlatformAdapter.ServerPlatform.valueOf("FOLIA"));
    }

    @Test
    void serverPlatform_ValueOf_Paper() {
        assertNotNull(PlatformAdapter.ServerPlatform.valueOf("PAPER"));
    }

    @Test
    void serverPlatform_ValueOf_Spigot() {
        assertNotNull(PlatformAdapter.ServerPlatform.valueOf("SPIGOT"));
    }

    @Test
    void serverPlatform_ValueOf_Bukkit() {
        assertNotNull(PlatformAdapter.ServerPlatform.valueOf("BUKKIT"));
    }

    @Test
    void serverPlatform_DisplayName_Folia() {
        assertEquals("Folia", PlatformAdapter.ServerPlatform.FOLIA.getDisplayName());
    }

    @Test
    void serverPlatform_DisplayName_Paper() {
        assertEquals("Paper", PlatformAdapter.ServerPlatform.PAPER.getDisplayName());
    }

    @Test
    void serverPlatform_DisplayName_Spigot() {
        assertEquals("Spigot", PlatformAdapter.ServerPlatform.SPIGOT.getDisplayName());
    }

    @Test
    void serverPlatform_DisplayName_Bukkit() {
        assertEquals("Bukkit", PlatformAdapter.ServerPlatform.BUKKIT.getDisplayName());
    }

    @Test
    void isFolia_DoesNotThrow() {
        // Without a running server, Class.forName will be used (no exception)
        assertDoesNotThrow(PlatformAdapter::isFolia);
    }

    @Test
    void isPaper_DoesNotThrow() {
        assertDoesNotThrow(PlatformAdapter::isPaper);
    }

    @Test
    void isSpigot_DoesNotThrow() {
        // Uses Class.forName internally — should never throw
        assertDoesNotThrow(PlatformAdapter::isSpigot);
    }

    @Test
    void getPlatform_DoesNotThrow() {
        assertDoesNotThrow(PlatformAdapter::getPlatform);
    }

    @Test
    void getPlatform_ReturnsNonNull() {
        assertNotNull(PlatformAdapter.getPlatform());
    }

    @Test
    void getPlatform_ReturnsKnownValue() {
        // The platform should be one of the 4 known types
        PlatformAdapter.ServerPlatform p = PlatformAdapter.getPlatform();
        assertTrue(
            p == PlatformAdapter.ServerPlatform.FOLIA ||
            p == PlatformAdapter.ServerPlatform.PAPER ||
            p == PlatformAdapter.ServerPlatform.SPIGOT ||
            p == PlatformAdapter.ServerPlatform.BUKKIT,
            "Platform must be one of the 4 known types, got: " + p
        );
    }
}

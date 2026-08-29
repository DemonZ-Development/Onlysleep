package com.demonzdevelopment.onlysleep.fabric.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionUtilTest {

    @Test
    void compare_MajorMinorPatch() {
        assertEquals(-1, Integer.signum(VersionUtil.compare("1.3.0", "1.4.0")));
        assertEquals(1, Integer.signum(VersionUtil.compare("2.0.0", "1.9.9")));
        assertEquals(0, VersionUtil.compare("1.3.1", "1.3.1"));
    }

    @Test
    void compare_PatchLevel() {
        assertEquals(-1, Integer.signum(VersionUtil.compare("1.3.1", "1.3.2")));
        assertEquals(1, Integer.signum(VersionUtil.compare("1.3.10", "1.3.9")));
    }

    @Test
    void compare_IgnoresPreReleaseSuffixesByFirstMatch() {
        assertEquals(0, VersionUtil.compare("1.3.0-beta.1", "1.3.0"));
    }

    @Test
    void compare_UnparsableInputIsNeutral() {
        assertEquals(0, VersionUtil.compare("abc", "1.0.0"));
        assertEquals(0, VersionUtil.compare("1.0.0", ""));
    }
}

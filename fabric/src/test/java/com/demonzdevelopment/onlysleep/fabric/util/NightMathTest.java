package com.demonzdevelopment.onlysleep.fabric.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightMathTest {

    @Test
    void isNight_BoundsMatchVanillaSleepWindow() {
        assertTrue(NightMath.isNight(12542));
        assertTrue(NightMath.isNight(18000));
        assertTrue(NightMath.isNight(23458));
        assertFalse(NightMath.isNight(12541));
        assertFalse(NightMath.isNight(23459));
        assertFalse(NightMath.isNight(1000));
    }

    @Test
    void requiredSleepers_ZeroPercentageMeansOne() {
        assertEquals(1, NightMath.requiredSleepers(10, 0));
    }

    @Test
    void requiredSleepers_FiftyPercentRoundsUp() {
        assertEquals(1, NightMath.requiredSleepers(1, 50));
        assertEquals(2, NightMath.requiredSleepers(3, 50));
        assertEquals(5, NightMath.requiredSleepers(10, 50));
    }

    @Test
    void requiredSleepers_HundredPercentMeansEveryone() {
        assertEquals(4, NightMath.requiredSleepers(4, 100));
        assertEquals(1, NightMath.requiredSleepers(0, 100));
    }

    @Test
    void distanceTo_WrapsAroundMidnight() {
        assertEquals(11000L, NightMath.distanceTo(13000, 0));
        assertEquals(12000L, NightMath.distanceTo(13000, 1000));
        assertEquals(500L, NightMath.distanceTo(13000, 13500));
    }

    @Test
    void gradualSteps_CoversFullDistance() {
        assertEquals(334, NightMath.gradualSteps(10000, 30));
        assertEquals(1, NightMath.gradualSteps(29, 30));
        assertEquals(0, NightMath.gradualSteps(0, 30));
    }

    @Test
    void progressBar_SplitsCompletedAndRemaining() {
        String bar = NightMath.progressBar(5, 10, "X", 20);
        assertTrue(bar.startsWith("&a"));
        assertTrue(bar.contains("&7"));
        assertEquals(24, bar.length());
    }

    @Test
    void progressBar_Empty_WhenMaxZeroOrLengthZero() {
        assertEquals("", NightMath.progressBar(5, 0, "X", 20));
        assertEquals("", NightMath.progressBar(5, 10, "X", 0));
    }
}

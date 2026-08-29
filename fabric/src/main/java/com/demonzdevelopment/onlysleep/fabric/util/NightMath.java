package com.demonzdevelopment.onlysleep.fabric.util;

public final class NightMath {

    public static final long NIGHT_START_TICK = 12542;
    public static final long NIGHT_END_TICK = 23458;

    private NightMath() {}

    public static boolean isNight(long time) {
        return time >= NIGHT_START_TICK && time <= NIGHT_END_TICK;
    }

    public static int requiredSleepers(int total, int percentage) {
        if (percentage <= 0) return 1;
        if (percentage >= 100) return Math.max(1, total);
        return Math.max(1, (int) Math.ceil(total * percentage / 100.0));
    }

    public static long distanceTo(long from, long target) {
        if (target <= from) {
            return (24000 - from) + target;
        }
        return target - from;
    }

    public static int gradualSteps(long distance, int speedPerStep) {
        return (int) Math.ceil((double) distance / speedPerStep);
    }

    public static String progressBar(double current, double max, String symbol, int length) {
        if (max <= 0 || length <= 0) return "";
        int completed = (int) Math.round((current / max) * length);
        if (completed > length) completed = length;
        if (completed < 0) completed = 0;

        StringBuilder bar = new StringBuilder("&a");
        bar.append(symbol.repeat(completed));
        int remaining = length - completed;
        if (remaining > 0) {
            bar.append("&7").append(symbol.repeat(remaining));
        }
        return bar.toString();
    }
}

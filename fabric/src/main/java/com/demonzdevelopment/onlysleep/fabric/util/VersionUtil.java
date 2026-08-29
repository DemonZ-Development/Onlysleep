package com.demonzdevelopment.onlysleep.fabric.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionUtil {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    private VersionUtil() {}

    public static int compare(String v1, String v2) {
        Matcher m1 = VERSION_PATTERN.matcher(v1);
        Matcher m2 = VERSION_PATTERN.matcher(v2);

        if (!m1.find() || !m2.find()) return 0;

        try {
            int major1 = Integer.parseInt(m1.group(1));
            int minor1 = Integer.parseInt(m1.group(2));
            int patch1 = Integer.parseInt(m1.group(3));

            int major2 = Integer.parseInt(m2.group(1));
            int minor2 = Integer.parseInt(m2.group(2));
            int patch2 = Integer.parseInt(m2.group(3));

            if (major1 != major2) return Integer.compare(major1, major2);
            if (minor1 != minor2) return Integer.compare(minor1, minor2);
            return Integer.compare(patch1, patch2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

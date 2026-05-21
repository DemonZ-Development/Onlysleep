package com.demonzdevelopment.onlysleep.util;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks for plugin updates against Modrinth's API.
 */
public final class UpdateChecker {

    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/onlysleep/version";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    private final Onlysleep plugin;
    private String latestVersion;
    private boolean updateAvailable = false;

    public UpdateChecker(Onlysleep plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks for updates asynchronously against Modrinth API.
     */
    public CompletableFuture<UpdateResult> checkAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URI uri = new URI(MODRINTH_API + "?loaders=%5B%22paper%22%2C%22spigot%22%2C%22folia%22%5D");
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "Onlysleep/" + plugin.getDescription().getVersion());

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return new UpdateResult(false, null, "API returned " + responseCode);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    String json = response.toString();
                    String versionField = "\"version_number\":\"";
                    int startIdx = json.indexOf(versionField);
                    if (startIdx != -1) {
                        startIdx += versionField.length();
                        int endIdx = json.indexOf("\"", startIdx);
                        if (endIdx != -1) {
                            this.latestVersion = json.substring(startIdx, endIdx);
                            this.updateAvailable = compareVersions(
                                plugin.getDescription().getVersion(),
                                this.latestVersion
                            ) < 0;
                            return new UpdateResult(
                                this.updateAvailable,
                                this.latestVersion,
                                this.updateAvailable ? "Update available: " + this.latestVersion : "Up to date"
                            );
                        }
                    }
                }

                return new UpdateResult(false, null, "Could not parse version info");

            } catch (Exception e) {
                return new UpdateResult(false, null, "Check failed: " + e.getMessage());
            }
        });
    }

    /**
     * Compares two semantic versions.
     * Returns negative if v1 < v2, positive if v1 > v2, 0 if equal.
     */
    private int compareVersions(String v1, String v2) {
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

    public static class UpdateResult {
        private final boolean updateAvailable;
        private final String latestVersion;
        private final String message;

        public UpdateResult(boolean updateAvailable, String latestVersion, String message) {
            this.updateAvailable = updateAvailable;
            this.latestVersion = latestVersion;
            this.message = message;
        }

        public boolean isUpdateAvailable() { return updateAvailable; }
        public String getLatestVersion() { return latestVersion; }
        public String getMessage() { return message; }
    }
}

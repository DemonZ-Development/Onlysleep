package com.demonzdevelopment.onlysleep.fabric.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public final class UpdateChecker {

    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/onlysleep/version";

    private final String currentVersion;
    private volatile CompletableFuture<Result> inFlight;

    public UpdateChecker(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public CompletableFuture<Result> checkAsync() {
        synchronized (this) {
            if (inFlight != null && !inFlight.isDone()) {
                return inFlight;
            }
            inFlight = doCheck();
            return inFlight;
        }
    }

    private CompletableFuture<Result> doCheck() {
        return CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                URI uri = new URI(MODRINTH_API + "?loaders=%5B%22fabric%22%5D");
                connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "Onlysleep-Fabric/" + currentVersion);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return new Result(false, null, "API returned " + responseCode);
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
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
                            String latest = json.substring(startIdx, endIdx);
                            boolean available = VersionUtil.compare(currentVersion, latest) < 0;
                            return new Result(available, latest,
                                available ? "Update available: " + latest : "Up to date");
                        }
                    }
                }

                return new Result(false, null, "Could not parse version info");
            } catch (Exception e) {
                return new Result(false, null, "Check failed: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public record Result(boolean updateAvailable, String latestVersion, String message) {}
}

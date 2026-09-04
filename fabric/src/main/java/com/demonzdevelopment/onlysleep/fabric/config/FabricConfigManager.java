package com.demonzdevelopment.onlysleep.fabric.config;

import com.demonzdevelopment.onlysleep.fabric.util.LegacyText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Path configDir;
    private JsonObject config = new JsonObject();
    private JsonObject messages = new JsonObject();

    public FabricConfigManager(Path configDir) {
        this.configDir = configDir;
    }

    public void load() {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create config directory " + configDir, e);
        }

        this.config = loadWithDefaults("config.json");
        this.messages = loadWithDefaults("messages.json");
    }

    public void reload() {
        load();
    }

    private JsonObject loadWithDefaults(String fileName) {
        JsonObject defaults;
        String resource = "/onlysleep-defaults/" + fileName;
        try (InputStream in = FabricConfigManager.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing bundled default resource " + resource);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                defaults = GSON.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read bundled default resource " + resource, e);
        }

        Path target = configDir.resolve(fileName);
        JsonObject user = new JsonObject();
        if (Files.exists(target)) {
            try (Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
                JsonElement parsed = GSON.fromJson(reader, JsonElement.class);
                if (parsed != null && parsed.isJsonObject()) {
                    user = parsed.getAsJsonObject();
                }
            } catch (Exception e) {
                user = new JsonObject();
            }
        }

        JsonObject merged = merge(defaults, user);

        try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(merged, writer);
        } catch (IOException ignored) {
        }

        return merged;
    }

    private static JsonObject merge(JsonObject defaults, JsonObject user) {
        JsonObject out = defaults.deepCopy();
        for (Map.Entry<String, JsonElement> entry : user.entrySet()) {
            JsonElement replacement = entry.getValue();
            JsonElement current = out.get(entry.getKey());
            if (replacement != null && replacement.isJsonObject()
                    && current != null && current.isJsonObject()) {
                out.add(entry.getKey(), merge(current.getAsJsonObject(), replacement.getAsJsonObject()));
            } else if (replacement != null) {
                out.add(entry.getKey(), replacement.deepCopy());
            }
        }
        return out;
    }

    private JsonElement get(String path) {
        String[] parts = path.split("\\.");
        JsonElement current = messages;
        for (String part : parts) {
            if (current == null || !current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
        }
        return current;
    }

    public MutableComponent getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }

    public MutableComponent getMessage(String path, Map<String, String> placeholders) {
        JsonElement element = get(path);
        String raw = element != null && element.isJsonPrimitive() ? element.getAsString() : "";
        if (raw.isEmpty()) {
            return LegacyText.of("\u00a7cMessage not found: " + path);
        }

        String prefix = "\u00a78[\u00a7bOnlysleep\u00a78] \u00a7r";
        JsonElement prefixEl = get("prefix");
        if (prefixEl != null && prefixEl.isJsonPrimitive()) {
            prefix = prefixEl.getAsString();
        }

        String result = prefix + translateAmp(raw);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return LegacyText.of(result);
    }

    public MutableComponent getRawMessage(String path, Map<String, String> placeholders) {
        JsonElement element = get(path);
        String raw = element != null && element.isJsonPrimitive() ? element.getAsString() : "";
        if (raw.isEmpty()) {
            return LegacyText.of("\u00a7cMessage not found: " + path);
        }

        String result = translateAmp(raw);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return LegacyText.of(result);
    }

    public String buildProgressBar(double current, double max) {
        String symbol = getStringOr("ui.progress-bar.symbol", "\u25a0");
        int length = getInt("ui.progress-bar.length", 20);
        return com.demonzdevelopment.onlysleep.fabric.util.NightMath.progressBar(current, max, symbol, length);
    }

    private static String translateAmp(String input) {
        StringBuilder sb = new StringBuilder(input);
        for (int i = 0; i < sb.length() - 1; i++) {
            if (sb.charAt(i) == '&') {
                char code = Character.toLowerCase(sb.charAt(i + 1));
                if ("0123456789abcdefklmnor".indexOf(code) >= 0) {
                    sb.setCharAt(i, '\u00a7');
                }
            }
        }
        return sb.toString();
    }

    public boolean isWorldEnabled(String worldId) {
        for (String disabled : getStringList("disabled-worlds")) {
            if (disabled.equalsIgnoreCase(worldId)) return false;
        }
        return true;
    }

    public boolean isGameModeDisabled(String gameMode) {
        for (String gm : getStringList("disabled-gamemodes")) {
            if (gm.equalsIgnoreCase(gameMode)) return true;
        }
        return false;
    }

    public int getInt(String path, int def) {
        JsonElement el = walkConfig(path);
        return el != null && el.isJsonPrimitive() ? el.getAsInt() : def;
    }

    public double getDouble(String path, double def) {
        JsonElement el = walkConfig(path);
        return el != null && el.isJsonPrimitive() ? el.getAsDouble() : def;
    }

    public boolean getBool(String path, boolean def) {
        JsonElement el = walkConfig(path);
        return el != null && el.isJsonPrimitive() ? el.getAsBoolean() : def;
    }

    public String getStringOr(String path, String def) {
        JsonElement el = walkConfig(path);
        return el != null && el.isJsonPrimitive() ? el.getAsString() : def;
    }

    public List<String> getStringList(String path) {
        JsonElement el = walkConfig(path);
        if (el == null || !el.isJsonArray()) return List.of();
        return el.getAsJsonArray().asList().stream()
            .filter(JsonElement::isJsonPrimitive)
            .map(e -> e.getAsJsonPrimitive().getAsString())
            .toList();
    }

    private JsonElement walkConfig(String path) {
        String[] parts = path.split("\\.");
        JsonElement current = config;
        for (String part : parts) {
            if (current == null || !current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
        }
        return current;
    }

    public int getSleepPercentage() { return getInt("sleep-percentage", 0); }
    public int getSkipDelayTicks() { return getInt("skip-delay-ticks", 60); }
    public String getSkipType() { return getStringOr("skip-type", "instant"); }
    public int getGradualSkipSpeedTicks() { return getInt("gradual-skip-speed-ticks", 30); }
    public int getMorningTime() { return getInt("morning-time", 1000); }
    public boolean isResetTime() { return getBool("reset-time", true); }
    public boolean isPerWorldSleep() { return getBool("per-world-sleep", true); }
    public boolean isRequireAllPlayersOnline() { return getBool("require-all-players-online", false); }

    public boolean isClearWeather() { return getBool("clear-weather", true); }
    public boolean isResetWeather() { return getBool("reset-weather", true); }
    public boolean isClearThunder() { return getBool("clear-thunder", true); }
    public boolean isResetThunder() { return getBool("reset-thunder", true); }
    public boolean isResetWeatherCycle() { return getBool("reset-weather-cycle", true); }

    public boolean isCountAfkAsSleeping() { return getBool("count-afk-as-sleeping", false); }
    public boolean isExcludeAfkFromTotal() { return getBool("exclude-afk-from-total", true); }
    public boolean isCountSpectators() { return getBool("count-spectators", false); }
    public boolean isCountFlying() { return getBool("count-flying", true); }
    public boolean isIgnoreCreativeMode() { return getBool("ignore-creative-mode", false); }

    public int getAfkTimeSeconds() { return getInt("afk-detection.time-seconds", 300); }

    public boolean isShowProgressBar() { return getBool("ui.progress-bar.enabled", true); }
    public boolean isShowBossBar() { return getBool("ui.boss-bar.enabled", true); }
    public String getBossBarColor() { return getStringOr("ui.boss-bar.color", "BLUE"); }
    public String getBossBarStyle() { return getStringOr("ui.boss-bar.style", "SOLID"); }
    public boolean isShowTitle() { return getBool("ui.title.enabled", false); }
    public String getTitleMessage() { return getStringOr("ui.title.title", "&bGood Morning!"); }
    public String getSubtitleMessage() { return getStringOr("ui.title.subtitle", "&fNight skipped by &b%player%"); }
    public int getTitleFadeIn() { return getInt("ui.title.fade-in", 10); }
    public int getTitleStay() { return getInt("ui.title.stay", 70); }
    public int getTitleFadeOut() { return getInt("ui.title.fade-out", 20); }
    public boolean isShowActionBar() { return getBool("ui.action-bar.enabled", true); }

    public boolean isPlaySounds() { return getBool("sounds.enabled", true); }
    public String getSkipSound() { return getStringOr("sounds.skip-sound", "minecraft:entity.player.levelup"); }
    public float getSkipSoundVolume() { return (float) getDouble("sounds.skip-sound-volume", 1.0); }
    public float getSkipSoundPitch() { return (float) getDouble("sounds.skip-sound-pitch", 1.0); }
    public String getNightSound() { return getStringOr("sounds.night-sound", "minecraft:block.note_block.pling"); }
    public float getNightSoundVolume() { return (float) getDouble("sounds.night-sound-volume", 0.5); }
    public float getNightSoundPitch() { return (float) getDouble("sounds.night-sound-pitch", 1.0); }
    public String getStormSound() { return getStringOr("sounds.storm-sound", "minecraft:entity.lightning_bolt.thunder"); }
    public float getStormSoundVolume() { return (float) getDouble("sounds.storm-sound-volume", 1.0); }
    public float getStormSoundPitch() { return (float) getDouble("sounds.storm-sound-pitch", 1.0); }

    public boolean isManageGamerule() { return getBool("manage-gamerule", true); }
    public boolean isCheckForUpdates() { return getBool("check-for-updates", true); }



    public void save() {
        Path target = configDir.resolve("config.json");
        try (Writer writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config.json", e);
        }
    }

    public void setValue(String path, Object value) {
        JsonElement element;
        if (value instanceof Boolean) element = new com.google.gson.JsonPrimitive((Boolean) value);
        else if (value instanceof Number) element = new com.google.gson.JsonPrimitive((Number) value);
        else if (value instanceof String) element = new com.google.gson.JsonPrimitive((String) value);
        else if (value instanceof List) {
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (Object o : (List<?>) value) arr.add(o == null ? com.google.gson.JsonNull.INSTANCE : new com.google.gson.JsonPrimitive(o.toString()));
            element = arr;
        } else if (value instanceof JsonElement) element = (JsonElement) value;
        else element = new com.google.gson.JsonPrimitive(value.toString());
        setJson(path, element);
        save();
    }

    private void setJson(String path, JsonElement value) {
        String[] parts = path.split("\\.");
        JsonObject current = config;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            JsonElement next = current.get(part);
            if (next == null || !next.isJsonObject()) {
                JsonObject created = new JsonObject();
                current.add(part, created);
                current = created;
            } else {
                current = next.getAsJsonObject();
            }
        }
        current.add(parts[parts.length - 1], value);
    }

    public String getValueAsString(String path) {
        JsonElement el = walkConfig(path);
        if (el == null || el.isJsonNull()) return null;
        if (el.isJsonPrimitive()) return el.getAsString();
        if (el.isJsonArray()) return el.toString();
        return el.toString();
    }

    public boolean setSleepPercentage(int value) {
        if (value < 0 || value > 100) return false;
        setValue("sleep-percentage", value);
        return true;
    }

    public boolean setSkipType(String type) {
        String lower = type.toLowerCase();
        if (!lower.equals("instant") && !lower.equals("gradual") && !lower.equals("speed")) return false;
        setValue("skip-type", lower);
        return true;
    }

    public void setPerWorldSleep(boolean v) { setValue("per-world-sleep", v); }
    public void setSkipDelayTicks(int v) { setValue("skip-delay-ticks", v); }
    public void setMorningTime(int v) { setValue("morning-time", v); }
    public void setResetTime(boolean v) { setValue("reset-time", v); }
    public void setGradualSkipSpeedTicks(int v) { setValue("gradual-skip-speed-ticks", v); }
    public void setClearWeather(boolean v) { setValue("clear-weather", v); }
    public void setClearThunder(boolean v) { setValue("clear-thunder", v); }
    public void setResetWeather(boolean v) { setValue("reset-weather", v); }
    public void setResetThunder(boolean v) { setValue("reset-thunder", v); }
    public void setManageGamerule(boolean v) { setValue("manage-gamerule", v); }
}

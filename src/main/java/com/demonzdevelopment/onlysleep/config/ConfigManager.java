package com.demonzdevelopment.onlysleep.config;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Onlysleep plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    // Sleep settings
    private int sleepPercentage;
    private int skipDelayTicks;
    private int morningTime;
    private boolean resetTime;
    private boolean perWorldSleep;
    private boolean requireAllPlayersOnline;

    // Weather settings
    private boolean clearWeather;
    private boolean resetWeather;
    private boolean clearThunder;
    private boolean resetThunder;

    // Player filtering
    private boolean countAfkAsSleeping;
    private boolean excludeAfkFromTotal;
    private boolean countSpectators;
    private boolean countFlying;
    private boolean ignoreCreativeMode;

    // AFK detection
    private boolean useEssentialsAfk;
    private boolean useCmiAfk;
    private int afkTimeSeconds;

    // UI settings
    private boolean showProgressBar;
    private String progressBarSymbol;
    private int progressBarLength;
    private boolean showBossBar;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;
    private boolean showTitle;
    private String titleMessage;
    private String subtitleMessage;
    private int titleFadeIn;
    private int titleStay;
    private int titleFadeOut;
    private boolean showActionBar;

    // Sound settings
    private boolean playSounds;
    private String skipSound;
    private float skipSoundVolume;
    private float skipSoundPitch;
    private String nightSound;
    private float nightSoundVolume;
    private float nightSoundPitch;
    private String stormSound;
    private float stormSoundVolume;
    private float stormSoundPitch;

    // Night skip type
    private String skipType; // "instant", "speed", "gradual"
    private int gradualSkipSpeedTicks;
    private boolean resetWeatherCycle;

    // Gamerule management
    private boolean manageGamerule;

    // Update checker
    private boolean checkForUpdates;

    // Disabled worlds
    private List<String> disabledWorlds;

    // Disabled game modes
    private List<String> disabledGameModes;

    public ConfigManager(Onlysleep plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        // Automatically merge new default options and comments into config.yml
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        ConfigUpdater.update(plugin, "config.yml", configFile);
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Automatically merge new default options and comments into messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        ConfigUpdater.update(plugin, "messages.yml", messagesFile);
        loadMessages();
        loadSettings();
    }

    public void reload() {
        loadConfigs();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void loadSettings() {
        // Sleep
        this.sleepPercentage = config.getInt("sleep-percentage", 50);
        this.skipDelayTicks = config.getInt("skip-delay-ticks", 60);
        this.morningTime = config.getInt("morning-time", 1000);
        this.resetTime = config.getBoolean("reset-time", true);
        this.perWorldSleep = config.getBoolean("per-world-sleep", true);
        this.requireAllPlayersOnline = config.getBoolean("require-all-players-online", false);

        // Weather
        this.clearWeather = config.getBoolean("clear-weather", true);
        this.resetWeather = config.getBoolean("reset-weather", true);
        this.clearThunder = config.getBoolean("clear-thunder", true);
        this.resetThunder = config.getBoolean("reset-thunder", true);

        // Player filtering
        this.countAfkAsSleeping = config.getBoolean("count-afk-as-sleeping", false);
        this.excludeAfkFromTotal = config.getBoolean("exclude-afk-from-total", true);
        this.countSpectators = config.getBoolean("count-spectators", false);
        this.countFlying = config.getBoolean("count-flying", true);
        this.ignoreCreativeMode = config.getBoolean("ignore-creative-mode", false);

        // AFK detection
        this.useEssentialsAfk = config.getBoolean("afk-detection.use-essentials", true);
        this.useCmiAfk = config.getBoolean("afk-detection.use-cmi", true);
        this.afkTimeSeconds = config.getInt("afk-detection.time-seconds", 300);

        // UI - Progress bar
        this.showProgressBar = config.getBoolean("ui.progress-bar.enabled", true);
        this.progressBarSymbol = config.getString("ui.progress-bar.symbol", "■");
        this.progressBarLength = config.getInt("ui.progress-bar.length", 20);

        // UI - Boss bar
        this.showBossBar = config.getBoolean("ui.boss-bar.enabled", true);
        try {
            this.bossBarColor = BarColor.valueOf(config.getString("ui.boss-bar.color", "BLUE").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.bossBarColor = BarColor.BLUE;
        }
        try {
            this.bossBarStyle = BarStyle.valueOf(config.getString("ui.boss-bar.style", "SOLID").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.bossBarStyle = BarStyle.SOLID;
        }

        // UI - Title
        this.showTitle = config.getBoolean("ui.title.enabled", false);
        this.titleMessage = config.getString("ui.title.title", "&bGood Morning!");
        this.subtitleMessage = config.getString("ui.title.subtitle", "&fNight skipped by &b%player%");
        this.titleFadeIn = config.getInt("ui.title.fade-in", 10);
        this.titleStay = config.getInt("ui.title.stay", 70);
        this.titleFadeOut = config.getInt("ui.title.fade-out", 20);

        // UI - Action bar
        this.showActionBar = config.getBoolean("ui.action-bar.enabled", true);

        // Sound settings
        this.playSounds = config.getBoolean("sounds.enabled", true);
        this.skipSound = config.getString("sounds.skip-sound", "ENTITY_PLAYER_LEVELUP");
        this.skipSoundVolume = (float) config.getDouble("sounds.skip-sound-volume", 1.0);
        this.skipSoundPitch = (float) config.getDouble("sounds.skip-sound-pitch", 1.0);
        this.nightSound = config.getString("sounds.night-sound", "ENTITY_PLAYER_LEVELUP");
        this.nightSoundVolume = (float) config.getDouble("sounds.night-sound-volume", 0.5);
        this.nightSoundPitch = (float) config.getDouble("sounds.night-sound-pitch", 1.0);
        this.stormSound = config.getString("sounds.storm-sound", "ENTITY_LIGHTNING_BOLT_THUNDER");
        this.stormSoundVolume = (float) config.getDouble("sounds.storm-sound-volume", 1.0);
        this.stormSoundPitch = (float) config.getDouble("sounds.storm-sound-pitch", 1.0);

        // Skip type
        this.skipType = config.getString("skip-type", "instant");
        this.gradualSkipSpeedTicks = config.getInt("gradual-skip-speed-ticks", 30);
        this.resetWeatherCycle = config.getBoolean("reset-weather-cycle", true);

        // Gamerule
        this.manageGamerule = config.getBoolean("manage-gamerule", true);

        // Update checker
        this.checkForUpdates = config.getBoolean("check-for-updates", true);

        // Disabled worlds
        this.disabledWorlds = config.getStringList("disabled-worlds");

        // Disabled game modes
        this.disabledGameModes = config.getStringList("disabled-gamemodes");
    }

    // --- Message methods ---

    public String getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null || message.isEmpty()) {
            return ChatColor.RED + "Message not found: " + path;
        }

        // Apply prefix
        String prefix = ChatColor.translateAlternateColorCodes('&',
            messages.getString("prefix", "&8[&bOnlysleep&8] &r"));
        String result = prefix + ChatColor.translateAlternateColorCodes('&', message);

        // Apply placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return result;
    }

    public String getRawMessage(String path) {
        String message = messages.getString(path);
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getRawMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null) return "";
        String result = ChatColor.translateAlternateColorCodes('&', message);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }

    /**
     * Builds a visual progress bar string.
     */
    public String buildProgressBar(double current, double max) {
        if (max <= 0) return "";
        int completed = (int) Math.round((current / max) * progressBarLength);
        // Cap at progressBarLength to handle the case where current > max
        if (completed > progressBarLength) completed = progressBarLength;
        int remaining = progressBarLength - completed;

        StringBuilder bar = new StringBuilder();
        bar.append("&a");
        for (int i = 0; i < completed; i++) bar.append(progressBarSymbol);
        if (remaining > 0) {
            bar.append("&7");
            for (int i = 0; i < remaining; i++) bar.append(progressBarSymbol);
        }

        return ChatColor.translateAlternateColorCodes('&', bar.toString());
    }

    /**
     * Checks if a world is enabled for sleep features.
     */
    public boolean isWorldEnabled(String worldName) {
        return !disabledWorlds.contains(worldName);
    }

    // --- Getters ---

    public int getSleepPercentage() { return sleepPercentage; }
    public int getSkipDelayTicks() { return skipDelayTicks; }
    public int getMorningTime() { return morningTime; }
    public boolean isResetTime() { return resetTime; }
    public boolean isPerWorldSleep() { return perWorldSleep; }
    public boolean isRequireAllPlayersOnline() { return requireAllPlayersOnline; }

    public boolean isClearWeather() { return clearWeather; }
    public boolean isResetWeather() { return resetWeather; }
    public boolean isClearThunder() { return clearThunder; }
    public boolean isResetThunder() { return resetThunder; }

    public boolean isCountAfkAsSleeping() { return countAfkAsSleeping; }
    public boolean isExcludeAfkFromTotal() { return excludeAfkFromTotal; }
    public boolean isCountSpectators() { return countSpectators; }
    public boolean isCountFlying() { return countFlying; }
    public boolean isIgnoreCreativeMode() { return ignoreCreativeMode; }

    public boolean isUseEssentialsAfk() { return useEssentialsAfk; }
    public boolean isUseCmiAfk() { return useCmiAfk; }
    public int getAfkTimeSeconds() { return afkTimeSeconds; }

    public boolean isShowProgressBar() { return showProgressBar; }
    public int getProgressBarLength() { return progressBarLength; }
    public boolean isShowBossBar() { return showBossBar; }
    public BarColor getBossBarColor() { return bossBarColor; }
    public BarStyle getBossBarStyle() { return bossBarStyle; }
    public boolean isShowTitle() { return showTitle; }
    public String getTitleMessage() { return titleMessage; }
    public String getSubtitleMessage() { return subtitleMessage; }
    public int getTitleFadeIn() { return titleFadeIn; }
    public int getTitleStay() { return titleStay; }
    public int getTitleFadeOut() { return titleFadeOut; }
    public boolean isShowActionBar() { return showActionBar; }

    public boolean isPlaySounds() { return playSounds; }
    public String getSkipSound() { return skipSound; }
    public float getSkipSoundVolume() { return skipSoundVolume; }
    public float getSkipSoundPitch() { return skipSoundPitch; }
    public String getNightSound() { return nightSound; }
    public float getNightSoundVolume() { return nightSoundVolume; }
    public float getNightSoundPitch() { return nightSoundPitch; }
    public String getStormSound() { return stormSound; }
    public float getStormSoundVolume() { return stormSoundVolume; }
    public float getStormSoundPitch() { return stormSoundPitch; }

    public String getSkipType() { return skipType; }
    public int getGradualSkipSpeedTicks() { return gradualSkipSpeedTicks; }
    public boolean isResetWeatherCycle() { return resetWeatherCycle; }

    public boolean isManageGamerule() { return manageGamerule; }
    public boolean isCheckForUpdates() { return checkForUpdates; }

    public List<String> getDisabledGameModes() { return disabledGameModes; }

    public boolean isGameModeDisabled(String gameMode) {
        return disabledGameModes.contains(gameMode.toUpperCase());
    }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getMessages() { return messages; }
}

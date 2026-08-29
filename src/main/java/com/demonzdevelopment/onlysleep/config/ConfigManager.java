package com.demonzdevelopment.onlysleep.config;

import com.demonzdevelopment.onlysleep.Onlysleep;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final Onlysleep plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    private int sleepPercentage;
    private int skipDelayTicks;
    private int morningTime;
    private boolean resetTime;
    private boolean perWorldSleep;
    private boolean requireAllPlayersOnline;

    private boolean clearWeather;
    private boolean resetWeather;
    private boolean clearThunder;
    private boolean resetThunder;

    private boolean countAfkAsSleeping;
    private boolean excludeAfkFromTotal;
    private boolean countSpectators;
    private boolean countFlying;
    private boolean ignoreCreativeMode;

    private boolean useEssentialsAfk;
    private boolean useCmiAfk;
    private int afkTimeSeconds;

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

    private String skipType;
    private int gradualSkipSpeedTicks;
    private boolean resetWeatherCycle;

    private boolean manageGamerule;

    private boolean checkForUpdates;

    private List<String> disabledWorlds;

    private List<String> disabledGameModes;

    public ConfigManager(Onlysleep plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        ConfigUpdater.update(plugin, "config.yml", configFile);
        plugin.reloadConfig();
        this.config = plugin.getConfig();

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

        this.sleepPercentage = config.getInt("sleep-percentage", 0);
        this.skipDelayTicks = config.getInt("skip-delay-ticks", 60);
        this.morningTime = config.getInt("morning-time", 1000);
        this.resetTime = config.getBoolean("reset-time", true);
        this.perWorldSleep = config.getBoolean("per-world-sleep", true);
        this.requireAllPlayersOnline = config.getBoolean("require-all-players-online", false);

        this.clearWeather = config.getBoolean("clear-weather", true);
        this.resetWeather = config.getBoolean("reset-weather", true);
        this.clearThunder = config.getBoolean("clear-thunder", true);
        this.resetThunder = config.getBoolean("reset-thunder", true);

        this.countAfkAsSleeping = config.getBoolean("count-afk-as-sleeping", false);
        this.excludeAfkFromTotal = config.getBoolean("exclude-afk-from-total", true);
        this.countSpectators = config.getBoolean("count-spectators", false);
        this.countFlying = config.getBoolean("count-flying", true);
        this.ignoreCreativeMode = config.getBoolean("ignore-creative-mode", false);

        this.useEssentialsAfk = config.getBoolean("afk-detection.use-essentials", true);
        this.useCmiAfk = config.getBoolean("afk-detection.use-cmi", true);
        this.afkTimeSeconds = config.getInt("afk-detection.time-seconds", 300);

        this.showProgressBar = config.getBoolean("ui.progress-bar.enabled", true);
        this.progressBarSymbol = config.getString("ui.progress-bar.symbol", "■");
        this.progressBarLength = config.getInt("ui.progress-bar.length", 20);

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

        this.showTitle = config.getBoolean("ui.title.enabled", false);
        this.titleMessage = config.getString("ui.title.title", "&bGood Morning!");
        this.subtitleMessage = config.getString("ui.title.subtitle", "&fNight skipped by &b%player%");
        this.titleFadeIn = config.getInt("ui.title.fade-in", 10);
        this.titleStay = config.getInt("ui.title.stay", 70);
        this.titleFadeOut = config.getInt("ui.title.fade-out", 20);

        this.showActionBar = config.getBoolean("ui.action-bar.enabled", true);

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

        this.skipType = config.getString("skip-type", "instant");
        this.gradualSkipSpeedTicks = config.getInt("gradual-skip-speed-ticks", 30);
        this.resetWeatherCycle = config.getBoolean("reset-weather-cycle", true);

        this.manageGamerule = config.getBoolean("manage-gamerule", true);

        this.checkForUpdates = config.getBoolean("check-for-updates", true);

        this.disabledWorlds = config.getStringList("disabled-worlds");

        this.disabledGameModes = config.getStringList("disabled-gamemodes");
    }

    public String getMessage(String path) {
        return getMessage(path, new HashMap<>());
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getString(path);
        if (message == null || message.isEmpty()) {
            return ChatColor.RED + "Message not found: " + path;
        }

        String prefix = ChatColor.translateAlternateColorCodes('&',
            messages.getString("prefix", "&8[&bOnlysleep&8] &r"));
        String result = prefix + ChatColor.translateAlternateColorCodes('&', message);

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

    public String buildProgressBar(double current, double max) {
        if (max <= 0) return "";
        int completed = (int) Math.round((current / max) * progressBarLength);

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

    public boolean isWorldEnabled(String worldName) {
        return !disabledWorlds.contains(worldName);
    }

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

    public void setValue(String path, Object value) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        plugin.getConfig().set(path, value);
        try {
            plugin.saveConfig();
        } catch (Exception e) {

            try {
                config.set(path, value);
                config.save(configFile);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to save config value " + path + ": " + ex.getMessage());
            }
        }
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadSettings();
    }

    public String getValueAsString(String path) {
        if (config == null || !config.contains(path)) return null;
        Object val = config.get(path);
        if (val instanceof List) {
            List<?> list = (List<?>) val;
            if (list.isEmpty()) return "[]";
            return String.join(", ", list.stream().map(Object::toString).collect(java.util.stream.Collectors.toList()));
        }
        return val == null ? "null" : val.toString();
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

    public void setPerWorldSleep(boolean value) { setValue("per-world-sleep", value); }
    public void setSkipDelayTicks(int value) { setValue("skip-delay-ticks", value); }
    public void setMorningTime(int value) { setValue("morning-time", value); }
    public void setResetTime(boolean value) { setValue("reset-time", value); }
    public void setGradualSkipSpeedTicks(int value) { setValue("gradual-skip-speed-ticks", value); }
    public void setClearWeather(boolean value) { setValue("clear-weather", value); }
    public void setClearThunder(boolean value) { setValue("clear-thunder", value); }
    public void setResetWeather(boolean value) { setValue("reset-weather", value); }
    public void setResetThunder(boolean value) { setValue("reset-thunder", value); }
    public void setManageGamerule(boolean value) { setValue("manage-gamerule", value); }

    public FileConfiguration getConfig() { return config; }
    public FileConfiguration getMessages() { return messages; }
}

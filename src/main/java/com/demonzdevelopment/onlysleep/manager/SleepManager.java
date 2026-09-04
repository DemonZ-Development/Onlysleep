package com.demonzdevelopment.onlysleep.manager;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.util.AfkTracker;
import com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter.ScheduledTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SleepManager {

    public static final long NIGHT_START_TICK = 12542;

    public static final long NIGHT_END_TICK = 23458;

    public static boolean isNight(long time) {
        return time >= NIGHT_START_TICK && time <= NIGHT_END_TICK;
    }

    private final Onlysleep plugin;
    private final ConfigManager configManager;
    private final GameruleAccess gameruleAccess;
    private final Map<World, Set<UUID>> sleepingPlayers = new ConcurrentHashMap<>();
    private final Map<World, ScheduledTask> skipTasks = new ConcurrentHashMap<>();
    private final Map<World, BossBar> worldBossBars = new ConcurrentHashMap<>();
    private final Map<World, ScheduledTask> bossBarTasks = new ConcurrentHashMap<>();
    private final Set<World> activeTransitions = ConcurrentHashMap.newKeySet();
    private final Map<World, String> skippingPlayerNames = new ConcurrentHashMap<>();
    private final Map<World, GradualSkipState> gradualSkipStates = new ConcurrentHashMap<>();

    private final Map<World, Integer> originalGameruleValues = new ConcurrentHashMap<>();

    private record GradualSkipState(
        int totalSteps,
        int[] currentStep,
        long targetTime,
        long originalTime,
        Runnable onComplete
    ) {}

    public SleepManager(Onlysleep plugin, ConfigManager configManager) {
        this(plugin, configManager, new BukkitGameruleAccess());
    }

    SleepManager(Onlysleep plugin, ConfigManager configManager, GameruleAccess gameruleAccess) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameruleAccess = gameruleAccess;
    }

    interface GameruleAccess {
        Integer get(World world);

        void set(World world, int value);
    }

    private static final class BukkitGameruleAccess implements GameruleAccess {
        private GameRule<Integer> rule;

        @Override
        public Integer get(World world) {
            return world.getGameRuleValue(rule());
        }

        @Override
        public void set(World world, int value) {
            world.setGameRule(rule(), value);
        }

        private GameRule<Integer> rule() {
            if (rule == null) {
                rule = resolve();
            }
            return rule;
        }

        @SuppressWarnings({"unchecked", "removal"})
        private static GameRule<Integer> resolve() {
            try {
                Class<?> gameRules = Class.forName("org.bukkit.GameRules");
                return (GameRule<Integer>) gameRules.getField("PLAYERS_SLEEPING_PERCENTAGE").get(null);
            } catch (ClassNotFoundException e) {
                return GameRule.PLAYERS_SLEEPING_PERCENTAGE;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to resolve players sleeping percentage gamerule", e);
            }
        }
    }

    public void applyGamerules() {
        boolean manageGamerule = configManager.isManageGamerule();

        for (World world : new HashSet<>(originalGameruleValues.keySet())) {
            if (!manageGamerule || !configManager.isWorldEnabled(world.getName())) {
                restoreGamerule(world);
            }
        }

        if (!manageGamerule) {
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            applyGamerule(world);
        }
    }

    public void applyGamerule(World world) {
        if (!configManager.isManageGamerule()
                || !configManager.isWorldEnabled(world.getName())) {
            restoreGamerule(world);
            return;
        }

        Integer current = gameruleAccess.get(world);
        if (current == null) {
            return;
        }

        originalGameruleValues.putIfAbsent(world, current);
        gameruleAccess.set(world, 101);
    }

    public void restoreGamerules() {
        for (World world : new HashSet<>(originalGameruleValues.keySet())) {
            restoreGamerule(world);
        }
    }

    private void restoreGamerule(World world) {
        Integer original = originalGameruleValues.remove(world);
        if (original != null) {
            gameruleAccess.set(world, original);
        }
    }

    public void onPlayerBedEnter(Player player) {
        World world = player.getWorld();

        if (!configManager.isWorldEnabled(world.getName())) return;

        sleepingPlayers.computeIfAbsent(world, k -> ConcurrentHashMap.newKeySet()).add(player.getUniqueId());


        try {
            int cur = getSleepingCount(world);
            int req = getRequiredSleepingCount(world);
            com.demonzdevelopment.onlysleep.api.events.SleepStartEvent startEvent = new com.demonzdevelopment.onlysleep.api.events.SleepStartEvent(player, cur, req);
            Bukkit.getPluginManager().callEvent(startEvent);
            if (startEvent.isCancelled()) {
                Set<UUID> set = sleepingPlayers.get(world);
                if (set != null) {
                    set.remove(player.getUniqueId());
                    if (set.isEmpty()) sleepingPlayers.remove(world);
                }
                return;
            }
        } catch (Exception ignored) {}

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getDisplayName());
        placeholders.put("count", String.valueOf(getSleepingCount(world)));
        placeholders.put("required", String.valueOf(getRequiredSleepingCount(world)));

        String message = configManager.getMessage("sleep.start-sleep", placeholders);
        for (Player p : world.getPlayers()) {
            p.sendMessage(message);
        }

        if (configManager.isPlaySounds()) {
            playSound(world, configManager.getNightSound(),
                configManager.getNightSoundVolume(), configManager.getNightSoundPitch());
        }

        checkSleepStatus(world);
    }

    private static final long DAWN_COMPLETE_WINDOW_TICKS = 1500;

    private static boolean isMorningArrived(World world) {
        return !isNight(world.getTime()) && !world.hasStorm() && !world.isThundering();
    }

    private long skipTargetTime() {
        return configManager.isResetTime() ? 0 : configManager.getMorningTime();
    }

    private static boolean isSkipCompleting(World world, long targetTime) {
        if (isMorningArrived(world)) return true;
        long now = world.getTime() % 24000;
        long remaining = targetTime <= now ? (24000 - now) + targetTime : targetTime - now;
        return remaining <= DAWN_COMPLETE_WINDOW_TICKS;
    }

    public void onPlayerBedLeave(Player player) {
        World world = player.getWorld();
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null) {
            players.remove(player.getUniqueId());
            if (players.isEmpty()) {
                sleepingPlayers.remove(world);
            }
        }

        if ((activeTransitions.contains(world) || skipTasks.containsKey(world)) && isSkipCompleting(world, skipTargetTime())) {
            return;
        }

        if (activeTransitions.contains(world)) {

            if (skipTasks.containsKey(world)) {
                int required = getRequiredSleepingCount(world);
                int current = getSleepingCount(world);
                if (current < required) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", player.getDisplayName());
                    String message = configManager.getMessage("sleep.cancelled", placeholders);
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(message);
                    }
                    try {
                        com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent cancelEvent = new com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent(player, com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent.Cause.BED_LEAVE, current, required);
                        Bukkit.getPluginManager().callEvent(cancelEvent);
                    } catch (Exception ignored) {}
                    cancelSkip(world);
                    gradualSkipStates.remove(world);
                }
            }
            return;
        }

        if (skipTasks.containsKey(world)) {
            int required = getRequiredSleepingCount(world);
            int current = getSleepingCount(world);
            if (current < required) {

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", player.getDisplayName());
                String message = configManager.getMessage("sleep.cancelled", placeholders);
                for (Player p : world.getPlayers()) {
                    p.sendMessage(message);
                }
                try {
                    com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent cancelEvent = new com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent(player, com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent.Cause.BED_LEAVE, current, required);
                    Bukkit.getPluginManager().callEvent(cancelEvent);
                } catch (Exception ignored) {}
                cancelSkip(world);
            }
        }
    }

    public void onPlayerQuit(Player player) {
        World world = player.getWorld();
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null) {
            players.remove(player.getUniqueId());
            if (players.isEmpty()) {
                sleepingPlayers.remove(world);
            }
        }

        if ((activeTransitions.contains(world) || skipTasks.containsKey(world)) && isSkipCompleting(world, skipTargetTime())) {
            return;
        }

        if (activeTransitions.contains(world)) {
            if (skipTasks.containsKey(world)) {
                int required = getRequiredSleepingCount(world);
                int current = getSleepingCount(world);
                if (current < required) {
                    try {
                        com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent cancelEvent = new com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent(player, com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent.Cause.QUIT, current, required);
                        Bukkit.getPluginManager().callEvent(cancelEvent);
                    } catch (Exception ignored) {}
                    cancelSkip(world);
                    gradualSkipStates.remove(world);
                }
            }
            return;
        }

        if (skipTasks.containsKey(world)) {
            int required = getRequiredSleepingCount(world);
            int current = getSleepingCount(world);
            if (current < required) {
                try {
                    com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent cancelEvent = new com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent(player, com.demonzdevelopment.onlysleep.api.events.SleepCancelEvent.Cause.QUIT, current, required);
                    Bukkit.getPluginManager().callEvent(cancelEvent);
                } catch (Exception ignored) {}
                cancelSkip(world);
            }
        } else {

            checkSleepStatus(world);
        }
    }

    public boolean isSkipScheduled(World world) {
        return skipTasks.containsKey(world);
    }

    public boolean isPlayerSleeping(Player player) {
        Set<UUID> players = sleepingPlayers.get(player.getWorld());
        return players != null && players.contains(player.getUniqueId());
    }

    private void checkSleepStatus(World world) {
        if (skipTasks.containsKey(world)) return;

        int required = getRequiredSleepingCount(world);
        int current = getSleepingCount(world);

        if (current >= required && required > 0) {
            scheduleSkip(world);
        }
    }

    private void scheduleSkip(World world) {
        int delay = configManager.getSkipDelayTicks();

        skippingPlayerNames.put(world, getSleepingPlayerName(world));
        if (configManager.isShowBossBar()) {
            showBossBarForWorld(world);
        }
        if (configManager.isShowBossBar() || configManager.isShowActionBar()) {
            ScheduledTask uiTask = SchedulerAdapter.runTaskTimer(plugin, world, () -> {
                if (!skipTasks.containsKey(world)) {
                    removeBossBar(world);
                    return;
                }
                updateSleepStatus(world);
            }, 0L, 10L);
            bossBarTasks.put(world, uiTask);
        }

        ScheduledTask task = SchedulerAdapter.runTaskLater(plugin, world, () -> {
            skipNight(world);
        }, delay);

        skipTasks.put(world, task);
    }

    private void cancelSkip(World world) {
        activeTransitions.remove(world);
        ScheduledTask task = skipTasks.remove(world);
        if (task != null) {
            task.cancel();
        }
        skippingPlayerNames.remove(world);
        removeBossBar(world);
    }

    private void skipNight(World world) {

        try {
            Player initiator = null;
            Set<UUID> sleeping = sleepingPlayers.get(world);
            if (sleeping != null && !sleeping.isEmpty()) {
                initiator = Bukkit.getPlayer(sleeping.iterator().next());
            }
            com.demonzdevelopment.onlysleep.api.events.NightSkipEvent skipEvent = new com.demonzdevelopment.onlysleep.api.events.NightSkipEvent(world, initiator, getSleepingCount(world), getRequiredSleepingCount(world), getTotalPlayerCount(world));
            Bukkit.getPluginManager().callEvent(skipEvent);
            if (skipEvent.isCancelled()) {
                cancelSkip(world);
                return;
            }
        } catch (Exception ignored) {}

        boolean clearedRain = false;
        if (configManager.isClearWeather() && world.hasStorm()) {
            if (configManager.isResetWeather()) {
                world.setStorm(false);
                clearedRain = true;
                if (configManager.isResetWeatherCycle()) {
                    world.setWeatherDuration(Integer.MAX_VALUE);
                }
            }
        }

        boolean clearedThunder = false;
        if (configManager.isClearThunder() && world.isThundering()) {
            if (configManager.isResetThunder()) {
                world.setThundering(false);
                clearedThunder = true;
                if (configManager.isResetWeatherCycle()) {
                    world.setThunderDuration(Integer.MAX_VALUE);
                }
            }
        }

        final boolean weatherWasCleared = clearedRain || clearedThunder;

        final String playerName = getSleepingPlayerName(world);
        skippingPlayerNames.put(world, playerName);

        Runnable onSkipComplete = () -> {

            activeTransitions.remove(world);

            if (configManager.isPlaySounds()) {
                if (weatherWasCleared) {
                    playSound(world, configManager.getStormSound(),
                        configManager.getStormSoundVolume(), configManager.getStormSoundPitch());
                }
                playSkipSound(world);
            }

            if (configManager.isShowTitle()) {
                showSkipTitle(world, playerName);
            }

            SchedulerAdapter.runGlobalTask(plugin, () ->
                broadcastSkipMessages(world, playerName, weatherWasCleared)
            );

            sleepingPlayers.remove(world);
            skipTasks.remove(world);
            removeBossBar(world);
        };

        String skipType = configManager.getSkipType();
        long targetTime = configManager.isResetTime() ? 0 : configManager.getMorningTime();

        switch (skipType.toLowerCase()) {
            case "gradual":
                activeTransitions.add(world);
                ScheduledTask gradualTask = scheduleGradualSkip(world, targetTime,
                        configManager.getGradualSkipSpeedTicks(), onSkipComplete);
                if (gradualTask != null) {
                    skipTasks.put(world, gradualTask);
                }
                break;
            case "speed":
                activeTransitions.add(world);
                ScheduledTask speedTask = scheduleGradualSkip(world, targetTime, 150, onSkipComplete);
                if (speedTask != null) {
                    skipTasks.put(world, speedTask);
                }
                break;
            case "instant":
            default:
                world.setTime(targetTime);
                onSkipComplete.run();
                break;
        }
    }

    private void broadcastSkipMessages(World world, String playerName, boolean clearedWeather) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);

        String clearingMsg = clearedWeather ? configManager.getMessage("weather.clearing", placeholders) : null;
        String skipMsg = configManager.getMessage("sleep.enough-sleeping", placeholders);

        for (Player p : world.getPlayers()) {
            if (clearingMsg != null) p.sendMessage(clearingMsg);
            p.sendMessage(skipMsg);
        }
    }

    private ScheduledTask scheduleGradualSkip(World world, long targetTime, int speed, Runnable onComplete) {
        final long startTime = world.getTime();

        final long totalDistance;
        if (targetTime <= startTime) {

            totalDistance = (24000 - startTime) + targetTime;
        } else {

            totalDistance = targetTime - startTime;
        }

        if (totalDistance <= 0) {
            world.setTime(targetTime);
            if (onComplete != null) onComplete.run();
            return null;
        }

        final int totalSteps = (int) Math.ceil((double) totalDistance / speed);
        final int[] currentStep = {0};

        GradualSkipState state = new GradualSkipState(totalSteps, currentStep, targetTime, startTime, onComplete);
        gradualSkipStates.put(world, state);

        final ScheduledTask[] taskHolder = new ScheduledTask[1];

        final long[] covered = {0};

        taskHolder[0] = SchedulerAdapter.runTaskTimer(plugin, world, () -> {

            long now = world.getTime() % 24000;
            long remaining = targetTime <= now
                ? (24000 - now) + targetTime
                : targetTime - now;

            if (covered[0] >= totalDistance) {
                world.setTime(targetTime);
                clearPhantoms(world);
                gradualSkipStates.remove(world);
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel();
                }
                if (onComplete != null) {
                    onComplete.run();
                }
                return;
            }
            if (getSleepingCount(world) < getRequiredSleepingCount(world) && !isSkipCompleting(world, targetTime)) {
                gradualSkipStates.remove(world);
                cancelSkip(world);
                if (taskHolder[0] != null) taskHolder[0].cancel();
                return;
            }

            long step = Math.min(speed, remaining);
            world.setTime((now + step) % 24000);
            covered[0] += step;

            updateSleepStatus(world);

            BossBar bar = worldBossBars.get(world);
            if (bar != null && totalDistance > 0) {
                bar.setProgress(Math.min(1.0, (double) covered[0] / totalDistance));
            }
        }, 1L, 1L);

        return taskHolder[0];
    }

    public void updateSleepStatus(World world) {
        if (!skipTasks.containsKey(world)) return;

        int total = getRequiredSleepingCount(world);
        int current = getSleepingCount(world);

        BossBar bossBar = worldBossBars.get(world);
        if (bossBar != null) {
            double progress = total > 0 ? Math.min(1.0, (double) current / total) : 1.0;
            bossBar.setProgress(progress);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", skippingPlayerNames.getOrDefault(world, "Players"));
            bossBar.setTitle(configManager.getMessage("boss-bar.title", placeholders));
        }

        if (configManager.isShowProgressBar() || configManager.isShowActionBar()) {
            String bar = "";
            if (configManager.isShowProgressBar()) {
                bar = configManager.buildProgressBar(current, total);
            }

            String actionMsg = configManager.getMessage("sleep.progress-bar", Map.of(
                "bar", bar,
                "count", String.valueOf(current),
                "required", String.valueOf(total)
            ));

            for (Player player : world.getPlayers()) {
                if (isPlayerSleeping(player)) {
                    if (configManager.isShowActionBar()) {
                        try {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionMsg));
                        } catch (NoSuchMethodError | NoClassDefFoundError e) {

                            player.sendMessage(actionMsg);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("removal")
    private void playSound(World world, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName);
            for (Player player : world.getPlayers()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
        }
    }

    private void playSkipSound(World world) {
        playSound(world, configManager.getSkipSound(),
            configManager.getSkipSoundVolume(), configManager.getSkipSoundPitch());
    }

    private void showSkipTitle(World world, String playerName) {
        String title = ChatColor.translateAlternateColorCodes('&',
            configManager.getTitleMessage().replace("%player%", playerName));
        String subtitle = ChatColor.translateAlternateColorCodes('&',
            configManager.getSubtitleMessage().replace("%player%", playerName));

        for (Player player : world.getPlayers()) {
            player.sendTitle(title, subtitle, configManager.getTitleFadeIn(),
                configManager.getTitleStay(), configManager.getTitleFadeOut());
        }
    }

    public Set<UUID> getSleepingPlayers(World world) {
        Set<UUID> players = sleepingPlayers.get(world);
        return players == null ? null : Collections.unmodifiableSet(players);
    }

    private String getSleepingPlayerName(World world) {
        Set<UUID> players = sleepingPlayers.get(world);
        if (players != null && !players.isEmpty()) {
            Player p = Bukkit.getPlayer(players.iterator().next());
            if (p != null) return p.getDisplayName();
        }
        return "Unknown";
    }

    public int getRequiredSleepingCount(World world) {

        if (configManager.isRequireAllPlayersOnline()) {
            if (OfflinePlayerTracker.hasOfflinePlayers()) {
                return Integer.MAX_VALUE;
            }
        }

        int total = getTotalPlayerCount(world);

        int percentage = configManager.getSleepPercentage();
        if (percentage <= 0) return 1;
        if (percentage >= 100) return Math.max(1, total);

        return Math.max(1, (int) Math.ceil(total * percentage / 100.0));
    }

    public int getSleepingCount(World world) {
        Set<UUID> sleeping = sleepingPlayers.get(world);

        int count = 0;

        if (sleeping != null) {
            for (UUID uuid : sleeping) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    count++;
                }
            }
        }

        if (configManager.isCountAfkAsSleeping()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (configManager.isPerWorldSleep() && !player.getWorld().equals(world)) continue;
                if (player.hasPermission("onlysleep.exempt")) continue;
                if (isAfk(player) && (sleeping == null || !sleeping.contains(player.getUniqueId()))) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getTotalPlayerCount(World world) {

        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        int count = 0;
        for (Player player : onlinePlayers) {

            if (configManager.isPerWorldSleep() && !player.getWorld().equals(world)) continue;

            if (player.hasPermission("onlysleep.exempt")) continue;

            if (player.getGameMode() == GameMode.SPECTATOR && !configManager.isCountSpectators()) continue;
            if (player.getGameMode() == GameMode.CREATIVE && configManager.isIgnoreCreativeMode()) continue;

            GameMode gameMode = player.getGameMode();
            if (gameMode != null && configManager.isGameModeDisabled(gameMode.name())) continue;

            if (player.isFlying() && !configManager.isCountFlying()) continue;

            if (configManager.isExcludeAfkFromTotal() && isAfk(player)) continue;

            count++;
        }
        return count;
    }

    private boolean isAfk(Player player) {

        if (AfkTracker.isAfk(player)) {
            return true;
        }

        if (configManager.isUseEssentialsAfk()) {
            if (player.hasMetadata("afk") && !player.getMetadata("afk").isEmpty()
                    && player.getMetadata("afk").get(0).asBoolean()) {
                return true;
            }
        }

        if (configManager.isUseCmiAfk()) {
            try {
                Class<?> cmiClass = Class.forName("com.Zrips.CMI.CMI");
                Object cmi = cmiClass.getMethod("getInstance").invoke(null);
                Object playerManager = cmi.getClass().getMethod("getPlayerManager").invoke(cmi);
                Object cmiUser = playerManager.getClass().getMethod("getUser", Player.class).invoke(playerManager, player);
                if (cmiUser != null) {
                    boolean isAfk = (boolean) cmiUser.getClass().getMethod("isAfk").invoke(cmiUser);
                    if (isAfk) return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    private void showBossBarForWorld(World world) {
        removeBossBar(world);

        BossBar bossBar = Bukkit.createBossBar(
            configManager.getMessage("boss-bar.title", Map.of("player", skippingPlayerNames.getOrDefault(world, "Players"))),
            configManager.getBossBarColor(),
            configManager.getBossBarStyle()
        );

        for (Player player : world.getPlayers()) {
            if (player.hasPermission("onlysleep.exempt")) continue;
            bossBar.addPlayer(player);
        }

        worldBossBars.put(world, bossBar);
    }

    private void removeBossBar(World world) {
        BossBar bar = worldBossBars.remove(world);
        if (bar != null) bar.removeAll();

        ScheduledTask task = bossBarTasks.remove(world);
        if (task != null) task.cancel();
    }

    public void shutdown() {

        skipTasks.values().forEach(ScheduledTask::cancel);
        skipTasks.clear();

        bossBarTasks.values().forEach(ScheduledTask::cancel);
        bossBarTasks.clear();

        worldBossBars.values().forEach(BossBar::removeAll);
        worldBossBars.clear();

        sleepingPlayers.clear();
        activeTransitions.clear();
        gradualSkipStates.clear();
        skippingPlayerNames.clear();
    }

    public void cleanupWorld(World world) {
        restoreGamerule(world);
        cancelSkip(world);
        skippingPlayerNames.remove(world);
        sleepingPlayers.remove(world);
        removeBossBar(world);
    }

    private void clearPhantoms(World world) {
        for (org.bukkit.entity.Phantom phantom : world.getEntitiesByClass(org.bukkit.entity.Phantom.class)) {
            phantom.remove();
        }
    }

    public void forceSkipNight(World world) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        skipNight(world);
    }

    void skipNightForTest(World world) {
        skipNight(world);
    }

    String getSkippingPlayerNameForTest(World world) {
        return skippingPlayerNames.get(world);
    }

    void showBossBarForWorldForTest(World world) {
        showBossBarForWorld(world);
    }

    void clearPhantomsForTest(World world) {
        clearPhantoms(world);
    }

    ScheduledTask scheduleGradualSkipForTest(World world, long targetTime, int speed, Runnable onComplete) {
        return scheduleGradualSkip(world, targetTime, speed, onComplete);
    }

    int getGradualSkipTotalStepsForTest(World world) {
        GradualSkipState state = gradualSkipStates.get(world);
        return state != null ? state.totalSteps() : -1;
    }
}

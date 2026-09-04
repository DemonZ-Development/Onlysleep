package com.demonzdevelopment.onlysleep.fabric;

import com.demonzdevelopment.onlysleep.fabric.api.events.NightSkipEvent;
import com.demonzdevelopment.onlysleep.fabric.api.events.SleepCancelEvent;
import com.demonzdevelopment.onlysleep.fabric.api.events.SleepStartEvent;
import com.demonzdevelopment.onlysleep.fabric.config.FabricConfigManager;
import com.demonzdevelopment.onlysleep.fabric.scheduler.TaskScheduler;
import com.demonzdevelopment.onlysleep.fabric.tracker.AfkTracker;
import com.demonzdevelopment.onlysleep.fabric.tracker.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.fabric.util.LegacyText;
import com.demonzdevelopment.onlysleep.fabric.util.NightMath;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.WeatherData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SleepManager {

    private static final int DAY_LENGTH = 24000;

    private final OnlysleepMod mod;
    private final FabricConfigManager config;
    private final TaskScheduler scheduler;

    private final Map<ServerLevel, Set<UUID>> sleepingPlayers = new ConcurrentHashMap<>();
    private final Map<ServerLevel, TaskScheduler.Task> skipTasks = new ConcurrentHashMap<>();
    private final Map<ServerLevel, ServerBossEvent> worldBossBars = new ConcurrentHashMap<>();
    private final Map<ServerLevel, TaskScheduler.Task> worldBossBarTasks = new ConcurrentHashMap<>();
    private final Set<ServerLevel> activeTransitions = ConcurrentHashMap.newKeySet();
    private final Map<ServerLevel, String> skippingPlayerNames = new ConcurrentHashMap<>();
    private final Map<ServerLevel, Integer> originalGameruleValues = new ConcurrentHashMap<>();

    public SleepManager(OnlysleepMod mod) {
        this.mod = mod;
        this.config = mod.config();
        this.scheduler = mod.scheduler();
    }

    public void applyGamerules(net.minecraft.server.MinecraftServer server) {
        if (!config.isManageGamerule()) {
            for (ServerLevel level : new HashSet<>(originalGameruleValues.keySet())) {
                restoreGamerule(level);
            }
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            applyGamerule(level);
        }
    }

    public void applyGamerule(ServerLevel level) {
        if (!config.isManageGamerule() || !config.isWorldEnabled(worldKey(level))) {
            restoreGamerule(level);
            return;
        }

        originalGameruleValues.putIfAbsent(level, currentSleepPercentage(level));
        level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, 101, level.getServer());
    }

    public void restoreGamerules() {
        for (ServerLevel level : new HashSet<>(originalGameruleValues.keySet())) {
            restoreGamerule(level);
        }
    }

    private void restoreGamerule(ServerLevel level) {
        Integer original = originalGameruleValues.remove(level);
        if (original == null) return;
        try {
            level.getGameRules().set(GameRules.PLAYERS_SLEEPING_PERCENTAGE, original, level.getServer());
        } catch (Exception e) {
            mod.logger().warn("Could not restore gamerule for {}: {}", worldKey(level), e.getMessage());
        }
    }

    private int currentSleepPercentage(ServerLevel level) {
        return level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
    }

    private Holder<WorldClock> overworldClock(ServerLevel anyLevel) {
        return anyLevel.getServer().registryAccess()
            .lookupOrThrow(Registries.WORLD_CLOCK)
            .getOrThrow(WorldClocks.OVERWORLD);
    }

    public long dayTimeOf(ServerLevel level) {
        try {
            long total = ((ServerClockManager) level.clockManager()).getTotalTicks(overworldClock(level));
            return Math.floorMod(total, DAY_LENGTH);
        } catch (Exception e) {
            return 18000L;
        }
    }

    public void setDayTime(ServerLevel level, long targetTickOfDay) {
        try {
            ServerClockManager clocks = (ServerClockManager) level.clockManager();
            Holder<WorldClock> clock = overworldClock(level);
            long total = clocks.getTotalTicks(clock);
            long currentPos = Math.floorMod(total, DAY_LENGTH);
            long delta = NightMath.distanceTo(currentPos, targetTickOfDay);
            clocks.addTicks(clock, (int) Math.min(Integer.MAX_VALUE, delta));
        } catch (Exception e) {
            mod.logger().warn("Failed to set time: {}", e.getMessage());
        }
    }

    public void wakeFromSleep(ServerLevel level) {
        try {
            ServerClockManager clocks = (ServerClockManager) level.clockManager();
            clocks.moveToTimeMarker(overworldClock(level), ClockTimeMarkers.WAKE_UP_FROM_SLEEP);
        } catch (Exception e) {
            setDayTime(level, 0);
        }
    }

    public void onBedEnter(ServerPlayer player) {
        ServerLevel level = player.level();
        if (!config.isWorldEnabled(worldKey(level))) return;
        if (isSkipScheduled(level)) {
            player.sendSystemMessage(config.getMessage("sleep.already-skipping"));
            return;
        }

        String gamemode = player.gameMode.getGameModeForPlayer().name();
        if (config.isGameModeDisabled(gamemode)) return;
        if (gamemode.equals("CREATIVE") && config.isIgnoreCreativeMode()) return;

        sleepingPlayers.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet()).add(player.getUUID());


        try {
            int cur = getSleepingCount(level);
            int req = getRequiredSleepingCount(level);
            SleepStartEvent ev = new SleepStartEvent(player, cur, req);
            SleepStartEvent.EVENT.invoker().onSleepStart(ev);
            if (ev.isCancelled()) {
                var set = sleepingPlayers.get(level);
                if (set != null) { set.remove(player.getUUID()); if (set.isEmpty()) sleepingPlayers.remove(level); }
                return;
            }
        } catch (RuntimeException exception) {
            mod.logger().warn("SleepStartEvent listener failed", exception);
        }

        Map<String, String> ph = new HashMap<>();
        ph.put("player", player.getName().getString());
        ph.put("count", String.valueOf(getSleepingCount(level)));
        ph.put("required", String.valueOf(getRequiredSleepingCount(level)));

        MutableComponent message = config.getMessage("sleep.start-sleep", ph);
        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(message);
        }

        playSound(level, config.getNightSound(), config.getNightSoundVolume(), config.getNightSoundPitch());

        checkSleepStatus(level);
    }

    public void onBedLeave(ServerPlayer player) {
        ServerLevel level = player.level();
        removeSleeper(level, player.getUUID());

        if (activeTransitions.contains(level)) {
            if (skipTasks.containsKey(level) && getSleepingCount(level) < getRequiredSleepingCount(level)) {
                fireSleepCancel(player, SleepCancelEvent.Cause.BED_LEAVE);
                broadcastCancelled(level, player.getName().getString());
            }
            return;
        }

        if (skipTasks.containsKey(level)) {
            if (getSleepingCount(level) < getRequiredSleepingCount(level)) {
                fireSleepCancel(player, SleepCancelEvent.Cause.BED_LEAVE);
                broadcastCancelled(level, player.getName().getString());
            }
        }
    }

    public void onQuit(ServerPlayer player) {
        ServerLevel level = player.level();
        removeSleeper(level, player.getUUID());

        if (activeTransitions.contains(level)) {
            if (skipTasks.containsKey(level) && getSleepingCount(level) < getRequiredSleepingCount(level)) {
                fireSleepCancel(player, SleepCancelEvent.Cause.QUIT);
                cancelSkip(level);
            }
            return;
        }

        if (skipTasks.containsKey(level)) {
            if (getSleepingCount(level) < getRequiredSleepingCount(level)) {
                fireSleepCancel(player, SleepCancelEvent.Cause.QUIT);
                cancelSkip(level);
            }
        } else {
            checkSleepStatus(level);
        }
    }

    private void removeSleeper(ServerLevel level, UUID uuid) {
        Set<UUID> players = sleepingPlayers.get(level);
        if (players != null) {
            players.remove(uuid);
            if (players.isEmpty()) sleepingPlayers.remove(level);
        }
    }

    private void fireSleepCancel(ServerPlayer player, SleepCancelEvent.Cause cause) {
        ServerLevel level = player.level();
        try {
            SleepCancelEvent event = new SleepCancelEvent(
                player,
                cause,
                getSleepingCount(level),
                getRequiredSleepingCount(level)
            );
            SleepCancelEvent.EVENT.invoker().onSleepCancel(event);
        } catch (RuntimeException exception) {
            mod.logger().warn("SleepCancelEvent listener failed", exception);
        }
    }

    private void broadcastCancelled(ServerLevel level, String playerName) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", playerName);
        MutableComponent message = config.getMessage("sleep.cancelled", ph);
        for (ServerPlayer p : level.players()) {
            p.sendSystemMessage(message);
        }
        cancelSkip(level);
    }

    public boolean isSkipScheduled(ServerLevel level) {
        return skipTasks.containsKey(level);
    }

    public boolean isPlayerSleeping(ServerPlayer player) {
        Set<UUID> players = sleepingPlayers.get(player.level());
        return players != null && players.contains(player.getUUID());
    }

    private void checkSleepStatus(ServerLevel level) {
        if (skipTasks.containsKey(level)) return;

        int required = getRequiredSleepingCount(level);
        int current = getSleepingCount(level);

        if (current >= required && required > 0) {
            scheduleSkip(level);
        }
    }

    private void scheduleSkip(ServerLevel level) {
        skippingPlayerNames.put(level, getSleepingPlayerName(level));
        if (config.isShowBossBar()) {
            showBossBarForWorld(level);
        }

        TaskScheduler.Task task = scheduler.runLater(config.getSkipDelayTicks(), () -> skipNight(level));
        skipTasks.put(level, task);
    }

    private void cancelSkip(ServerLevel level) {
        activeTransitions.remove(level);
        TaskScheduler.Task task = skipTasks.remove(level);
        if (task != null) task.cancel();
        skippingPlayerNames.remove(level);
        removeBossBar(level);
    }

    public void forceSkipNight(ServerLevel level) {
        if (level == null) throw new IllegalArgumentException("level cannot be null");
        skipNight(level);
    }

    private void skipNight(ServerLevel level) {
        try {
            ServerPlayer initiator = null;
            var sleeping = sleepingPlayers.get(level);
            if (sleeping != null && !sleeping.isEmpty()) initiator = level.getServer().getPlayerList().getPlayer(sleeping.iterator().next());
            NightSkipEvent ev = new NightSkipEvent(
                level,
                initiator,
                getSleepingCount(level),
                getRequiredSleepingCount(level),
                getTotalPlayerCount(level)
            );
            NightSkipEvent.EVENT.invoker().onNightSkip(ev);
            if (ev.isCancelled()) { cancelSkip(level); return; }
        } catch (RuntimeException exception) {
            mod.logger().warn("NightSkipEvent listener failed", exception);
        }

        WeatherData weather = level.getServer().getWeatherData();

        boolean clearedRain = false;
        if (config.isClearWeather() && level.isRaining() && weather.isRaining()) {
            if (config.isResetWeather()) {
                weather.setRaining(false);
                clearedRain = true;
                if (config.isResetWeatherCycle()) {
                    weather.setRainTime(Integer.MAX_VALUE);
                }
            }
        }

        boolean clearedThunder = false;
        if (config.isClearThunder() && level.isThundering() && weather.isThundering()) {
            if (config.isResetThunder()) {
                weather.setThundering(false);
                clearedThunder = true;
                if (config.isResetWeatherCycle()) {
                    weather.setThunderTime(Integer.MAX_VALUE);
                }
            }
        }

        final boolean weatherWasCleared = clearedRain || clearedThunder;

        final String playerName = getSleepingPlayerName(level);
        skippingPlayerNames.put(level, playerName);

        Runnable onSkipComplete = () -> {
            activeTransitions.remove(level);

            if (config.isPlaySounds()) {
                if (weatherWasCleared) {
                    playSound(level, config.getStormSound(),
                        config.getStormSoundVolume(), config.getStormSoundPitch());
                }
                playSound(level, config.getSkipSound(),
                    config.getSkipSoundVolume(), config.getSkipSoundPitch());
            }

            if (config.isShowTitle()) {
                showSkipTitle(level, playerName);
            }

            scheduler.runSync(() -> broadcastSkipMessages(level, playerName, weatherWasCleared));

            sleepingPlayers.remove(level);
            skipTasks.remove(level);
            removeBossBar(level);
        };

        String skipType = config.getSkipType();
        long targetTime = config.isResetTime() ? 0 : config.getMorningTime();

        switch (skipType.toLowerCase()) {
            case "gradual" -> {
                activeTransitions.add(level);
                TaskScheduler.Task gradualTask =
                    scheduleGradualSkip(level, targetTime, config.getGradualSkipSpeedTicks(), onSkipComplete);
                if (gradualTask != null) skipTasks.put(level, gradualTask);
            }
            case "speed" -> {
                activeTransitions.add(level);
                TaskScheduler.Task speedTask =
                    scheduleGradualSkip(level, targetTime, 150, onSkipComplete);
                if (speedTask != null) skipTasks.put(level, speedTask);
            }
            default -> {
                finishSkipInstant(level, targetTime);
                onSkipComplete.run();
            }
        }
    }

    private void finishSkipInstant(ServerLevel level, long targetTime) {
        if (targetTime == 0) {
            wakeFromSleep(level);
        } else {
            setDayTime(level, targetTime);
        }
        wakeUpSleepers(level);
        clearPhantoms(level);
    }

    private void broadcastSkipMessages(ServerLevel level, String playerName, boolean clearedWeather) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", playerName);

        MutableComponent clearingMsg = clearedWeather
            ? config.getMessage("weather.clearing", ph) : null;
        MutableComponent skipMsg = config.getMessage("sleep.enough-sleeping", ph);

        for (ServerPlayer p : level.players()) {
            if (clearingMsg != null) p.sendSystemMessage(clearingMsg);
            p.sendSystemMessage(skipMsg);
        }
    }

    private TaskScheduler.Task scheduleGradualSkip(ServerLevel level, long targetTime,
                                                   int speed, Runnable onComplete) {
        final long startTime = dayTimeOf(level);
        final long totalDistance = NightMath.distanceTo(startTime, targetTime);

        if (totalDistance <= 0) {
            finishSkipInstant(level, targetTime);
            if (onComplete != null) onComplete.run();
            return null;
        }

        final int totalSteps = NightMath.gradualSteps(totalDistance, speed);
        final long[] covered = {0};

        final TaskScheduler.Task[] taskHolder = new TaskScheduler.Task[1];

        taskHolder[0] = scheduler.runTimer(1L, 1L, () -> {
            long now = dayTimeOf(level);
            long remaining = NightMath.distanceTo(now, targetTime);

            if (remaining <= 0) {
                finishSkipInstant(level, targetTime);
                if (taskHolder[0] != null) taskHolder[0].cancel();
                if (onComplete != null) onComplete.run();
                return;
            }
            if (getSleepingCount(level) < getRequiredSleepingCount(level) && remaining > speed) {
                cancelSkip(level);
                if (taskHolder[0] != null) taskHolder[0].cancel();
                return;
            }

            long step = Math.min(speed, remaining);
            setDayTime(level, (now + step) % DAY_LENGTH);
            covered[0] += step;

            updateSleepStatus(level);

            ServerBossEvent bar = worldBossBars.get(level);
            if (bar != null && totalDistance > 0) {
                bar.setProgress((float) Math.min(1.0, (double) covered[0] / totalDistance));
            }
        });

        return taskHolder[0];
    }

    private void wakeUpSleepers(ServerLevel level) {
        for (ServerPlayer p : level.players()) {
            if (p.isSleeping()) {
                p.stopSleeping();
            }
        }
    }

    public void updateSleepStatus(ServerLevel level) {
        if (!skipTasks.containsKey(level)) return;

        int total = getRequiredSleepingCount(level);
        int current = getSleepingCount(level);

        ServerBossEvent bossBar = worldBossBars.get(level);
        if (bossBar != null) {
            float progress = total > 0 ? (float) Math.min(1.0, (double) current / total) : 1.0f;
            bossBar.setProgress(progress);

            Map<String, String> ph = new HashMap<>();
            ph.put("player", skippingPlayerNames.getOrDefault(level, "Players"));
            bossBar.setName(config.getMessage("boss-bar.title", ph));
        }

        if (!config.isShowActionBar()) return;

        String bar = "";
        if (config.isShowProgressBar()) {
            bar = config.buildProgressBar(current, total);
        }

        MutableComponent actionMsg = config.getMessage("sleep.progress-bar", Map.of(
            "bar", bar,
            "count", String.valueOf(current),
            "required", String.valueOf(total)
        ));

        for (ServerPlayer player : level.players()) {
            if (isPlayerSleeping(player)) {
                player.sendSystemMessage(actionMsg, true);
            }
        }
    }

    private void playSound(ServerLevel level, String soundId, float volume, float pitch) {
        SoundEvent sound = resolveSound(soundId);
        if (sound == null) {
            mod.logger().warn("Invalid sound: {}", soundId);
            return;
        }
        for (ServerPlayer player : level.players()) {
            player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    static SoundEvent resolveSound(String soundId) {
        try {
            Identifier id = Identifier.parse(soundId.toLowerCase());
            if (net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.containsKey(id)) {
                return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(id);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void showSkipTitle(ServerLevel level, String playerName) {
        MutableComponent title = LegacyText.of(
            config.getTitleMessage().replace("%player%", playerName));
        MutableComponent subtitle = LegacyText.of(
            config.getSubtitleMessage().replace("%player%", playerName));

        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(
                config.getTitleFadeIn(), config.getTitleStay(), config.getTitleFadeOut()));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void showBossBarForWorld(ServerLevel level) {
        removeBossBar(level);

        Map<String, String> ph = new HashMap<>();
        ph.put("player", skippingPlayerNames.getOrDefault(level, "Players"));

        ServerBossEvent bossBar = new ServerBossEvent(
            UUID.randomUUID(),
            config.getMessage("boss-bar.title", ph),
            parseBarColor(config.getBossBarColor()),
            parseBarOverlay(config.getBossBarStyle())
        );
        bossBar.setProgress(0.0f);

        for (ServerPlayer player : level.players()) {
            if (mod.permissions().isExempt(player)) continue;
            bossBar.addPlayer(player);
        }

        worldBossBars.put(level, bossBar);

        TaskScheduler.Task bossTask = scheduler.runTimer(1L, 10L, () -> {
            if (!skipTasks.containsKey(level)) {
                removeBossBar(level);
                return;
            }
            updateSleepStatus(level);
        });

        worldBossBarTasks.put(level, bossTask);
    }

    private void removeBossBar(ServerLevel level) {
        ServerBossEvent bar = worldBossBars.remove(level);
        if (bar != null) {
            bar.removeAllPlayers();
            bar.setVisible(false);
        }

        TaskScheduler.Task task = worldBossBarTasks.remove(level);
        if (task != null) task.cancel();
    }

    private static BossEvent.BossBarColor parseBarColor(String name) {
        try {
            return BossEvent.BossBarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BossEvent.BossBarColor.BLUE;
        }
    }

    private static BossEvent.BossBarOverlay parseBarOverlay(String name) {
        return switch (name.toUpperCase()) {
            case "SEGMENTED_6", "NOTCHED_6" -> BossEvent.BossBarOverlay.NOTCHED_6;
            case "SEGMENTED_10", "NOTCHED_10" -> BossEvent.BossBarOverlay.NOTCHED_10;
            case "SEGMENTED_12", "NOTCHED_12" -> BossEvent.BossBarOverlay.NOTCHED_12;
            case "SEGMENTED_20", "NOTCHED_20" -> BossEvent.BossBarOverlay.NOTCHED_20;
            default -> BossEvent.BossBarOverlay.PROGRESS;
        };
    }

    public Set<UUID> getSleepingPlayers(ServerLevel level) {
        Set<UUID> players = sleepingPlayers.get(level);
        return players == null ? null : Set.copyOf(players);
    }

    private String getSleepingPlayerName(ServerLevel level) {
        Set<UUID> players = sleepingPlayers.get(level);
        if (players != null && !players.isEmpty()) {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(players.iterator().next());
            if (p != null) return p.getName().getString();
        }
        return "Unknown";
    }

    public int getRequiredSleepingCount(ServerLevel level) {
        if (config.isRequireAllPlayersOnline() && OfflinePlayerTracker.hasOfflinePlayers()) {
            return Integer.MAX_VALUE;
        }
        int total = getTotalPlayerCount(level);
        return NightMath.requiredSleepers(total, config.getSleepPercentage());
    }

    public int getSleepingCount(ServerLevel level) {
        Set<UUID> sleeping = sleepingPlayers.get(level);

        int count = 0;
        if (sleeping != null) {
            for (UUID uuid : sleeping) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(uuid);
                if (player != null && !player.hasDisconnected()) count++;
            }
        }

        if (config.isCountAfkAsSleeping()) {
            for (ServerPlayer player : eligiblePlayers(level)) {
                if (mod.permissions().isExempt(player)) continue;
                boolean alreadyCounted = sleeping != null && sleeping.contains(player.getUUID());
                if (AfkTracker.isAfk(player) && !alreadyCounted) count++;
            }
        }

        return count;
    }

    public int getTotalPlayerCount(ServerLevel level) {
        int count = 0;
        for (ServerPlayer player : eligiblePlayers(level)) {
            String gamemode = player.gameMode.getGameModeForPlayer().name();
            if (gamemode.equals("SPECTATOR") && !config.isCountSpectators()) continue;
            if (gamemode.equals("CREATIVE") && config.isIgnoreCreativeMode()) continue;
            if (config.isGameModeDisabled(gamemode)) continue;
            if (player.getAbilities().flying && !config.isCountFlying()) continue;
            if (config.isExcludeAfkFromTotal() && AfkTracker.isAfk(player)) continue;
            count++;
        }
        return count;
    }

    private Iterable<ServerPlayer> eligiblePlayers(ServerLevel level) {
        if (config.isPerWorldSleep()) {
            return level.players();
        }
        return level.getServer().getPlayerList().getPlayers();
    }

    public void cleanupWorld(ServerLevel level) {
        restoreGamerule(level);
        cancelSkip(level);
        skippingPlayerNames.remove(level);
        sleepingPlayers.remove(level);
        removeBossBar(level);
    }

    public void shutdown() {
        skipTasks.values().forEach(TaskScheduler.Task::cancel);
        skipTasks.clear();
        worldBossBarTasks.values().forEach(TaskScheduler.Task::cancel);
        worldBossBarTasks.clear();
        worldBossBars.values().forEach(bar -> {
            bar.removeAllPlayers();
            bar.setVisible(false);
        });
        worldBossBars.clear();
        sleepingPlayers.clear();
        activeTransitions.clear();
        skippingPlayerNames.clear();
        originalGameruleValues.clear();
    }

    private void clearPhantoms(ServerLevel level) {
        List<Phantom> phantoms = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Phantom phantom && phantom.isAlive()) {
                phantoms.add(phantom);
            }
        }
        phantoms.forEach(Entity::discard);
    }

    public static String worldKey(ServerLevel level) {
        return level.dimension().identifier().toString();
    }
}

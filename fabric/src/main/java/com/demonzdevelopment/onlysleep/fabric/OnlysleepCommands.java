package com.demonzdevelopment.onlysleep.fabric;

import com.demonzdevelopment.onlysleep.fabric.tracker.AfkTracker;
import com.demonzdevelopment.onlysleep.fabric.tracker.OfflinePlayerTracker;
import com.demonzdevelopment.onlysleep.fabric.util.LegacyText;
import com.demonzdevelopment.onlysleep.fabric.util.UpdateChecker;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlysleepCommands {

    private final OnlysleepMod mod;

    private static final List<String> SET_KEYS = List.of(
        "percentage", "sleep-percentage",
        "skiptype", "skip-type",
        "perworld", "per-world-sleep",
        "skipdelay", "skip-delay-ticks",
        "morningtime", "morning-time",
        "resettime", "reset-time",
        "gradualspeed", "gradual-skip-speed-ticks",
        "clearweather", "clear-weather",
        "clearthunder", "clear-thunder",
        "resetweather", "reset-weather",
        "resetthunder", "reset-thunder",
        "bossbar", "actionbar", "progressbar", "title", "sounds",
        "managegamerule", "manage-gamerule",
        "afktime", "countafk", "excludeafk"
    );
    private static final List<String> TOGGLE_KEYS = List.of(
        "perworld", "per-world-sleep",
        "resettime", "reset-time",
        "clearweather", "clear-weather",
        "clearthunder", "clear-thunder",
        "resetweather", "reset-weather",
        "resetthunder", "reset-thunder",
        "bossbar", "actionbar", "progressbar", "title", "sounds",
        "managegamerule", "manage-gamerule",
        "countafk", "count-afk-as-sleeping",
        "excludeafk", "exclude-afk-from-total"
    );

    public OnlysleepCommands(OnlysleepMod mod) {
        this.mod = mod;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String root : new String[]{"onlysleep", "os", "sleep"}) {
            dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal(root)
                .executes(ctx -> { sendHelp(ctx.getSource()); return Command.SINGLE_SUCCESS; })
                .then(sub("reload"))
                .then(sub("info"))
                .then(sub("status"))
                .then(sub("update"))
                .then(sub("help"))
                .then(setCommand())
                .then(getCommand())
                .then(toggleCommand())
                .then(worldCommand())
                .then(gamemodeCommand())
                .then(dumpCommand())
            );
        }
    }

    private LiteralArgumentBuilder<CommandSourceStack> sub(String name) {
        return Commands.literal(name)
            .executes(ctx -> {
                CommandSourceStack src = ctx.getSource();
                switch (name) {
                    case "reload" -> reload(src);
                    case "info" -> info(src);
                    case "status" -> status(src);
                    case "update" -> update(src);
                    default -> sendHelp(src);
                }
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> setCommand() {
        return Commands.literal("set")
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests(suggest(SET_KEYS))
                .then(Commands.argument("value", StringArgumentType.greedyString())
                    .suggests((ctx, b) -> {
                        String key = StringArgumentType.getString(ctx, "key").toLowerCase();
                        List<String> vals = switch (key) {
                            case "skiptype", "skip-type" -> List.of("instant", "gradual", "speed");
                            case "perworld", "per-world-sleep", "resettime", "reset-time",
                                 "clearweather", "clear-weather", "clearthunder", "clear-thunder",
                                 "resetweather", "reset-weather", "resetthunder", "reset-thunder",
                                 "bossbar", "actionbar", "progressbar", "title", "sounds",
                                 "managegamerule", "manage-gamerule", "countafk", "excludeafk" -> List.of("true", "false");
                            case "percentage", "sleep-percentage" -> List.of("0", "25", "50", "75", "100");
                            default -> List.of("0", "30", "60", "100", "1000");
                        };
                        return SharedSuggestionProvider.suggest(vals, b);
                    })
                    .executes(ctx -> {
                        String key = StringArgumentType.getString(ctx, "key");
                        String value = StringArgumentType.getString(ctx, "value");
                        handleSet(ctx.getSource(), key, value);
                        return Command.SINGLE_SUCCESS;
                    }))
                .executes(ctx -> {
                    send(ctx.getSource(), LegacyText.of("&cUsage: /onlysleep set <option> <value>"));
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("get")
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests(suggest(SET_KEYS))
                .executes(ctx -> {
                    handleGet(ctx.getSource(), StringArgumentType.getString(ctx, "key"));
                    return Command.SINGLE_SUCCESS;
                }))
            .executes(ctx -> {
                handleGet(ctx.getSource(), "");
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> toggleCommand() {
        return Commands.literal("toggle")
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests(suggest(TOGGLE_KEYS))
                .executes(ctx -> {
                    handleToggle(ctx.getSource(), StringArgumentType.getString(ctx, "key"));
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> worldCommand() {
        return Commands.literal("world")
            .then(Commands.literal("list").executes(ctx -> { handleWorld(ctx.getSource(), "list", null); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("enable").then(Commands.argument("world", StringArgumentType.word()).suggests((ctx,b)-> SharedSuggestionProvider.suggest(mod.server()!=null ? java.util.stream.StreamSupport.stream(mod.server().getAllLevels().spliterator(), false).map(l -> SleepManager.worldKey(l)).toList() : List.of(), b)).executes(ctx -> { handleWorld(ctx.getSource(), "enable", StringArgumentType.getString(ctx, "world")); return Command.SINGLE_SUCCESS; })))
            .then(Commands.literal("disable").then(Commands.argument("world", StringArgumentType.word()).suggests((ctx,b)-> SharedSuggestionProvider.suggest(mod.server()!=null ? java.util.stream.StreamSupport.stream(mod.server().getAllLevels().spliterator(), false).map(l -> SleepManager.worldKey(l)).toList() : List.of(), b)).executes(ctx -> { handleWorld(ctx.getSource(), "disable", StringArgumentType.getString(ctx, "world")); return Command.SINGLE_SUCCESS; })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> gamemodeCommand() {
        return Commands.literal("gamemode")
            .then(Commands.literal("list").executes(ctx -> { handleGamemode(ctx.getSource(), "list", null); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("enable").then(Commands.argument("gm", StringArgumentType.word()).suggests(suggest(List.of("survival","creative","adventure","spectator"))).executes(ctx -> { handleGamemode(ctx.getSource(), "enable", StringArgumentType.getString(ctx, "gm")); return Command.SINGLE_SUCCESS; })))
            .then(Commands.literal("disable").then(Commands.argument("gm", StringArgumentType.word()).suggests(suggest(List.of("survival","creative","adventure","spectator"))).executes(ctx -> { handleGamemode(ctx.getSource(), "disable", StringArgumentType.getString(ctx, "gm")); return Command.SINGLE_SUCCESS; })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> dumpCommand() {
        return Commands.literal("dump")
            .executes(ctx -> { handleDump(ctx.getSource(), false); return Command.SINGLE_SUCCESS; })
            .then(Commands.literal("paste").executes(ctx -> { handleDump(ctx.getSource(), true); return Command.SINGLE_SUCCESS; }))
            .then(Commands.literal("upload").executes(ctx -> { handleDump(ctx.getSource(), true); return Command.SINGLE_SUCCESS; }));
    }

    private SuggestionProvider<CommandSourceStack> suggest(List<String> opts) {
        return (ctx, builder) -> SharedSuggestionProvider.suggest(opts, builder);
    }

    private void sendHelp(CommandSourceStack src) {
        send(src, mod.config().getMessage("help.header", Map.of("cmd", "onlysleep")));
        send(src, mod.config().getMessage("help.reload", Map.of("cmd", "onlysleep")));
        send(src, mod.config().getMessage("help.info", Map.of("cmd", "onlysleep")));
        send(src, mod.config().getMessage("help.status", Map.of("cmd", "onlysleep")));
        send(src, mod.config().getMessage("help.update", Map.of("cmd", "onlysleep")));
        send(src, LegacyText.of("&7/onlysleep set <option> <value> &8- &bChange config"));
        send(src, LegacyText.of("&7/onlysleep get <option> &8- &bView config"));
        send(src, LegacyText.of("&7/onlysleep toggle <option> &8- &bToggle boolean"));
        send(src, LegacyText.of("&7/onlysleep world <enable|disable|list> [world] &8- &bManage disabled worlds"));
        send(src, LegacyText.of("&7/onlysleep gamemode <enable|disable|list> [type] &8- &bManage disabled gamemodes"));
        send(src, LegacyText.of("&7/onlysleep dump [paste] &8- &bCreate debug dump"));
        send(src, mod.config().getMessage("help.help", Map.of("cmd", "onlysleep")));
        send(src, mod.config().getMessage("help.footer", Map.of("cmd", "onlysleep")));
        if (mod.permissions().canConfig(src)) {
            send(src, LegacyText.of("&8--- &bSet Options &8---"));
            send(src, LegacyText.of("&7percentage &8<0-100> &7skiptype &8<instant|gradual|speed> &7perworld &8<true|false>"));
            send(src, LegacyText.of("&7skipdelay &8<ticks> &7morningtime &8<ticks> &7gradualspeed &8<ticks>"));
            send(src, LegacyText.of("&7clearweather &7clearthunder &7bossbar &7actionbar &7progressbar &7title &7sounds"));
        }
    }

    private void reload(CommandSourceStack src) {
        if (!mod.permissions().canReload(src)) {
            send(src, mod.config().getMessage("command.no-permission"));
            return;
        }

        try {
            mod.config().reload();
            if (mod.server() != null) {
                mod.sleepManager().applyGamerules(mod.server());
            }

            AfkTracker.shutdown();
            int afkSeconds = mod.config().getAfkTimeSeconds();
            if (afkSeconds > 0) {
                AfkTracker.init(afkSeconds);
            }
            OfflinePlayerTracker.shutdown();
            if (mod.config().isRequireAllPlayersOnline() && mod.server() != null) {
                OfflinePlayerTracker.init(mod.server());
            }

            send(src, mod.config().getMessage("command.reload-success"));
            mod.logger().info("Configuration reloaded by {}", src.getTextName());
        } catch (Exception e) {
            mod.logger().error("Failed to reload config: {}", e.getMessage());
            send(src, mod.config().getMessage("command.reload-fail"));
        }
    }

    private void info(CommandSourceStack src) {
        if (!mod.permissions().canViewInfo(src)) {
            send(src, mod.config().getMessage("command.no-permission"));
            return;
        }

        String version = mod.version();

        Map<String, String> ph = new HashMap<>();
        ph.put("version", version);
        ph.put("author", "Demonz Development");
        ph.put("percent", String.valueOf(mod.config().getSleepPercentage()));
        ph.put("platform", "Fabric");
        ph.put("mcversion", mod.minecraftVersion());

        send(src, mod.config().getMessage("command.info.header", ph));
        send(src, mod.config().getMessage("command.info.version", ph));
        send(src, mod.config().getMessage("command.info.author", ph));
        send(src, mod.config().getMessage("command.info.status-enabled", ph));
        send(src, mod.config().getMessage("command.info.sleep-pct", ph));
        send(src, mod.config().getMessage("command.info.platform", ph));
        send(src, mod.config().getMessage("command.info.links-header"));
        send(src, mod.config().getMessage("command.info.link-modrinth"));
        send(src, mod.config().getMessage("command.info.link-github"));
        send(src, mod.config().getMessage("command.info.link-website"));
        send(src, mod.config().getMessage("command.info.link-discord"));
        send(src, mod.config().getMessage("command.info.footer", ph));
    }

    private void status(CommandSourceStack src) {
        if (!mod.permissions().canViewStatus(src)) {
            send(src, mod.config().getMessage("command.no-permission"));
            return;
        }

        send(src, LegacyText.of("&8=== &bOnlysleep Status &8==="));
        send(src, LegacyText.of("&7Platform: &bFabric &7" + mod.minecraftVersion()));
        send(src, LegacyText.of("&7Version: &b" + mod.version()));
        send(src, LegacyText.of("&7Sleep Required: &b" + mod.config().getSleepPercentage() + "%"));
        send(src, LegacyText.of("&7Skip Type: &b" + mod.config().getSkipType()));
        send(src, LegacyText.of("&8========================"));
    }

    private void update(CommandSourceStack src) {
        if (!mod.permissions().canCheckUpdates(src)) {
            send(src, mod.config().getMessage("command.no-permission"));
            return;
        }

        send(src, mod.config().getMessage("update.checking"));

        UpdateChecker checker = mod.updateChecker();
        if (checker == null) {
            send(src, mod.config().getMessage("update.check-fail"));
            return;
        }

        checker.checkAsync().thenAccept(result ->
            mod.scheduler().runSync(() -> {
                if (result.updateAvailable()) {
                    Map<String, String> ph = new HashMap<>();
                    ph.put("new", result.latestVersion());
                    ph.put("current", mod.version());
                    send(src, mod.config().getMessage("update.available", ph));
                } else {
                    send(src, mod.config().getMessage("update.current"));
                }
            })
        ).exceptionally(t -> {
            mod.scheduler().runSync(() ->
                send(src, mod.config().getMessage("update.check-fail")));
            return null;
        });
    }



    private void handleSet(CommandSourceStack src, String key, String value) {
        if (!mod.permissions().canConfig(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        try {
            boolean ok = applySet(key, value);
            if (ok) {
                if (mod.server() != null) mod.sleepManager().applyGamerules(mod.server());
                send(src, LegacyText.of("&aSet &b" + key + " &ato &b" + value));
                mod.logger().info("{} set {} = {}", src.getTextName(), key, value);
            }
        } catch (IllegalArgumentException e) { send(src, LegacyText.of("&c" + e.getMessage())); }
        catch (Exception e) { send(src, LegacyText.of("&cFailed: " + e.getMessage())); }
    }

    private boolean applySet(String key, String value) {
        String k = key.toLowerCase();
        return switch (k) {
            case "percentage", "sleep-percentage" -> {
                int pct = Integer.parseInt(value);
                if (pct < 0 || pct > 100) throw new IllegalArgumentException("Percentage must be 0-100");
                if (!mod.config().setSleepPercentage(pct)) throw new IllegalArgumentException("Failed");
                yield true;
            }
            case "skiptype", "skip-type" -> {
                if (!mod.config().setSkipType(value)) throw new IllegalArgumentException("Skip type must be instant, gradual or speed");
                yield true;
            }
            case "perworld", "per-world-sleep" -> { mod.config().setPerWorldSleep(parseBool(value)); yield true; }
            case "skipdelay", "skip-delay-ticks" -> { mod.config().setSkipDelayTicks(parseInt(value)); yield true; }
            case "morningtime", "morning-time" -> { mod.config().setMorningTime(parseInt(value)); yield true; }
            case "resettime", "reset-time" -> { mod.config().setResetTime(parseBool(value)); yield true; }
            case "gradualspeed", "gradual-skip-speed-ticks" -> { mod.config().setGradualSkipSpeedTicks(parseInt(value)); yield true; }
            case "clearweather", "clear-weather" -> { mod.config().setClearWeather(parseBool(value)); yield true; }
            case "clearthunder", "clear-thunder" -> { mod.config().setClearThunder(parseBool(value)); yield true; }
            case "resetweather", "reset-weather" -> { mod.config().setResetWeather(parseBool(value)); yield true; }
            case "resetthunder", "reset-thunder" -> { mod.config().setResetThunder(parseBool(value)); yield true; }
            case "bossbar" -> { mod.config().setValue("ui.boss-bar.enabled", parseBool(value)); yield true; }
            case "actionbar" -> { mod.config().setValue("ui.action-bar.enabled", parseBool(value)); yield true; }
            case "progressbar" -> { mod.config().setValue("ui.progress-bar.enabled", parseBool(value)); yield true; }
            case "title" -> { mod.config().setValue("ui.title.enabled", parseBool(value)); yield true; }
            case "sounds" -> { mod.config().setValue("sounds.enabled", parseBool(value)); yield true; }
            case "managegamerule", "manage-gamerule" -> { mod.config().setManageGamerule(parseBool(value)); yield true; }
            case "afktime" -> { mod.config().setValue("afk-detection.time-seconds", parseInt(value)); AfkTracker.shutdown(); int v = parseInt(value); if (v>0) AfkTracker.init(v); yield true; }
            case "countafk" -> { mod.config().setValue("count-afk-as-sleeping", parseBool(value)); yield true; }
            case "excludeafk" -> { mod.config().setValue("exclude-afk-from-total", parseBool(value)); yield true; }
            default -> {
                if (!k.contains(".") && !k.contains("-")) throw new IllegalArgumentException("Unknown option '" + key + "'");
                Object inf = inferValue(value);
                mod.config().setValue(k, inf);
                yield true;
            }
        };
    }

    private void handleGet(CommandSourceStack src, String key) {
        if (!mod.permissions().canViewStatus(src) && !mod.permissions().canConfig(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        if (key == null || key.isEmpty()) {
            send(src, LegacyText.of("&cUsage: /onlysleep get <option>"));
            send(src, LegacyText.of("&epercentage=" + mod.config().getSleepPercentage() + "% skiptype=" + mod.config().getSkipType() + " perworld=" + mod.config().isPerWorldSleep()));
            return;
        }
        String val = resolveGet(key);
        if (val == null) val = mod.config().getValueAsString(key);
        if (val == null) send(src, LegacyText.of("&cUnknown option '" + key + "'"));
        else send(src, LegacyText.of("&7" + key + " &8= &b" + val));
    }

    private String resolveGet(String key) {
        return switch (key.toLowerCase()) {
            case "percentage", "sleep-percentage" -> String.valueOf(mod.config().getSleepPercentage());
            case "skiptype", "skip-type" -> mod.config().getSkipType();
            case "perworld", "per-world-sleep" -> String.valueOf(mod.config().isPerWorldSleep());
            case "skipdelay", "skip-delay-ticks" -> String.valueOf(mod.config().getSkipDelayTicks());
            case "morningtime", "morning-time" -> String.valueOf(mod.config().getMorningTime());
            case "resettime", "reset-time" -> String.valueOf(mod.config().isResetTime());
            case "gradualspeed", "gradual-skip-speed-ticks" -> String.valueOf(mod.config().getGradualSkipSpeedTicks());
            case "clearweather", "clear-weather" -> String.valueOf(mod.config().isClearWeather());
            case "clearthunder", "clear-thunder" -> String.valueOf(mod.config().isClearThunder());
            case "bossbar" -> String.valueOf(mod.config().isShowBossBar());
            case "actionbar" -> String.valueOf(mod.config().isShowActionBar());
            case "progressbar" -> String.valueOf(mod.config().isShowProgressBar());
            case "title" -> String.valueOf(mod.config().isShowTitle());
            case "sounds" -> String.valueOf(mod.config().isPlaySounds());
            case "managegamerule", "manage-gamerule" -> String.valueOf(mod.config().isManageGamerule());
            default -> null;
        };
    }

    private void handleToggle(CommandSourceStack src, String key) {
        if (!mod.permissions().canConfig(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        String cur = resolveGet(key);
        if (cur == null) {
            String raw = mod.config().getValueAsString(key);
            if (raw != null && (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false"))) {
                boolean next = !Boolean.parseBoolean(raw);
                mod.config().setValue(key, next);
                if (mod.server()!=null) mod.sleepManager().applyGamerules(mod.server());
                send(src, LegacyText.of("&aSet &b" + key + " &ato &b" + next));
                return;
            }
            send(src, LegacyText.of("&cCannot toggle '" + key + "'"));
            return;
        }
        if (!cur.equalsIgnoreCase("true") && !cur.equalsIgnoreCase("false")) { send(src, LegacyText.of("&cNot toggleable: " + cur)); return; }
        boolean next = !Boolean.parseBoolean(cur);
        applySet(key, String.valueOf(next));
        if (mod.server()!=null) mod.sleepManager().applyGamerules(mod.server());
        send(src, LegacyText.of("&aSet &b" + key + " &ato &b" + next));
    }

    private void handleWorld(CommandSourceStack src, String action, String world) {
        if (!mod.permissions().canManageWorld(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        if (action == null || action.equals("list")) {
            var list = mod.config().getStringList("disabled-worlds");
            send(src, LegacyText.of("&eDisabled worlds: " + (list.isEmpty() ? "none" : String.join(", ", list))));
            if (world == null) return;
        }
        if (world == null) { send(src, LegacyText.of("&cUsage: /onlysleep world <enable|disable|list> [world]")); return; }
        var disabled = new java.util.ArrayList<>(mod.config().getStringList("disabled-worlds"));
        if (action.equals("disable")) {
            if (disabled.contains(world)) { send(src, LegacyText.of("&eAlready disabled")); return; }
            disabled.add(world); mod.config().setValue("disabled-worlds", disabled); send(src, LegacyText.of("&aDisabled " + world));
        } else if (action.equals("enable")) {
            if (!disabled.contains(world)) { send(src, LegacyText.of("&eAlready enabled")); return; }
            disabled.remove(world); mod.config().setValue("disabled-worlds", disabled); send(src, LegacyText.of("&aEnabled " + world));
        }
        if (mod.server()!=null) mod.sleepManager().applyGamerules(mod.server());
    }

    private void handleGamemode(CommandSourceStack src, String action, String gm) {
        if (!mod.permissions().canManageGamemode(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        if (action == null || action.equals("list")) {
            var list = mod.config().getStringList("disabled-gamemodes");
            send(src, LegacyText.of("&eDisabled gamemodes: " + (list.isEmpty() ? "none" : String.join(", ", list))));
            if (gm == null) return;
        }
        if (gm == null) { send(src, LegacyText.of("&cUsage: /onlysleep gamemode <enable|disable|list> [gamemode]")); return; }
        String upper = gm.toUpperCase();
        try { net.minecraft.world.level.GameType.valueOf(upper); } catch (Exception e) { send(src, LegacyText.of("&cInvalid gamemode: " + gm)); return; }
        var disabled = new java.util.ArrayList<>(mod.config().getStringList("disabled-gamemodes"));
        if (action.equals("disable")) {
            if (disabled.contains(upper)) { send(src, LegacyText.of("&eAlready disabled")); return; }
            disabled.add(upper); mod.config().setValue("disabled-gamemodes", disabled); send(src, LegacyText.of("&aDisabled " + upper));
        } else if (action.equals("enable")) {
            if (!disabled.contains(upper)) { send(src, LegacyText.of("&eAlready enabled")); return; }
            disabled.remove(upper); mod.config().setValue("disabled-gamemodes", disabled); send(src, LegacyText.of("&aEnabled " + upper));
        }
    }

    private void handleDump(CommandSourceStack src, boolean paste) {
        if (!mod.permissions().canDump(src)) { send(src, mod.config().getMessage("command.no-permission")); return; }
        send(src, LegacyText.of("&eGenerating dump..."));
        try {
            String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            java.io.File dir = new java.io.File(mod.configDir().toFile(), "dumps");
            dir.mkdirs();
            java.io.File file = new java.io.File(dir, "dump-" + ts + ".txt");
            StringBuilder sb = new StringBuilder();
            sb.append("=== Onlysleep Fabric Dump ===\nTS: ").append(ts).append("\nVer: ").append(mod.version()).append(" MC: ").append(mod.minecraftVersion()).append("\n");
            sb.append("Config:\n");
            for (String k : mod.config().getStringList("disabled-worlds")) sb.append("  disabled-worlds: ").append(k).append("\n");

            sb.append("sleep-percentage: ").append(mod.config().getSleepPercentage()).append("\n");
            sb.append("skip-type: ").append(mod.config().getSkipType()).append("\n");
            sb.append("per-world-sleep: ").append(mod.config().isPerWorldSleep()).append("\n");
            if (mod.server()!=null) {
                sb.append("\nWorlds:\n");
                for (net.minecraft.server.level.ServerLevel lvl : mod.server().getAllLevels()) {
                    String wk = SleepManager.worldKey(lvl);
                    sb.append(" ").append(wk).append(" enabled=").append(mod.config().isWorldEnabled(wk));
                    sb.append(" time=").append(mod.sleepManager().dayTimeOf(lvl)).append(" players=").append(lvl.players().size());
                    try { sb.append(" eligible=").append(mod.sleepManager().getTotalPlayerCount(lvl)).append(" sleeping=").append(mod.sleepManager().getSleepingCount(lvl)).append(" required=").append(mod.sleepManager().getRequiredSleepingCount(lvl)); } catch (Exception e) {}
                    sb.append("\n");
                }
                sb.append("\nPlayers:\n");
                for (ServerPlayer p : mod.server().getPlayerList().getPlayers()) {
                    sb.append(" ").append(p.getName().getString()).append(" world=").append(p.level().dimension().identifier()).append(" gm=").append(p.gameMode.getGameModeForPlayer());
                    sb.append(" flying=").append(p.getAbilities().flying).append(" exempt=").append(mod.permissions().isExempt(p));
                    try { sb.append(" afk=").append(AfkTracker.isAfk(p)); } catch (Exception e) {}
                    sb.append("\n");
                }
            }
            java.nio.file.Files.writeString(file.toPath(), sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
            send(src, LegacyText.of("&aDump saved to &b" + file.getPath()));
            mod.logger().info("Dump created: {}", file.getAbsolutePath());
            if (paste) {
                send(src, LegacyText.of("&eUploading..."));
                new Thread(() -> {
                    try {
                        String url = uploadToPaste(sb.toString());
                        mod.scheduler().runSync(() -> send(src, LegacyText.of("&aPaste: &b" + url)));
                    } catch (Exception ex) { mod.scheduler().runSync(() -> send(src, LegacyText.of("&cPaste failed: " + ex.getMessage()))); }
                }).start();
            }
        } catch (Exception e) { send(src, LegacyText.of("&cDump failed: " + e.getMessage())); }
    }

    private String uploadToPaste(String c) throws Exception {
        java.net.URL url = new java.net.URL("https://paste.mcsrv.top/documents");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST"); conn.setDoOutput(true); conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        conn.setConnectTimeout(5000); conn.setReadTimeout(5000);
        try (java.io.OutputStream os = conn.getOutputStream()) { os.write(c.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        if (conn.getResponseCode()!=200) throw new java.io.IOException("HTTP " + conn.getResponseCode());
        String resp = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        String key = resp.replaceAll(".*\"key\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        if (key.equals(resp)) throw new java.io.IOException(resp);
        return "https://paste.mcsrv.top/" + key;
    }

    private boolean parseBool(String v) {
        if (v.equalsIgnoreCase("true")||v.equalsIgnoreCase("yes")||v.equalsIgnoreCase("on")||v.equals("1")) return true;
        if (v.equalsIgnoreCase("false")||v.equalsIgnoreCase("no")||v.equalsIgnoreCase("off")||v.equals("0")) return false;
        throw new IllegalArgumentException("Value must be true/false");
    }
    private int parseInt(String v) { try { return Integer.parseInt(v); } catch (NumberFormatException e) { throw new IllegalArgumentException("Must be a number"); } }
    private Object inferValue(String v) {
        if (v.equalsIgnoreCase("true")||v.equalsIgnoreCase("false")) return Boolean.parseBoolean(v);
        try { return Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        try { return Double.parseDouble(v); } catch (NumberFormatException ignored) {}
        return v;
    }

    private void send(CommandSourceStack src, MutableComponent message) {
        ServerPlayer player = src.getPlayer();
        if (player != null) {
            player.sendSystemMessage(message);
        } else {
            src.sendSuccess(() -> message, false);
        }
    }
}

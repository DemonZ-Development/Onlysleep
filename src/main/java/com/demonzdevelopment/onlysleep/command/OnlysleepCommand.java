package com.demonzdevelopment.onlysleep.command;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;
import com.demonzdevelopment.onlysleep.util.SchedulerAdapter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OnlysleepCommand implements CommandExecutor, TabCompleter {

    private final Onlysleep plugin;
    private final ConfigManager configManager;
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "info", "status", "help", "update", "set", "get", "toggle", "world", "gamemode", "dump");
    private static final List<String> SET_KEYS = Arrays.asList(
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
    private static final List<String> TOGGLE_KEYS = Arrays.asList(
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
    private static final List<String> GET_KEYS = Arrays.asList(
        "percentage", "skiptype", "perworld", "skipdelay", "morningtime", "resettime",
        "gradualspeed", "clearweather", "clearthunder", "bossbar", "actionbar", "progressbar"
    );

    public OnlysleepCommand(Onlysleep plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "info":
                sendInfo(sender);
                break;
            case "status":
                sendStatus(sender);
                break;
            case "help":
                sendHelp(sender);
                break;
            case "update":
                handleUpdate(sender);
                break;
            case "set":
                handleSet(sender, args);
                break;
            case "get":
                handleGet(sender, args);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "world":
                handleWorld(sender, args);
                break;
            case "gamemode":
                handleGamemode(sender, args);
                break;
            case "dump":
                handleDump(sender, args);
                break;

            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("onlysleep.reload")) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }

        try {
            configManager.reload();

            plugin.getSleepManager().applyGamerules();

            com.demonzdevelopment.onlysleep.util.AfkTracker.shutdown();
            if (configManager.getAfkTimeSeconds() > 0) {
                com.demonzdevelopment.onlysleep.util.AfkTracker.init(plugin);
                plugin.getLogger().info("AFK tracker re-initialised (" + configManager.getAfkTimeSeconds() + "s timeout)");
            }

            com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker.shutdown();
            if (configManager.isRequireAllPlayersOnline()) {
                com.demonzdevelopment.onlysleep.util.OfflinePlayerTracker.init(plugin);
                plugin.getLogger().info("Offline player tracker re-initialised for require-all-players-online");
            }

            sender.sendMessage(configManager.getMessage("command.reload-success"));
            plugin.getLogger().info("Configuration reloaded by " + sender.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload config: " + e.getMessage());
            sender.sendMessage(configManager.getMessage("command.reload-fail"));
        }
    }

    private boolean hasConfigPermission(CommandSender sender) {
        return sender.hasPermission("onlysleep.config") || sender.hasPermission("onlysleep.reload") || sender.hasPermission("onlysleep.admin");
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (!hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /onlysleep set <option> <value>");
            sender.sendMessage(ChatColor.GRAY + "Options: percentage <0-100>, skiptype <instant|gradual|speed>, perworld <true|false>, skipdelay <ticks>, morningtime <ticks>, resettime <true|false>, gradualspeed <ticks>, clearweather <true|false>, clearthunder <true|false>, bossbar <true|false>, actionbar <true|false>, progressbar <true|false>, title <true|false>, sounds <true|false>, managegamerule <true|false>");
            sender.sendMessage(ChatColor.GRAY + "Or raw path: /onlysleep set <config.path> <value>  e.g. afk-detection.time-seconds 300");
            return;
        }
        String key = args[1].toLowerCase();
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        try {
            boolean applied = applySet(sender, key, value);
            if (applied) {
                plugin.getSleepManager().applyGamerules();
                Map<String, String> ph = new HashMap<>();
                ph.put("key", key);
                ph.put("value", value);
                sender.sendMessage(configManager.getMessage("command.set-success", ph));
                plugin.getLogger().info(sender.getName() + " set " + key + " = " + value);
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to set value: " + e.getMessage());
            plugin.getLogger().warning("Failed to set config " + key + ": " + e.getMessage());
        }
    }

    private boolean applySet(CommandSender sender, String key, String value) {
        switch (key) {
            case "percentage":
            case "sleep-percentage":
            case "sleep_percentage":
                int pct;
                try { pct = Integer.parseInt(value); } catch (NumberFormatException e) { throw new IllegalArgumentException("Percentage must be 0-100"); }
                if (pct < 0 || pct > 100) throw new IllegalArgumentException("Percentage must be 0-100 (0 = one player)");
                configManager.setSleepPercentage(pct);
                return true;
            case "skiptype":
            case "skip-type":
            case "skip_type":
                if (!configManager.setSkipType(value)) throw new IllegalArgumentException("Skip type must be instant, gradual or speed");
                return true;
            case "perworld":
            case "per-world":
            case "per-world-sleep":
            case "per_world_sleep":
                configManager.setPerWorldSleep(parseBool(value));
                return true;
            case "skipdelay":
            case "skip-delay":
            case "skip-delay-ticks":
            case "skip_delay_ticks":
                configManager.setSkipDelayTicks(parseInt(value, "skipdelay"));
                return true;
            case "morningtime":
            case "morning-time":
            case "morning_time":
                configManager.setMorningTime(parseInt(value, "morningtime"));
                return true;
            case "resettime":
            case "reset-time":
            case "reset_time":
                configManager.setResetTime(parseBool(value));
                return true;
            case "gradualspeed":
            case "gradual-speed":
            case "gradual-skip-speed-ticks":
            case "gradual_skip_speed_ticks":
                configManager.setGradualSkipSpeedTicks(parseInt(value, "gradualspeed"));
                return true;
            case "clearweather":
            case "clear-weather":
                configManager.setClearWeather(parseBool(value));
                return true;
            case "clearthunder":
            case "clear-thunder":
                configManager.setClearThunder(parseBool(value));
                return true;
            case "resetweather":
            case "reset-weather":
                configManager.setResetWeather(parseBool(value));
                return true;
            case "resetthunder":
            case "reset-thunder":
                configManager.setResetThunder(parseBool(value));
                return true;
            case "bossbar":
            case "boss-bar":
            case "ui.boss-bar.enabled":
                configManager.setValue("ui.boss-bar.enabled", parseBool(value));
                return true;
            case "actionbar":
            case "action-bar":
            case "ui.action-bar.enabled":
                configManager.setValue("ui.action-bar.enabled", parseBool(value));
                return true;
            case "progressbar":
            case "progress-bar":
            case "ui.progress-bar.enabled":
                configManager.setValue("ui.progress-bar.enabled", parseBool(value));
                return true;
            case "title":
            case "ui.title.enabled":
                configManager.setValue("ui.title.enabled", parseBool(value));
                return true;
            case "sounds":
            case "sounds.enabled":
                configManager.setValue("sounds.enabled", parseBool(value));
                return true;
            case "managegamerule":
            case "manage-gamerule":
            case "manage_gamerule":
                configManager.setManageGamerule(parseBool(value));
                return true;
            case "afktime":
            case "afk-time":
            case "afk-detection.time-seconds":
                configManager.setValue("afk-detection.time-seconds", parseInt(value, "afktime"));

                com.demonzdevelopment.onlysleep.util.AfkTracker.shutdown();
                if (parseInt(value, "afktime") > 0) com.demonzdevelopment.onlysleep.util.AfkTracker.init(plugin);
                return true;
            case "countafk":
            case "count-afk-as-sleeping":
                configManager.setValue("count-afk-as-sleeping", parseBool(value));
                return true;
            case "excludeafk":
            case "exclude-afk-from-total":
                configManager.setValue("exclude-afk-from-total", parseBool(value));
                return true;
            default:

                String rawPath = key;

                if (!rawPath.contains(".") && !rawPath.contains("-") && !rawPath.contains("_")) {
                    throw new IllegalArgumentException("Unknown option '" + key + "'. Try /onlysleep set help");
                }

                Object inferred = inferValue(value);
                configManager.setValue(rawPath, inferred);
                return true;
        }
    }

    private void handleGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("onlysleep.status") && !hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /onlysleep get <option>");
            sender.sendMessage(ChatColor.GRAY + "Options: percentage, skiptype, perworld, skipdelay, morningtime, resettime, gradualspeed, clearweather, clearthunder, bossbar, actionbar, etc. Or raw path e.g. afk-detection.time-seconds");

            sender.sendMessage(ChatColor.YELLOW + "percentage=" + configManager.getSleepPercentage() + "%  skiptype=" + configManager.getSkipType() + "  perworld=" + configManager.isPerWorldSleep());
            return;
        }
        String key = args[1].toLowerCase();
        String val = resolveGet(key);
        if (val == null) val = configManager.getValueAsString(key);
        if (val == null) {
            sender.sendMessage(ChatColor.RED + "Unknown option '" + key + "'");
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7" + key + " &8= &b" + val));
        }
    }

    private String resolveGet(String key) {
        switch (key) {
            case "percentage": case "sleep-percentage": return String.valueOf(configManager.getSleepPercentage());
            case "skiptype": case "skip-type": return configManager.getSkipType();
            case "perworld": case "per-world-sleep": return String.valueOf(configManager.isPerWorldSleep());
            case "skipdelay": case "skip-delay-ticks": return String.valueOf(configManager.getSkipDelayTicks());
            case "morningtime": case "morning-time": return String.valueOf(configManager.getMorningTime());
            case "resettime": case "reset-time": return String.valueOf(configManager.isResetTime());
            case "gradualspeed": case "gradual-skip-speed-ticks": return String.valueOf(configManager.getGradualSkipSpeedTicks());
            case "clearweather": case "clear-weather": return String.valueOf(configManager.isClearWeather());
            case "clearthunder": case "clear-thunder": return String.valueOf(configManager.isClearThunder());
            case "bossbar": return String.valueOf(configManager.isShowBossBar());
            case "actionbar": return String.valueOf(configManager.isShowActionBar());
            case "progressbar": return String.valueOf(configManager.isShowProgressBar());
            case "title": return String.valueOf(configManager.isShowTitle());
            case "sounds": return String.valueOf(configManager.isPlaySounds());
            case "managegamerule": case "manage-gamerule": return String.valueOf(configManager.isManageGamerule());
            default: return null;
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /onlysleep toggle <option>");
            sender.sendMessage(ChatColor.GRAY + "Toggleable: perworld, resettime, clearweather, clearthunder, bossbar, actionbar, progressbar, title, sounds, managegamerule, countafk, excludeafk");
            return;
        }
        String key = args[1].toLowerCase();
        String current = resolveGet(key);
        if (current == null) {
            String raw = configManager.getValueAsString(key);
            if (raw != null && (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false"))) {
                boolean next = !Boolean.parseBoolean(raw);
                configManager.setValue(key, next);
                plugin.getSleepManager().applyGamerules();
                Map<String, String> ph = new HashMap<>();
                ph.put("key", key);
                ph.put("value", String.valueOf(next));
                sender.sendMessage(configManager.getMessage("command.set-success", ph));
                return;
            }
            sender.sendMessage(ChatColor.RED + "Cannot toggle '" + key + "' - not a boolean option");
            return;
        }
        if (!current.equalsIgnoreCase("true") && !current.equalsIgnoreCase("false")) {
            sender.sendMessage(ChatColor.RED + "Option '" + key + "' is not toggleable (value=" + current + ")");
            return;
        }
        boolean next = !Boolean.parseBoolean(current);
        applySet(sender, key, String.valueOf(next));
        plugin.getSleepManager().applyGamerules();
        Map<String, String> ph = new HashMap<>();
        ph.put("key", key);
        ph.put("value", String.valueOf(next));
        sender.sendMessage(configManager.getMessage("command.set-success", ph));
    }

    private void handleWorld(CommandSender sender, String[] args) {
        if (!hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /onlysleep world <enable|disable|list> [world]");
            sender.sendMessage(ChatColor.GRAY + "Current disabled: " + (configManager.getConfig().getStringList("disabled-worlds").isEmpty() ? "none" : String.join(", ", configManager.getConfig().getStringList("disabled-worlds"))));
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            List<String> list = configManager.getConfig().getStringList("disabled-worlds");
            sender.sendMessage(ChatColor.YELLOW + "Disabled worlds: " + (list.isEmpty() ? "none" : String.join(", ", list)));
            return;
        }
        String world = args[2];
        List<String> disabled = new java.util.ArrayList<>(configManager.getConfig().getStringList("disabled-worlds"));
        if (action.equals("disable")) {
            if (disabled.contains(world)) {
                sender.sendMessage(ChatColor.YELLOW + "World '" + world + "' already disabled");
                return;
            }
            disabled.add(world);
            configManager.setValue("disabled-worlds", disabled);
            sender.sendMessage(ChatColor.GREEN + "Disabled world '" + world + "'");
        } else if (action.equals("enable")) {
            if (!disabled.contains(world)) {
                sender.sendMessage(ChatColor.YELLOW + "World '" + world + "' already enabled");
                return;
            }
            disabled.remove(world);
            configManager.setValue("disabled-worlds", disabled);
            sender.sendMessage(ChatColor.GREEN + "Enabled world '" + world + "'");
        } else {
            sender.sendMessage(ChatColor.RED + "Use enable/disable/list");
        }
        plugin.getSleepManager().applyGamerules();
    }

    private void handleGamemode(CommandSender sender, String[] args) {
        if (!hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /onlysleep gamemode <enable|disable|list> [gamemode]");
            sender.sendMessage(ChatColor.GRAY + "Disabled: " + (configManager.getConfig().getStringList("disabled-gamemodes").isEmpty() ? "none" : String.join(", ", configManager.getConfig().getStringList("disabled-gamemodes"))));
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            List<String> list = configManager.getConfig().getStringList("disabled-gamemodes");
            sender.sendMessage(ChatColor.YELLOW + "Disabled gamemodes: " + (list.isEmpty() ? "none" : String.join(", ", list)));
            return;
        }
        String gm = args[2].toUpperCase();
        try { org.bukkit.GameMode.valueOf(gm); } catch (Exception e) { sender.sendMessage(ChatColor.RED + "Invalid gamemode: " + gm + " (SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR)"); return; }
        List<String> disabled = new java.util.ArrayList<>(configManager.getConfig().getStringList("disabled-gamemodes"));
        if (action.equals("disable")) {
            if (disabled.contains(gm)) { sender.sendMessage(ChatColor.YELLOW + "Gamemode '" + gm + "' already disabled"); return; }
            disabled.add(gm);
            configManager.setValue("disabled-gamemodes", disabled);
            sender.sendMessage(ChatColor.GREEN + "Disabled gamemode '" + gm + "'");
        } else if (action.equals("enable")) {
            if (!disabled.contains(gm)) { sender.sendMessage(ChatColor.YELLOW + "Gamemode '" + gm + "' already enabled"); return; }
            disabled.remove(gm);
            configManager.setValue("disabled-gamemodes", disabled);
            sender.sendMessage(ChatColor.GREEN + "Enabled gamemode '" + gm + "'");
        } else sender.sendMessage(ChatColor.RED + "Use enable/disable/list");
    }

    private void handleDump(CommandSender sender, String[] args) {
        if (!sender.hasPermission("onlysleep.dump") && !sender.hasPermission("onlysleep.admin") && !hasConfigPermission(sender)) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }
        sender.sendMessage(ChatColor.YELLOW + "Generating dump...");

        try {
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = "dump-" + timestamp + ".txt";
            java.io.File dumpDir = new java.io.File(plugin.getDataFolder(), "dumps");
            if (!dumpDir.exists()) dumpDir.mkdirs();
            java.io.File dumpFile = new java.io.File(dumpDir, fileName);

            StringBuilder sb = new StringBuilder();
            sb.append("=== Onlysleep Dump ===\n");
            sb.append("Timestamp: ").append(timestamp).append("\n");
            sb.append("Plugin: Onlysleep v").append(plugin.getDescription().getVersion()).append("\n");
            sb.append("Platform: ").append(plugin.getPlatform().getDisplayName()).append(" | MC: ").append(PlatformAdapter.getMinecraftVersion()).append("\n");
            sb.append("Server: ").append(org.bukkit.Bukkit.getVersion()).append(" | Bukkit: ").append(org.bukkit.Bukkit.getBukkitVersion()).append("\n");
            sb.append("Java: ").append(System.getProperty("java.version")).append(" (").append(System.getProperty("java.vendor")).append(")\n");
            sb.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append("\n");
            sb.append("\n--- Config (config.yml) ---\n");

            org.bukkit.configuration.file.FileConfiguration cfg = configManager.getConfig();
            for (String key : cfg.getKeys(true)) {
                if (cfg.isConfigurationSection(key)) continue;
                Object val = cfg.get(key);
                sb.append(key).append(": ").append(val == null ? "null" : val.toString()).append("\n");
            }

            sb.append("\n--- Parsed Config ---\n");
            sb.append("sleep-percentage: ").append(configManager.getSleepPercentage()).append("\n");
            sb.append("skip-type: ").append(configManager.getSkipType()).append("\n");
            sb.append("per-world-sleep: ").append(configManager.isPerWorldSleep()).append("\n");
            sb.append("skip-delay-ticks: ").append(configManager.getSkipDelayTicks()).append("\n");
            sb.append("morning-time: ").append(configManager.getMorningTime()).append("\n");
            sb.append("reset-time: ").append(configManager.isResetTime()).append("\n");
            sb.append("gradual-skip-speed-ticks: ").append(configManager.getGradualSkipSpeedTicks()).append("\n");
            sb.append("clear-weather: ").append(configManager.isClearWeather()).append(" reset-weather: ").append(configManager.isResetWeather()).append("\n");
            sb.append("clear-thunder: ").append(configManager.isClearThunder()).append(" reset-thunder: ").append(configManager.isResetThunder()).append(" reset-weather-cycle: ").append(configManager.isResetWeatherCycle()).append("\n");
            sb.append("count-afk-as-sleeping: ").append(configManager.isCountAfkAsSleeping()).append(" exclude-afk-from-total: ").append(configManager.isExcludeAfkFromTotal()).append("\n");
            sb.append("count-spectators: ").append(configManager.isCountSpectators()).append(" count-flying: ").append(configManager.isCountFlying()).append(" ignore-creative: ").append(configManager.isIgnoreCreativeMode()).append("\n");
            sb.append("manage-gamerule: ").append(configManager.isManageGamerule()).append(" check-for-updates: ").append(configManager.isCheckForUpdates()).append("\n");
            sb.append("disabled-worlds: ").append(configManager.getConfig().getStringList("disabled-worlds")).append("\n");
            sb.append("disabled-gamemodes: ").append(configManager.getConfig().getStringList("disabled-gamemodes")).append("\n");

            sb.append("\n--- Worlds ---\n");
            for (org.bukkit.World world : org.bukkit.Bukkit.getWorlds()) {
                sb.append("World: ").append(world.getName()).append(" (").append(world.getEnvironment()).append(") enabled=").append(configManager.isWorldEnabled(world.getName())).append("\n");
                sb.append("  Time: ").append(world.getTime()).append(" isNight=").append(com.demonzdevelopment.onlysleep.manager.SleepManager.isNight(world.getTime())).append(" storm=").append(world.hasStorm()).append(" thunder=").append(world.isThundering()).append("\n");
                try {
                    Integer gamerule = world.getGameRuleValue(org.bukkit.GameRule.PLAYERS_SLEEPING_PERCENTAGE);
                    sb.append("  Gamerule PLAYERS_SLEEPING_PERCENTAGE: ").append(gamerule).append("\n");
                } catch (Exception e) { sb.append("  Gamerule: error ").append(e.getMessage()).append("\n"); }
                sb.append("  Players in world: ").append(world.getPlayers().size()).append(" / global: ").append(org.bukkit.Bukkit.getOnlinePlayers().size()).append("\n");
                try {
                    int total = plugin.getSleepManager().getTotalPlayerCount(world);
                    int sleeping = plugin.getSleepManager().getSleepingCount(world);
                    int required = plugin.getSleepManager().getRequiredSleepingCount(world);
                    boolean scheduled = plugin.getSleepManager().isSkipScheduled(world);
                    sb.append("  Eligible: ").append(total).append(" Sleeping: ").append(sleeping).append(" Required: ").append(required).append(" Scheduled: ").append(scheduled).append("\n");
                } catch (Exception e) { sb.append("  Sleep counts error: ").append(e.getMessage()).append("\n"); }
            }

            sb.append("\n--- Online Players ---\n");
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                sb.append("Player: ").append(p.getName()).append(" UUID=").append(p.getUniqueId()).append(" World=").append(p.getWorld().getName()).append(" GM=").append(p.getGameMode());
                sb.append(" Flying=").append(p.isFlying()).append(" AllowFlight=").append(p.getAllowFlight()).append(" Op=").append(p.isOp());
                sb.append(" Exempt=").append(p.hasPermission("onlysleep.exempt"));
                try { sb.append(" Afk=").append(com.demonzdevelopment.onlysleep.util.AfkTracker.isAfk(p)); } catch (Exception e) { sb.append(" Afk=err"); }
                try { sb.append(" Sleeping=").append(plugin.getSleepManager().isPlayerSleeping(p)); } catch (Exception e) { sb.append(" Sleeping=err"); }
                sb.append("\n");
            }

            sb.append("\n--- Messages (messages.yml excerpt) ---\n");
            org.bukkit.configuration.file.FileConfiguration msgs = configManager.getMessages();
            for (String key : msgs.getKeys(true)) {
                if (msgs.isConfigurationSection(key)) continue;
                if (key.startsWith("command") || key.startsWith("help") || key.startsWith("update")) continue;
                sb.append(key).append(": ").append(msgs.getString(key)).append("\n");
            }

            sb.append("\n--- End Dump ---\n");
            java.nio.file.Files.writeString(dumpFile.toPath(), sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
            sender.sendMessage(ChatColor.GREEN + "Dump saved to " + dumpFile.getPath());
            plugin.getLogger().info("Dump created: " + dumpFile.getAbsolutePath());


            if (sender instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player pl = (org.bukkit.entity.Player) sender;

                if (args.length > 1 && (args[1].equalsIgnoreCase("paste") || args[1].equalsIgnoreCase("upload"))) {
                    sender.sendMessage(ChatColor.YELLOW + "Uploading to paste...");

                    org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            String pasteUrl = uploadToPaste(sb.toString());
                            SchedulerAdapter.runGlobalTask(plugin, () -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aPaste uploaded: &b" + pasteUrl)));
                        } catch (Exception ex) {
                            SchedulerAdapter.runGlobalTask(plugin, () -> sender.sendMessage(ChatColor.RED + "Paste upload failed: " + ex.getMessage()));
                        }
                    });
                } else {
                    sender.sendMessage(ChatColor.GRAY + "Run /onlysleep dump paste to also upload to paste.mcsrv.top");
                }
            } else {

                sender.sendMessage(ChatColor.GRAY + "--- Dump preview (first 20 lines) ---");
                String[] lines = sb.toString().split("\n");
                for (int i = 0; i < Math.min(20, lines.length); i++) sender.sendMessage(ChatColor.GRAY + lines[i]);
            }

        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Dump failed: " + e.getMessage());
            plugin.getLogger().severe("Dump failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String uploadToPaste(String content) throws Exception {


        java.net.URL url = new java.net.URL("https://paste.mcsrv.top/documents");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        if (code != 200) throw new java.io.IOException("HTTP " + code);
        String resp;
        try (java.io.InputStream is = conn.getInputStream()) { resp = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); }

        String key = resp.replaceAll(".*\"key\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        if (key.equals(resp)) throw new java.io.IOException("Unexpected paste response: " + resp);
        return "https://paste.mcsrv.top/" + key;
    }

    private boolean parseBool(String v) {
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on") || v.equals("1")) return true;
        if (v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off") || v.equals("0")) return false;
        throw new IllegalArgumentException("Value must be true/false");
    }
    private int parseInt(String v, String field) {
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { throw new IllegalArgumentException(field + " must be a number"); }
    }
    private Object inferValue(String v) {
        if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) return Boolean.parseBoolean(v);
        try { return Integer.parseInt(v); } catch (NumberFormatException ignored) {}
        try { return Double.parseDouble(v); } catch (NumberFormatException ignored) {}
        return v;
    }

    private void sendInfo(CommandSender sender) {
        if (!sender.hasPermission("onlysleep.info")) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }

        String version = plugin.getDescription().getVersion();
        String author = plugin.getDescription().getAuthors().isEmpty()
            ? "Demonz Development" : plugin.getDescription().getAuthors().get(0);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("version", version);
        placeholders.put("author", author);
        placeholders.put("percent", String.valueOf(configManager.getSleepPercentage()));
        placeholders.put("platform", plugin.getPlatform().getDisplayName());

        sender.sendMessage(configManager.getMessage("command.info.header", placeholders));
        sender.sendMessage(configManager.getMessage("command.info.version", placeholders));
        sender.sendMessage(configManager.getMessage("command.info.author", placeholders));
        sender.sendMessage(configManager.getMessage("command.info.status-enabled", placeholders));
        sender.sendMessage(configManager.getMessage("command.info.sleep-pct", placeholders));
        sender.sendMessage(configManager.getMessage("command.info.platform", Map.of("platform", plugin.getPlatform().getDisplayName())));
        sender.sendMessage(configManager.getMessage("command.info.links-header"));
        sender.sendMessage(configManager.getMessage("command.info.link-modrinth"));
        sender.sendMessage(configManager.getMessage("command.info.link-github"));
        sender.sendMessage(configManager.getMessage("command.info.link-website"));
        sender.sendMessage(configManager.getMessage("command.info.link-discord"));
        sender.sendMessage(configManager.getMessage("command.info.footer", placeholders));
    }

    private void sendStatus(CommandSender sender) {
        if (!sender.hasPermission("onlysleep.status")) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8=== &bOnlysleep Status &8==="));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Platform: &b" + plugin.getPlatform().getDisplayName()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Minecraft: &b" + PlatformAdapter.getMinecraftVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Version: &b" + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Sleep %%: &b" + configManager.getSleepPercentage() + "%"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Per-World: &b" + (configManager.isPerWorldSleep() ? "&aYes" : "&cNo")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Boss Bar: &b" + (configManager.isShowBossBar() ? "&aEnabled" : "&cDisabled")));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&7Skip Type: &b" + configManager.getSkipType()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
            "&8========================"));
    }

    private void handleUpdate(CommandSender sender) {
        if (!sender.hasPermission("onlysleep.update")) {
            sender.sendMessage(configManager.getMessage("command.no-permission"));
            return;
        }

        sender.sendMessage(configManager.getMessage("update.checking"));
        plugin.getUpdateChecker().checkAsync().thenAccept(result -> {

            SchedulerAdapter.runGlobalTask(plugin, () -> {
                if (result.isUpdateAvailable()) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("new", result.getLatestVersion());
                    placeholders.put("current", plugin.getDescription().getVersion());
                    sender.sendMessage(configManager.getMessage("update.available", placeholders));
                } else {
                    sender.sendMessage(configManager.getMessage("update.current"));
                }
            });
        }).exceptionally(throwable -> {
            SchedulerAdapter.runGlobalTask(plugin, () ->
                sender.sendMessage(configManager.getMessage("update.check-fail"))
            );
            return null;
        });
    }

    private void sendHelp(CommandSender sender) {
        String cmd = "onlysleep";
        Map<String, String> placeholders = Map.of("cmd", cmd);
        sender.sendMessage(configManager.getMessage("help.header", placeholders));
        sender.sendMessage(configManager.getMessage("help.reload", placeholders));
        sender.sendMessage(configManager.getMessage("help.info", placeholders));
        sender.sendMessage(configManager.getMessage("help.status", placeholders));
        sender.sendMessage(configManager.getMessage("help.update", placeholders));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% set <option> <value> &8- &bChange config (try /%cmd% set)"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% get <option> &8- &bView config value"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% toggle <option> &8- &bToggle boolean"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% world <enable|disable|list> [world] &8- &bManage disabled worlds"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% gamemode <enable|disable|list> [type] &8- &bManage disabled gamemodes"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7/%cmd% dump [paste] &8- &bCreate debug dump"));
        sender.sendMessage(configManager.getMessage("help.help", placeholders));
        sender.sendMessage(configManager.getMessage("help.footer", placeholders));
        if (sender.hasPermission("onlysleep.config") || sender.hasPermission("onlysleep.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8--- &bSet Options &8---"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7percentage &8<0-100> &7skiptype &8<instant|gradual|speed> &7perworld &8<true|false>"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7skipdelay &8<ticks> &7morningtime &8<ticks> &7gradualspeed &8<ticks>"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7clearweather &7clearthunder &7bossbar &7actionbar &7progressbar &7title &7sounds"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String cur = args[1].toLowerCase();
            switch (sub) {
                case "set":
                    return SET_KEYS.stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                case "get":
                    return SET_KEYS.stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                case "toggle":
                    return TOGGLE_KEYS.stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                case "world":
                    return Arrays.asList("enable", "disable", "list").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                case "gamemode":
                    return Arrays.asList("enable", "disable", "list").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                case "dump":
                    return Arrays.asList("paste").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
            }
        }
        if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String key = args[1].toLowerCase();
            String cur = args[2].toLowerCase();
            if (sub.equals("set")) {
                switch (key) {
                    case "skiptype": case "skip-type":
                        return Arrays.asList("instant", "gradual", "speed").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                    case "perworld": case "per-world-sleep":
                    case "resettime": case "reset-time":
                    case "clearweather": case "clear-weather":
                    case "clearthunder": case "clear-thunder":
                    case "resetweather": case "reset-weather":
                    case "resetthunder": case "reset-thunder":
                    case "bossbar": case "actionbar": case "progressbar": case "title": case "sounds":
                    case "managegamerule": case "manage-gamerule":
                    case "countafk": case "excludeafk":
                        return Arrays.asList("true", "false").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                    case "percentage": case "sleep-percentage":
                        return Arrays.asList("0", "25", "50", "75", "100").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                    default:
                        if (key.equals("afktime") || key.equals("skipdelay") || key.equals("morningtime") || key.equals("gradualspeed")) {
                            return Arrays.asList("0", "30", "60", "100", "1000").stream().filter(s -> s.startsWith(cur)).collect(Collectors.toList());
                        }
                        break;
                }
            }
            if (sub.equals("world") && (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable"))) {
                return org.bukkit.Bukkit.getWorlds().stream().map(w -> w.getName()).filter(n -> n.toLowerCase().startsWith(cur)).collect(Collectors.toList());
            }
            if (sub.equals("gamemode") && (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable"))) {
                return Arrays.stream(org.bukkit.GameMode.values()).map(g -> g.name()).map(String::toLowerCase).filter(n -> n.startsWith(cur)).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}

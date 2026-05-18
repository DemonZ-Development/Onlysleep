package com.demonzdevelopment.onlysleep.command;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;
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
    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "info", "status", "help", "update");

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

            // Re-initialise AFK tracker if its config changed
            com.demonzdevelopment.onlysleep.util.AfkTracker.shutdown();
            if (configManager.getAfkTimeSeconds() > 0) {
                com.demonzdevelopment.onlysleep.util.AfkTracker.init(plugin);
                plugin.getLogger().info("AFK tracker re-initialised (" + configManager.getAfkTimeSeconds() + "s timeout)");
            }

            // Re-initialise offline player tracker if its config changed
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
            if (result.isUpdateAvailable()) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("new", result.getLatestVersion());
                placeholders.put("current", plugin.getDescription().getVersion());
                sender.sendMessage(configManager.getMessage("update.available", placeholders));
            } else {
                sender.sendMessage(configManager.getMessage("update.current"));
            }
        }).exceptionally(throwable -> {
            sender.sendMessage(configManager.getMessage("update.check-fail"));
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
        sender.sendMessage(configManager.getMessage("help.help", placeholders));
        sender.sendMessage(configManager.getMessage("help.footer", placeholders));
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}

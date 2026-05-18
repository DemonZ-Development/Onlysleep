# 🔌 Developer API

Onlysleep provides a public API for other plugins to integrate with. This allows you to check sleep status, query player counts, and hook into sleep events.

---

## Getting Started

### Add Onlysleep as a Dependency

> **Note:** Onlysleep does not currently publish artifacts to Maven Central or JitPack. The dependency information below is a template for when publication is configured. For now, add the compiled JAR from [Modrinth](https://modrinth.com/plugin/onlysleep) or [GitHub Releases](https://github.com/DemonZ-Development/Onlysleep/releases) to your project's `libs/` directory and depend on it locally.

#### Using Gradle

```kotlin
dependencies {
    compileOnly(files("libs/Onlysleep-2.0.0.jar"))
}
```

#### Using Maven

```xml
<dependencies>
    <dependency>
        <groupId>com.demonzdevelopment</groupId>
        <artifactId>onlysleep</artifactId>
        <version>2.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/libs/Onlysleep-2.0.0.jar</systemPath>
    </dependency>
</dependencies>
```

#### Soft-Depend in plugin.yml

```yaml
softdepend: [Onlysleep]
```

---

## API Reference

### Getting the Plugin Instance

```java
import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.manager.SleepManager;

Onlysleep plugin = Onlysleep.getInstance();
SleepManager sleepManager = plugin.getSleepManager();
```

### Checking Sleep Status

```java
// Check if a player is currently counted as sleeping
boolean sleeping = sleepManager.isPlayerSleeping(player);

// Check if a night skip is scheduled for a world
World world = player.getWorld();
boolean skipping = sleepManager.isSkipScheduled(world);

// Check if a specific player UUID is sleeping in a world
Set<UUID> sleepingPlayers = sleepManager.getSleepingPlayers(world);
boolean isSleeping = sleepingPlayers != null && sleepingPlayers.contains(player.getUniqueId());
```

### Querying Player Counts

```java
// Get how many players are needed to skip night in this world
int required = sleepManager.getRequiredSleepingCount(world);

// Get how many are currently sleeping in this world
int sleeping = sleepManager.getSleepingCount(world);

// Get the total number of eligible players in this world
int total = sleepManager.getTotalPlayerCount(world);
```

### Getting Configuration

```java
import com.demonzdevelopment.onlysleep.config.ConfigManager;

ConfigManager config = plugin.getConfigManager();

// Check settings
int sleepPercentage = config.getSleepPercentage();
boolean perWorld = config.isPerWorldSleep();
boolean bossBarEnabled = config.isShowBossBar();

// Check if a world is enabled
boolean worldEnabled = config.isWorldEnabled("world");

// Build a progress bar
String bar = config.buildProgressBar(current, max);

// Get formatted messages
String message = config.getMessage("sleep.enough-sleeping", Map.of(
    "player", player.getDisplayName()
));
```

### Platform Detection

```java
import com.demonzdevelopment.onlysleep.util.PlatformAdapter;

// Get the server platform
PlatformAdapter.ServerPlatform platform = plugin.getPlatform();
String platformName = platform.getDisplayName(); // "Folia", "Paper", "Spigot", "Bukkit"

// Static checks
boolean isFolia = PlatformAdapter.isFolia();
boolean isPaper = PlatformAdapter.isPaper();
boolean isSpigot = PlatformAdapter.isSpigot();

// Version info
String minecraftVersion = PlatformAdapter.getMinecraftVersion(); // "1.21.4"
int majorVersion = PlatformAdapter.getMinecraftMajorVersion();   // 21
String apiVersion = PlatformAdapter.getAPIVersion();             // "1.21.4-R0.1-SNAPSHOT"
```

### AFK Tracker

```java
import com.demonzdevelopment.onlysleep.util.AfkTracker;

// Check if a player is AFK
boolean isAfk = AfkTracker.isAfk(player);

// Update a player's activity timestamp (useful for custom triggers)
AfkTracker.updateActivity(player);

// Remove a player from tracking (e.g., on quit)
AfkTracker.removePlayer(player);
```

### Update Checker

```java
import com.demonzdevelopment.onlysleep.util.UpdateChecker;

UpdateChecker checker = plugin.getUpdateChecker();
if (checker != null) {
    // Trigger an async update check
    checker.checkAsync().thenAccept(result -> {
        if (result.isUpdateAvailable()) {
            getLogger().info("New version: " + result.getLatestVersion());
        }
    });
}
```

---

## Full API Example

```java
package com.example.myplugin;

import com.demonzdevelopment.onlysleep.Onlysleep;
import com.demonzdevelopment.onlysleep.config.ConfigManager;
import com.demonzdevelopment.onlysleep.manager.SleepManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MyListener(), this);
    }

    public void printSleepStatus(Player player) {
        Onlysleep onlysleep = Onlysleep.getInstance();
        if (onlysleep == null) {
            player.sendMessage("Onlysleep is not installed!");
            return;
        }

        SleepManager sm = onlysleep.getSleepManager();
        ConfigManager cm = onlysleep.getConfigManager();
        World world = player.getWorld();

        int sleeping = sm.getSleepingCount(world);
        int required = sm.getRequiredSleepingCount(world);
        int total = sm.getTotalPlayerCount(world);
        boolean skipping = sm.isSkipScheduled(world);

        player.sendMessage("§8=== §bSleep Status §8===");
        player.sendMessage("§7Sleeping: §b" + sleeping);
        player.sendMessage("§7Required: §b" + required);
        player.sendMessage("§7Total eligible: §b" + total);
        player.sendMessage("§7Skipping: " + (skipping ? "§aYes" : "§cNo"));
        player.sendMessage("§7Enabled: " + (cm.isWorldEnabled(world.getName()) ? "§aYes" : "§cNo"));
        player.sendMessage("§8=====================");
    }
}
```

---

## Events

Onlysleep fires standard Bukkit events when game states change. You can listen for:

- `PlayerBedEnterEvent` — When a player enters a bed (cancellable, Onlysleep uses HIGHEST priority)
- `PlayerBedLeaveEvent` — When a player leaves a bed
- `PlayerQuitEvent` — When a player disconnects

Onlysleep doesn't currently fire custom events, but you can observe these standard events to detect sleep-related state changes.

---

## Soft Depend Setup

To properly depend on Onlysleep without making it required:

### 1. plugin.yml

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
softdepend: [Onlysleep]
api-version: '1.16'
```

### 2. Check for Onlysleep at runtime

```java
@Override
public void onEnable() {
    if (getServer().getPluginManager().getPlugin("Onlysleep") != null) {
        getLogger().info("Hooked into Onlysleep!");
        Onlysleep onlysleep = Onlysleep.getInstance();
        // Use the API
    } else {
        getLogger().info("Onlysleep not found - sleep integration disabled");
    }
}
```

---

## Internal Architecture

For developers who want to understand the plugin's structure:

```
com.demonzdevelopment.onlysleep/
├── Onlysleep.java                  # Main plugin class
├── config/
│   └── ConfigManager.java          # Configuration management
├── command/
│   └── OnlysleepCommand.java       # Command execution
├── listener/
│   └── SleepListener.java          # Event handling
├── manager/
│   └── SleepManager.java           # Core sleep logic
└── util/
    ├── AfkTracker.java             # Built-in AFK detection
    ├── OfflinePlayerTracker.java   # Offline player caching
    ├── PlatformAdapter.java        # Server platform detection
    ├── SchedulerAdapter.java       # Folia-compatible scheduler
    ├── SleepPlaceholderExpansion.java  # PAPI expansion
    └── UpdateChecker.java          # Version checking
```

The plugin uses a **SchedulerAdapter** pattern to work on both Bukkit/Spigot/Paper and Folia servers. All world-specific tasks go through the region scheduler on Folia, while global tasks use the global region scheduler. On non-Folia servers, it falls back to the standard Bukkit scheduler.

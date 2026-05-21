<div align="center">

![Onlysleep Banner](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/banner.png)

# 🌙 Onlysleep

**Skip the night with just one player sleeping** — or configure it your way.

[![Modrinth](https://img.shields.io/modrinth/dt/onlysleep?color=00d875&label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/onlysleep)
[![Hangar](https://img.shields.io/badge/Hangar-Download-blue?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBmaWxsPSIjZmZmIiBkPSJNOCAwQTMuNTggMy41OCAwIDAgMCA0LjUgMy41djQuNUgzdjguNWgxMFY4aC0xLjVWMy41QTMuNTggMy41OCAwIDAgMCA4IDB6bTAgMWEyLjUgMi41IDAgMCAxIDIuNSAyLjV2NC41aC01VjMuNUEyLjUgMi41IDAgMCAxIDggMXoiLz48L3N2Zz4=)](https://hangar.papermc.io/DemonzDevelopment/Onlysleep)
[![Spigot](https://img.shields.io/badge/Spigot-Resource-orange?logo=spigotmc)](https://www.spigotmc.org/resources/onlysleep.12345/)
[![bStats](https://img.shields.io/badge/bStats-31415-ff69b4)](https://bstats.org/plugin/bukkit/OnlySleep/31415)
[![Build](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/build.yml/badge.svg)](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/build.yml)
[![CodeQL](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/codeql.yml/badge.svg)](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/codeql.yml)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**Supports:** Bukkit · Spigot · Paper · Purpur · Folia · Any Paper fork

</div>

---

## ✨ Features

- **One-Player Sleep** — Default mode lets a single player skip the night
- **Configurable Percentage** — Require any % of players to sleep (50%, 75%, 100%, etc.)
- **Per-World Sleep** — Configure per-world or global sleep counting
- **Multi-Platform** — Works on Bukkit, Spigot, Paper, Folia, and all derivatives
- **Weather Skip** — Automatically clear storms and thunderstorms
- **Visual Feedback** — Boss bar, action bar, progress bar, and title support
- **Sound Effects** — Configurable sounds when night is skipped
- **Smart Player Filtering** — Ignores AFK, spectators, exempt players, and more
- **AFK Detection** — Supports EssentialsX and CMI AFK status
- **PlaceholderAPI Support** — Over 10 placeholders for integrations
- **Update Checker** — Automatically checks for new versions
- **bStats Metrics** — Anonymous usage statistics
- **Disable Per World** — Disable sleep skipping in specific worlds

---

## 📥 Installation

1. **Download** the latest `Onlysleep-*.jar` from [Modrinth](https://modrinth.com/plugin/onlysleep), [Hangar](https://hangar.papermc.io/DemonzDevelopment/Onlysleep), or [Spigot](https://www.spigotmc.org/resources/onlysleep.12345/)
2. **Place** it in your server's `plugins/` folder
3. **Restart** your server (or reload with `/reload` — though restart is recommended)
4. **Configure** `plugins/Onlysleep/config.yml` to your liking
5. **Reload** with `/onlysleep reload`

### Requirements

- **Java 21+** (The plugin requires Java 21+ to run)
- **Minecraft 1.16.5+** (Bukkit, Spigot, Paper, Folia, or any compatible server)
- **No other plugins required!** (Works standalone)
- **[Optional] PlaceholderAPI** for placeholder expansion support

---

## ⚙️ Configuration

### Quick Start

The plugin works out-of-the-box with sensible defaults. Just drop it in and go!

### Main Settings (`config.yml`)

| Setting | Default | Description |
|---------|---------|-------------|
| `sleep-percentage` | `50` | % of players needed to sleep (0 = one player) |
| `skip-delay-ticks` | `60` | Delay before night skip (20 ticks = 1 second) |
| `skip-type` | `instant` | `instant`, `speed`, or `gradual` |
| `per-world-sleep` | `true` | Only count players in the same world |
| `clear-weather` | `true` | Clear storms when sleeping |
| `manage-gamerule` | `true` | Auto-manage `playersSleepingPercentage` gamerule |

See the full [config.yml](src/main/resources/config.yml) for all options.

### Messages (`messages.yml`)

All messages are fully customizable with color codes (`&0`-`&f`) and placeholders.

---

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/onlysleep` | Show help | `onlysleep.command` |
| `/onlysleep help` | Show help page | `onlysleep.command` |
| `/onlysleep info` | Plugin information | `onlysleep.info` |
| `/onlysleep status` | Detailed status | `onlysleep.status` |
| `/onlysleep reload` | Reload configuration | `onlysleep.reload` |

**Aliases:** `/os`, `/sleep`

---

## 🔐 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `onlysleep.*` | OP | All permissions |
| `onlysleep.command` | Everyone | Use `/onlysleep` command |
| `onlysleep.info` | OP | View plugin information |
| `onlysleep.reload` | OP | Reload configuration |
| `onlysleep.status` | OP | View plugin status |
| `onlysleep.exempt` | None | Excluded from sleep calculations (operators sleep by default) |
| `onlysleep.update` | OP | Receives update notifications |

---

## 📊 PlaceholderAPI

Onlysleep provides **12+ placeholders** when [PlaceholderAPI](https://placeholderapi.com/) is installed.

| Placeholder | Description |
|-------------|-------------|
| `%onlysleep_sleeping%` | Number of sleeping players in player's world |
| `%onlysleep_required%` | Number of players needed to skip night |
| `%onlysleep_percentage%` | Configured sleep percentage |
| `%onlysleep_total%` | Total eligible player count in the world |
| `%onlysleep_progress%` | Percentage of required sleepers achieved (0-100) |
| `%onlysleep_progress_bar%` | Visual progress bar of sleep progress |
| `%onlysleep_sleeping_names%` | Comma-separated names of sleeping players |
| `%onlysleep_status%` | "Sleeping" or "Awake" for the player |
| `%onlysleep_is_sleeping%` | `true`/`false` if player is sleeping |
| `%onlysleep_is_night%` | `true`/`false` if it's night |
| `%onlysleep_is_sleepable%` | `true`/`false` if it's night/storm in the world |
| `%onlysleep_skipping%` | `true`/`false` if night is being skipped |
| `%onlysleep_enabled%` | `true`/`false` if sleeping is enabled in player's world |
| `%onlysleep_afk%` | `true`/`false` if the player is AFK |
| `%onlysleep_version%` | Plugin version |
| `%onlysleep_platform%` | Server platform (Folia, Paper, Spigot, Bukkit) |
| `%onlysleep_world_sleeping_<world>%` | Sleeping count in specific world |
| `%onlysleep_world_required_<world>%` | Required count in specific world |
| `%onlysleep_world_total_<world>%` | Total eligible in specific world |

---

## 🏗️ Building from Source

### Gradle (Recommended)

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
./gradlew clean build
```

The compiled JAR will be in `build/libs/`.

### Maven (Alternative)

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
mvn clean package
```

The compiled JAR will be in `target/`.

---

## 🔌 API

Onlysleep provides a simple API for other plugins:

```java
// Get the plugin instance
Onlysleep plugin = Onlysleep.getInstance();

// Get sleep manager
SleepManager sleepManager = plugin.getSleepManager();

// Check if a player is currently counted as sleeping
boolean sleeping = sleepManager.isPlayerSleeping(player);

// Get how many players are needed in a world
int required = sleepManager.getRequiredSleepingCount(world);

// Get how many are currently sleeping in a world
int sleeping = sleepManager.getSleepingCount(world);

// Check if a night skip is scheduled
boolean skipping = sleepManager.isSkipScheduled(world);
```

---

## 📊 bStats

This plugin uses [bStats](https://bstats.org/plugin/bukkit/OnlySleep/31415) to collect anonymous usage statistics. No personal data is collected. You can opt-out in `plugins/bstats/config.yml`.

### Live Statistics

[![bStats Servers](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

---

## 🤝 Support & Links

- 🌐 [Website](https://demonzdevelopment.online)
- 💻 [GitHub](https://github.com/DemonZ-Development/Onlysleep)
- 🎮 [Modrinth](https://modrinth.com/plugin/onlysleep)
- 🐦 [Twitter / X](https://x.com/DemonZ_Dev)
- 🎥 [YouTube](https://www.youtube.com/@DemonzDevelopment)
- 📸 [Instagram](https://www.instagram.com/demonzdevelopement)
- 🔑 [Discord](https://discord.gg/qkvkEaPryF)
- 📋 [Reddit](https://www.reddit.com/r/DemonZDevelopment/)
- 📧 demonzdevelopment@gmail.com

---

## ❤️ Credits

Developed by **Demonz Development** with love for the Minecraft community.

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

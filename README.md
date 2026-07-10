<div align="center">

# Onlysleep

Skip the night with just one player sleeping — or configure it your way.

[![Modrinth](https://img.shields.io/modrinth/dt/onlysleep?color=00d875&label=Modrinth&logo=modrinth)](https://modrinth.com/plugin/onlysleep)
[![Hangar](https://img.shields.io/badge/Hangar-Download-blue?logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBmaWxsPSIjZmZmIiBkPSJNOCAwQTMuNTggMy41OCAwIDAgMCA0LjUgMy41djQuNUgzdjguNWgxMFY4aC0xLjVWMy41QTMuNTggMy41OCAwIDAgMCA4IDB6bTAgMWEyLjUgMi41IDAgMCAxIDIuNSAyLjV2NC41aC01VjMuNUEyLjUgMi41IDAgMCAxIDggMXoiLz48L3N2Zz4=)](https://hangar.papermc.io/DemonzDevelopment/Onlysleep)
[![bStats](https://img.shields.io/badge/bStats-31415-ff69b4)](https://bstats.org/plugin/bukkit/OnlySleep/31415)
[![Build](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/build.yml/badge.svg)](https://github.com/DemonZ-Development/Onlysleep/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**Supports:** Bukkit · Spigot · Paper · Purpur · Folia · any Paper fork (1.16.5+, Java 21+)

</div>

---

## Sponsored By

<div align="center">
  <a href="https://nexeu.zip">
    <img src="https://whodoesntloveavatars.s3.fra.databucket.eu/assets/promo.png" alt="Nexeu Sponsor" width="600px">
  </a>
  <br>
  Looking for high-performance, budget-friendly game server hosting? Check out <a href="https://nexeu.zip"><b>Nexeu Hosting</b></a>!
</div>

---

## Features

- One-player sleep by default, or require any percentage (0–100%)
- Per-world or global sleep counting
- Weather skip: clear rain and thunder independently
- Visual feedback: boss bar, action bar, progress bar, titles
- Configurable sounds on bed-enter and night-skip
- Smart filtering: AFK (EssentialsX & CMI), spectators, exempt permissions, disabled gamemodes
- Gamerule management: automatically pins `playersSleepingPercentage` so the plugin fully controls sleep math
- PlaceholderAPI integration (12+ placeholders)
- Update checker and bStats (anonymous, opt-out available)
- Per-world disable

## Installation

1. Download the latest `Onlysleep-*.jar` from [Modrinth](https://modrinth.com/plugin/onlysleep), [Hangar](https://hangar.papermc.io/DemonzDevelopment/Onlysleep), or the [GitHub releases](https://github.com/DemonZ-Development/Onlysleep/releases).
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. Tune `plugins/Onlysleep/config.yml`.
5. Apply changes with `/onlysleep reload`.

**Requirements:** Java 21+, Minecraft 1.16.5+. Works standalone; PlaceholderAPI is optional.

## Configuration

Defaults work out-of-the-box. Key settings in `config.yml`:

| Setting | Default | Description |
|---------|---------|-------------|
| `sleep-percentage` | `50` | % of players needed to sleep (0 = one player) |
| `skip-delay-ticks` | `60` | Delay before night skip (20 ticks = 1 second) |
| `skip-type` | `instant` | `instant`, `speed`, or `gradual` |
| `per-world-sleep` | `true` | Only count players in the same world |
| `clear-weather` | `true` | Clear rain when sleeping |
| `clear-thunder` | `true` | Clear thunder independently of `clear-weather` |
| `manage-gamerule` | `true` | Auto-manage `playersSleepingPercentage` gamerule |

See the full [`config.yml`](src/main/resources/config.yml) for all options and [`messages.yml`](src/main/resources/messages.yml) for message customization.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/onlysleep` | Show help | `onlysleep.command` |
| `/onlysleep help` | Show help page | `onlysleep.command` |
| `/onlysleep info` | Plugin information | `onlysleep.info` |
| `/onlysleep status` | Detailed status | `onlysleep.status` |
| `/onlysleep reload` | Reload configuration | `onlysleep.reload` |

**Aliases:** `/os`, `/sleep`

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `onlysleep.*` | OP | All permissions |
| `onlysleep.command` | Everyone | Use `/onlysleep` |
| `onlysleep.info` | OP | View plugin info |
| `onlysleep.reload` | OP | Reload config |
| `onlysleep.status` | OP | View status |
| `onlysleep.exempt` | None | Excluded from sleep calculations (operators sleep by default) |
| `onlysleep.update` | OP | Receives update notifications |

## PlaceholderAPI

When [PlaceholderAPI](https://placeholderapi.com/) is installed, the following placeholders are available:

| Placeholder | Description |
|-------------|-------------|
| `%onlysleep_sleeping%` | Sleeping players in the player's world |
| `%onlysleep_required%` | Players needed to skip the night |
| `%onlysleep_percentage%` | Configured sleep percentage |
| `%onlysleep_total%` | Total eligible players in the world |
| `%onlysleep_progress%` | % of required sleepers achieved (0–100) |
| `%onlysleep_progress_bar%` | Visual progress bar |
| `%onlysleep_sleeping_names%` | Comma-separated names of sleeping players |
| `%onlysleep_status%` | "Sleeping" or "Awake" |
| `%onlysleep_is_sleeping%` | `true`/`false` |
| `%onlysleep_is_night%` | `true`/`false` if it's night |
| `%onlysleep_is_sleepable%` | `true`/`false` if night/storm |
| `%onlysleep_skipping%` | `true`/`false` if a skip is scheduled |
| `%onlysleep_enabled%` | `true`/`false` if sleeping is enabled in the world |
| `%onlysleep_afk%` | `true`/`false` if the player is AFK |
| `%onlysleep_version%` | Plugin version |
| `%onlysleep_platform%` | Server platform (Folia/Paper/Spigot/Bukkit) |
| `%onlysleep_world_sleeping_<world>%` | Sleeping count in a specific world |
| `%onlysleep_world_required_<world>%` | Required count in a specific world |
| `%onlysleep_world_total_<world>%` | Total eligible in a specific world |

## Building

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
./gradlew clean build
```

The compiled JAR is placed in `build/libs/`.

## API

```java
Onlysleep plugin = Onlysleep.getInstance();
SleepManager sleepManager = plugin.getSleepManager();

boolean sleeping = sleepManager.isPlayerSleeping(player);
int required = sleepManager.getRequiredSleepingCount(world);
int current  = sleepManager.getSleepingCount(world);
boolean skipping = sleepManager.isSkipScheduled(world);
```

## bStats

This plugin uses [bStats](https://bstats.org/plugin/bukkit/OnlySleep/31415) to collect anonymous usage statistics. No personal data is collected. You can opt out in `plugins/bStats/config.yml`.

[![bStats](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

## Links

- [Website](https://demonzdevelopment.online)
- [GitHub](https://github.com/DemonZ-Development/Onlysleep)
- [Modrinth](https://modrinth.com/plugin/onlysleep)
- [Discord](https://discord.gg/qkvkEaPryF)
- [Twitter / X](https://x.com/DemonZ_Dev)
- [YouTube](https://www.youtube.com/@DemonzDevelopment)
- [demonzdevelopment@gmail.com](mailto:demonzdevelopment@gmail.com)

## License

MIT — see [LICENSE](LICENSE).

# Onlysleep

<div align="center">

Skip the night with just one player sleeping — or configure it your way.

**Supports:** Bukkit · Spigot · Paper · Purpur · Folia · any Paper fork
**Minecraft:** 1.16.5+ · **Java:** 21+

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
- Multi-platform: Bukkit, Spigot, Paper, Folia, and forks
- Weather skip: clear rain and thunder independently
- Visual feedback: boss bar, action bar, progress bar, titles
- Configurable sounds on bed-enter and night-skip
- Smart filtering: AFK (EssentialsX & CMI), spectators, exempt permissions, disabled gamemodes
- Gamerule management for `playersSleepingPercentage`
- PlaceholderAPI: 12+ placeholders
- Update checker and bStats (opt-out available)
- Per-world disable
- Full Folia regionized-scheduler support

## Quick Start

1. Download the latest `Onlysleep-*.jar` from [Modrinth](https://modrinth.com/plugin/onlysleep), [Hangar](https://hangar.papermc.io/DemonzDevelopment/Onlysleep), or the [GitHub releases](https://github.com/DemonZ-Development/Onlysleep/releases).
2. Place it in your server's `plugins/` folder.
3. Restart your server.
4. Configure `plugins/Onlysleep/config.yml`.
5. Reload with `/onlysleep reload`.

> No dependencies required. PlaceholderAPI is optional.

## Basic Commands

| Command | Description |
|---------|-------------|
| `/onlysleep help` | Show help page |
| `/onlysleep info` | Show plugin information |
| `/onlysleep status` | Detailed status (requires `onlysleep.status`) |
| `/onlysleep reload` | Reload configuration (requires `onlysleep.reload`) |

**Aliases:** `/os`, `/sleep`

## Wiki Pages

- [Installation](Installation)
- [Configuration](Configuration)
- [Messages](Messages)
- [Commands & Permissions](Commands-and-Permissions)
- [Placeholders](Placeholders)
- [Developer API](Developer-API)
- [FAQ & Troubleshooting](FAQ)
- [Building from Source](Building)
- [Changelog](Changelog)

## bStats

This plugin uses [bStats](https://bstats.org/plugin/bukkit/OnlySleep/31415) to collect anonymous usage statistics. No personal data is collected. You can opt out in `plugins/bStats/config.yml`.

[![bStats](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

## Support

- [GitHub Issues](https://github.com/DemonZ-Development/Onlysleep/issues)
- [Discord](https://discord.gg/qkvkEaPryF)

Licensed under the [MIT License](https://github.com/DemonZ-Development/Onlysleep/blob/master/LICENSE).

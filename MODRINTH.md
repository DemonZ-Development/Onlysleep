# Onlysleep

Skip the night with just one player sleeping — fully configurable, lightweight, and compatible with every major Minecraft server platform.

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

- One-player sleep by default, or configure any percentage
- Multi-platform: Bukkit, Spigot, Paper, Purpur, Folia, and forks
- Per-world or global sleep counting
- Weather skip: clear rain and thunder independently
- Visual feedback: boss bar, action bar, progress bar, titles
- Configurable sounds on bed-enter and night-skip
- Smart filtering: AFK detection (EssentialsX & CMI), spectators, exempt permissions, disabled gamemodes
- Gamerule management for `playersSleepingPercentage`
- PlaceholderAPI: 12+ placeholders
- Update checker and bStats (opt-out available)
- Per-world disable

## Installation

1. Download the JAR.
2. Place it in your `plugins/` folder.
3. Restart your server.
4. Configure `plugins/Onlysleep/config.yml`.
5. Reload with `/onlysleep reload`.

**Requirements:** Java 21+, Minecraft 1.16.5+. Works standalone; PlaceholderAPI is optional.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/onlysleep` | Show help | `onlysleep.command` |
| `/onlysleep reload` | Reload config | `onlysleep.reload` |
| `/onlysleep info` | Plugin info | `onlysleep.info` |
| `/onlysleep status` | Status overview | `onlysleep.status` |

**Aliases:** `/os`, `/sleep`

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `onlysleep.*` | OP | All permissions |
| `onlysleep.command` | Everyone | Use commands |
| `onlysleep.info` | OP | View plugin info |
| `onlysleep.reload` | OP | Reload config |
| `onlysleep.status` | OP | View status |
| `onlysleep.exempt` | None | Excluded from sleep (operators sleep by default) |
| `onlysleep.update` | OP | Update alerts |

## bStats

[![bStats](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

[View statistics](https://bstats.org/plugin/bukkit/OnlySleep/31415)

## Links

- [Website](https://demonzdevelopment.online)
- [GitHub](https://github.com/DemonZ-Development/Onlysleep)
- [Discord](https://discord.gg/qkvkEaPryF)
- [Twitter / X](https://x.com/DemonZ_Dev)
- [YouTube](https://www.youtube.com/@DemonzDevelopment)
- [demonzdevelopment@gmail.com](mailto:demonzdevelopment@gmail.com)

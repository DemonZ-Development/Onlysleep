# Onlysleep

**One player sleeps. Everyone wakes up.**

A lightweight sleep plugin that does its job and gets out of the way. No complicated setup, no wall of config you'll never touch. Drop the JAR in, restart, and your players can actually skip the night.

---

## Features

- One-player sleep by default, or any percentage you want
- Per-world sleep (each world tracks separately)
- Weather skip: clear rain and thunder independently
- Boss bar, action bar, progress bar, and title support
- Sounds when someone starts sleeping and when night is skipped
- AFK detection (built-in, plus EssentialsX and CMI)
- Filters out spectators, creative, flying, and exempt players
- Automatic gamerule management for `playersSleepingPercentage`
- PlaceholderAPI: 12+ placeholders
- Update checker and bStats (opt-out available)
- Full Folia support with regionized scheduler

## Commands

- `/onlysleep` - Show help
- `/onlysleep help` - Show help page
- `/onlysleep reload` - Reload configuration
- `/onlysleep info` - Show plugin information
- `/onlysleep status` - Show detailed plugin status
- `/onlysleep update` - Check for updates
- `/onlysleep set|get|toggle` - Manage configuration in game
- `/onlysleep world|gamemode` - Manage excluded worlds and game modes
- `/onlysleep dump [paste]` - Create a diagnostic dump

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

## Installation

1. Download the JAR.
2. Drop it into your `plugins/` folder.
3. Restart your server.
4. Edit `plugins/Onlysleep/config.yml` if you want to tweak anything.
5. Apply changes with `/onlysleep reload`.

**Requirements:** Minecraft 26.2 and Java 25+. Works standalone. PlaceholderAPI is optional.

## Configuration

Defaults work fine. The main knob is `sleep-percentage` in `config.yml`:

- `0` = one player sleeps, everyone wakes up
- `50` = half the server needs to be in bed
- `100` = everyone has to sleep

Set `per-world-sleep: false` if you want global counting across all worlds.

See [`config.yml`](src/main/resources/config.yml) for every option and [`messages.yml`](src/main/resources/messages.yml) for message customization.

## bStats

[![bStats](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

Anonymous stats only. No personal data. Opt out in `plugins/bStats/config.yml`.

## Links

[Modrinth](https://modrinth.com/plugin/onlysleep) | [GitHub](https://github.com/DemonZ-Development/Onlysleep) | [Issues](https://github.com/DemonZ-Development/Onlysleep/issues) | [Discord](https://discord.gg/qkvkEaPryF) | [Website](https://demonzdevelopment.online) | [Twitter / X](https://x.com/DemonZ_Dev) | [YouTube](https://www.youtube.com/@DemonzDevelopment) | [Instagram](https://www.instagram.com/demonzdevelopement) | [Reddit](https://www.reddit.com/r/DemonZDevelopment/) | [demonzdevelopment@gmail.com](mailto:demonzdevelopment@gmail.com)

---

## Sponsored By

<div align="center">
  <a href="https://nexeu.zip">
    <img src="https://whodoesntloveavatars.s3.fra.databucket.eu/assets/promo.png" alt="Nexeu Sponsor" width="600px">
  </a>
  <br>
  Looking for high-performance, budget-friendly game server hosting? Check out <a href="https://nexeu.zip"><b>Nexeu Hosting</b></a>!
</div>

![Onlysleep](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth-page.png)

# Onlysleep

**One player sleeps. Everyone wakes up.**

Onlysleep lets your server skip the night without waiting for every player to find a bed. One sleeper is enough by default, but you can require any percentage from 0 to 100. Install the JAR, restart the server, and the default configuration is ready to use.

---

![Features](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/features-heading.png)

- One-player sleep by default, with configurable percentage thresholds
- Separate sleep counts for each world
- Independent controls for clearing rain and thunder
- Boss bar, action bar, title, and progress-bar feedback
- Configurable sounds for bed entry and successful night skips
- Built-in AFK tracking, plus EssentialsX and CMI support
- Filters for spectators, creative players, flying players, game modes, and exempt permissions
- Automatic handling of the `playersSleepingPercentage` gamerule
- PlaceholderAPI support for sleep, world, and status data
- Optional update checks and anonymous bStats metrics

---


![Installation](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/installation-heading.png)

![Installation Steps](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/installation-steps.png)

**Requirements:** Minecraft 26.2 and Java 25 or newer. Onlysleep works on its own; PlaceholderAPI is optional.

---


![Commands](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/commands-heading.png)

![Commands Preview](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/commands-preview.png)

**Aliases:** `/os`, `/sleep`

---


![Permissions](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/permissions-heading.png)

| Permission | Default | Description |
|------------|---------|-------------|
| `onlysleep.*` | OP | All permissions |
| `onlysleep.command` | Everyone | Use commands |
| `onlysleep.info` | OP | View plugin info |
| `onlysleep.reload` | OP | Reload config |
| `onlysleep.status` | OP | View status |
| `onlysleep.exempt` | None | Excluded from sleep (operators sleep by default) |
| `onlysleep.update` | OP | Update alerts |

---


![Configuration](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/config-preview.png)

You can use the defaults as-is. The main setting is `sleep-percentage` in `config.yml`:

- `0` = one player sleeps, everyone wakes up
- `50` = half the server needs to be in bed
- `100` = everyone has to sleep

Set `per-world-sleep: false` if you want global counting across all worlds.

See [`config.yml`](https://github.com/DemonZ-Development/Onlysleep/blob/master/src/main/resources/config.yml) for every option and [`messages.yml`](https://github.com/DemonZ-Development/Onlysleep/blob/master/src/main/resources/messages.yml) for message customization.

---


![PlaceholderAPI](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/placeholders-heading.png)

![PlaceholderAPI Preview](https://raw.githubusercontent.com/DemonZ-Development/Onlysleep/master/assets/modrinth/placeholders-preview.png)

If you have [PlaceholderAPI](https://placeholderapi.com/) installed, these placeholders are available:

| Placeholder | Description |
|-------------|-------------|
| `%onlysleep_sleeping%` | Sleeping players in the player's world |
| `%onlysleep_required%` | Players needed to skip the night |
| `%onlysleep_percentage%` | Configured sleep percentage |
| `%onlysleep_total%` | Total eligible players in the world |
| `%onlysleep_progress%` | % of required sleepers achieved (0-100) |
| `%onlysleep_progress_bar%` | Visual progress bar |
| `%onlysleep_sleeping_names%` | Names of sleeping players |
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

---

## Building

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
./gradlew clean build
```

The compiled JAR is in `build/libs/`.

## bStats

[![bStats](https://bstats.org/signatures/bukkit/OnlySleep.svg)](https://bstats.org/plugin/bukkit/OnlySleep/31415)

Onlysleep uses bStats for anonymous usage metrics. You can opt out in `plugins/bStats/config.yml`.

## Links

- [Website](https://demonzdevelopment.online)
- [GitHub](https://github.com/DemonZ-Development/Onlysleep)
- [Discord](https://discord.gg/qkvkEaPryF)
- [Twitter / X](https://x.com/DemonZ_Dev)
- [YouTube](https://www.youtube.com/@DemonzDevelopment)
- [demonzdevelopment@gmail.com](mailto:demonzdevelopment@gmail.com)

---

## Sponsored By

<div align="center">
  <a href="https://nexeu.zip">
    <img src="https://whodoesntloveavatars.s3.fra.databucket.eu/assets/promo.png" alt="Nexeu Sponsor" width="600px">
  </a>
  <br>
  Server hosting for this project is sponsored by <a href="https://nexeu.zip"><b>Nexeu Hosting</b></a>.
</div>

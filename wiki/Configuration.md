# ⚙️ Configuration

Onlysleep's configuration is located at `plugins/Onlysleep/config.yml`. The plugin works out-of-the-box with sensible defaults, so you only need to change what you want.

> **Tip:** After editing config.yml, run `/onlysleep reload` to apply changes without restarting the server.

---

## Sleep Settings

```yaml
sleep-percentage: 50
```

The percentage of eligible players that need to sleep to skip the night.

| Value | Behavior |
|-------|----------|
| `0` | One-player sleep (any single player can skip) |
| `50` | Half of eligible players (default) |
| `100` | All eligible players must sleep |
| Any % | Calculates required count: `ceil(total × percentage / 100)` |

> **Note:** The actual player count is calculated per-world (if `per-world-sleep: true`). Players with `onlysleep.exempt` permission are excluded from the count. See [Player Filtering](#player-filtering) below.

---

```yaml
skip-delay-ticks: 60
```

Delay in ticks before the night is skipped after enough players sleep.

- **20 ticks = 1 second**
- Default: `60` (3 seconds)
- Set to `0` for instant skip
- **Note:** If any sleeping player gets out of bed during this delay, the night skip is cancelled

---

```yaml
skip-type: instant
```

The type of night skip animation.

| Value | Description |
|-------|-------------|
| `instant` | Time changes immediately to morning |
| `speed` | Time fast-forwards (adds 24000 ticks to set to next day) |
| `gradual` | Time smoothly transitions (uses `gradual-skip-speed-ticks`) |

---

```yaml
gradual-skip-speed-ticks: 30
```

How fast time passes during a gradual skip (higher = faster). Only used when `skip-type: gradual`.

---

```yaml
morning-time: 1000
```

Time of day to set after skipping (in Minecraft ticks).

| Value | Time of Day |
|-------|-------------|
| `0` | Dawn (sunrise) |
| `1000` | Morning (default) |
| `6000` | Noon |
| `12000` | Dusk |
| `18000` | Night |

---

```yaml
reset-time: true
```

If `true`, sets time to `0` (dawn) after skipping, ignoring `morning-time`.
If `false`, uses the `morning-time` value above.

---

```yaml
per-world-sleep: true
```

If `true`, only counts players in the same world as the sleeping player(s).
If `false`, counts players across all worlds.

> **Example:** With `per-world-sleep: true` and a 50% requirement in a world with 10 eligible players, 5 players from that world need to sleep. Players in other worlds don't count.

---

```yaml
require-all-players-online: false
```

If `true`, all eligible players must be online before sleep counting can begin.
If any eligible player is offline, the required count is set to `Integer.MAX_VALUE` (effectively preventing skip until all eligible players are online).

> **Performance note:** When enabled, Onlysleep uses an `OfflinePlayerTracker` that caches the offline player count asynchronously to avoid expensive disk I/O on every bed enter.

---

## Weather Settings

```yaml
clear-weather: true
reset-weather: true
clear-thunder: true
reset-thunder: true
reset-weather-cycle: true
```

Controls whether storms and thunderstorms are cleared when a player sleeps.

| Setting | Description |
|---------|-------------|
| `clear-weather` | Clear rain/thunder when a player sleeps during a storm |
| `reset-weather` | Actually set rain to clear after skipping |
| `clear-thunder` | Clear thunder specifically |
| `reset-thunder` | Actually set thunder to clear after skipping |
| `reset-weather-cycle` | Reset the weather cycle duration (prevents immediate rain after clearing) |

---

## Player Filtering

```yaml
count-afk-as-sleeping: false
```

If `true`, AFK players are counted as sleeping for the percentage calculation, even if they aren't in bed. This helps progress the night skip when AFK players aren't contributing.

---

```yaml
exclude-afk-from-total: true
```

If `true`, AFK players are excluded from the total player count. This prevents AFK players from making it harder to skip the night (since they won't sleep).

> **Common combination:** `count-afk-as-sleeping: false` + `exclude-afk-from-total: true` = AFK players are ignored entirely (don't count toward required, don't count as sleeping).

---

```yaml
count-spectators: false
```

If `true`, spectator mode players are included in the total eligible count.

---

```yaml
count-flying: true
```

If `true`, flying players are included in the total eligible count.

---

```yaml
ignore-creative-mode: false
```

If `true`, creative mode players are completely ignored (not counted toward required, not allowed to sleep-skip).

---

## AFK Detection

```yaml
afk-detection:
  use-essentials: true
  use-cmi: true
  time-seconds: 300
```

| Setting | Description |
|---------|-------------|
| `use-essentials` | Check [EssentialsX](https://essentialsx.net/) AFK metadata |
| `use-cmi` | Check [CMI](https://www.zrips.net/cmi/) AFK status |
| `time-seconds` | Time in seconds before a player is considered AFK by the built-in AFK tracker. Set to `0` to disable built-in AFK detection (EssentialsX/CMI AFK still works) |

The AFK check order is:
1. Built-in AFK tracker (movement + interaction based)
2. EssentialsX metadata (`afk` metadata value)
3. CMI API (`getCMIPlayer().getAfkData().isAfk()`)

---

## UI Settings

### Action Bar

```yaml
ui:
  action-bar:
    enabled: true
```

Shows a message in the action bar (above the hotbar) to sleeping players while the night skip is pending.

### Boss Bar

```yaml
ui:
  boss-bar:
    enabled: true
    color: BLUE
    style: SOLID
```

Shows a boss bar to all players in the world when night is being skipped.

| Setting | Options |
|---------|---------|
| `color` | `BLUE`, `GREEN`, `PINK`, `PURPLE`, `RED`, `WHITE`, `YELLOW` |
| `style` | `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, `SEGMENTED_20` |

### Progress Bar

```yaml
ui:
  progress-bar:
    enabled: true
    symbol: "■"
    length: 20
```

A visual progress bar in the action bar showing sleep progress. Example output when half complete:

```
Sleeping... ■■■■■■■■■■░░░░░░░░░░ (5/10)
```

| Setting | Description |
|---------|-------------|
| `symbol` | Character used for progress bar segments |
| `length` | Total length of the progress bar in segments |

### Title

```yaml
ui:
  title:
    enabled: false
    title: "&bGood Morning!"
    subtitle: "&fNight skipped by &b%player%"
    fade-in: 10
    stay: 70
    fade-out: 20
```

Shows a title/subtitle to all players when night is skipped.

| Setting | Description |
|---------|-------------|
| `title` | Main title text (supports `%player%` placeholder) |
| `subtitle` | Subtitle text (supports `%player%` placeholder) |
| `fade-in` | Fade-in duration in ticks |
| `stay` | Stay duration in ticks |
| `fade-out` | Fade-out duration in ticks |

Color codes (`&0`-`&f`) and formatting (`&l`, `&o`, etc.) are supported.

---

## Sound Settings

```yaml
sounds:
  enabled: true
  skip-sound: ENTITY_PLAYER_LEVELUP
  skip-sound-volume: 1.0
  skip-sound-pitch: 1.0
  night-sound: ENTITY_PLAYER_LEVELUP
  night-sound-volume: 0.5
  night-sound-pitch: 1.0
  storm-sound: ENTITY_LIGHTNING_BOLT_THUNDER
  storm-sound-volume: 1.0
  storm-sound-pitch: 1.0
```

| Setting | Description |
|---------|-------------|
| `enabled` | Master toggle for all sounds |
| `skip-sound` | Sound played when the night skip completes |
| `night-sound` | Alternative night skip sound |
| `storm-sound` | Sound played during storm skip |

Any valid [Bukkit Sound](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html) enum value can be used.

---

## Gamerule Management

```yaml
manage-gamerule: true
```

If `true`, Onlysleep will automatically manage the `playersSleepingPercentage` gamerule. This is useful for servers where Onlysleep's custom logic should override Minecraft's default sleep behavior.

---

## Update Checker

```yaml
check-for-updates: true
```

If `true`, Onlysleep checks for newer versions on startup via the Modrinth API. Updates are logged to console and notified to players with the `onlysleep.update` permission.

---

## Disabled Worlds

```yaml
disabled-worlds: []
```

A list of world names where sleep skipping is disabled entirely.

```yaml
disabled-worlds:
  - "world_nether"
  - "world_the_end"
```

Players in disabled worlds will see the message: `"Sleep skipping is disabled in this world."`

---

## Disabled Game Modes

```yaml
disabled-gamemodes: []
```

A list of game modes where sleep skipping is disabled for players.

```yaml
disabled-gamemodes:
  - "SPECTATOR"
```

| Option | Description |
|--------|-------------|
| `SURVIVAL` | Disable for survival mode players |
| `CREATIVE` | Disable for creative mode players |
| `ADVENTURE` | Disable for adventure mode players |
| `SPECTATOR` | Disable for spectator mode players |

---

## Complete Default Configuration

<details>
<summary>Click to expand the full default config.yml</summary>

```yaml
# --- Sleep Settings ---
sleep-percentage: 50
skip-delay-ticks: 60
skip-type: instant
gradual-skip-speed-ticks: 30
morning-time: 1000
reset-time: true
per-world-sleep: true
require-all-players-online: false

# --- Weather Settings ---
clear-weather: true
reset-weather: true
clear-thunder: true
reset-thunder: true
reset-weather-cycle: true

# --- Player Filtering ---
count-afk-as-sleeping: false
exclude-afk-from-total: true
count-spectators: false
count-flying: true
ignore-creative-mode: false

# --- AFK Detection ---
afk-detection:
  use-essentials: true
  use-cmi: true
  time-seconds: 300

# --- UI Settings ---
ui:
  action-bar:
    enabled: true
  boss-bar:
    enabled: true
    color: BLUE
    style: SOLID
  progress-bar:
    enabled: true
    symbol: "■"
    length: 20
  title:
    enabled: false
    title: "&bGood Morning!"
    subtitle: "&fNight skipped by &b%player%"
    fade-in: 10
    stay: 70
    fade-out: 20

# --- Sound Settings ---
sounds:
  enabled: true
  skip-sound: ENTITY_PLAYER_LEVELUP
  skip-sound-volume: 1.0
  skip-sound-pitch: 1.0
  night-sound: ENTITY_PLAYER_LEVELUP
  night-sound-volume: 0.5
  night-sound-pitch: 1.0
  storm-sound: ENTITY_LIGHTNING_BOLT_THUNDER
  storm-sound-volume: 1.0
  storm-sound-pitch: 1.0

# --- Gamerule Management ---
manage-gamerule: true

# --- Update Checker ---
check-for-updates: true

# --- Disabled Worlds ---
disabled-worlds: []

# --- Disabled Game Modes ---
disabled-gamemodes: []
```

</details>

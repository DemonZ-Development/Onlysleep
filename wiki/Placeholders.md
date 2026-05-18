# 📊 PlaceholderAPI Placeholders

Onlysleep provides **19+ placeholders** when [PlaceholderAPI](https://placeholderapi.com/) is installed. Placeholders are automatically registered when Onlysleep detects PlaceholderAPI on your server.

---

## Setup

1. Install [PlaceholderAPI](https://placeholderapi.com/) on your server
2. Restart or reload your server
3. Onlysleep will automatically register its placeholders (check console for: `"PlaceholderAPI expansion registered!"`)
4. Verify with: `/papi ecloud list` (Onlysleep should appear)
5. Use placeholders in any plugin that supports PlaceholderAPI (scoreboards, nametags, etc.)

---

## Placeholder Reference

### Player-Specific Placeholders

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%onlysleep_sleeping%` | Number of sleeping players in the player's world | `3` |
| `%onlysleep_required%` | Number of players required to skip night | `10` |
| `%onlysleep_percentage%` | Configured sleep percentage value | `50` |
| `%onlysleep_total%` | Total eligible player count in the world | `20` |
| `%onlysleep_progress%` | Percentage of required sleepers achieved (0-100) | `30` |
| `%onlysleep_progress_bar%` | Visual progress bar of sleep progress | `■■■■░░░░░░` |
| `%onlysleep_sleeping_names%` | Comma-separated list of sleeping player names | `Steve, Alex, Notch` |
| `%onlysleep_status%` | Player's sleep status | `Sleeping`, `AFK`, or `Awake` |
| `%onlysleep_is_sleeping%` | Whether the player is sleeping | `true` / `false` |
| `%onlysleep_skipping%` | Whether night skip is currently scheduled | `true` / `false` |
| `%onlysleep_enabled%` | Whether sleep is enabled in the player's world | `true` / `false` |
| `%onlysleep_is_sleepable%` | Whether it's night/storm currently | `true` / `false` |
| `%onlysleep_is_night%` | Whether it's currently night time | `true` / `false` |
| `%onlysleep_afk%` | Whether the player is AFK | `true` / `false` |

### Server-Specific Placeholders

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%onlysleep_version%` | Plugin version | `2.0.0` |
| `%onlysleep_platform%` | Server platform name | `Folia`, `Paper`, `Spigot`, `Bukkit` |

### World-Specific Placeholders

These placeholders let you query specific worlds by name:

| Placeholder | Description | Example |
|-------------|-------------|---------|
| `%onlysleep_world_sleeping_<world>%` | Sleeping count in a specific world | `%onlysleep_world_sleeping_world%` → `3` |
| `%onlysleep_world_required_<world>%` | Required count in a specific world | `%onlysleep_world_required_world_nether%` → `5` |
| `%onlysleep_world_total_<world>%` | Total eligible in a specific world | `%onlysleep_world_total_world_the_end%` → `8` |

> **Note:** World names with underscores may conflict with the placeholder syntax. For example, `world_the_end` works because the placeholder becomes `%onlysleep_world_total_world_the_end%`, which parses as `world_total_` prefix + `world_the_end` as the world name.

---

## Usage Examples

### In Scoreboards (using PlaceholderAPI)

With plugins like **Scoreboard-replacer**, **TAB**, or **GriefDefender**:

```yaml
lines:
  - "&7Sleeping: &b%onlysleep_sleeping%&7/&b%onlysleep_required%"
  - "&7Progress: &b%onlysleep_progress%%"
  - "&7Status: &b%onlysleep_status%"
```

### In Chat Format (using CMI or VentureChat)

```
{onlysleep_sleeping} players sleeping out of {onlysleep_required} required
```

### In Nametags (using TAB)

```yaml
nametags:
  suffix: " &7[%onlysleep_status%]"
```

### PlaceholderAPI Command Testing

Use the `/papi parse` command to test placeholders:

```
/papi parse %player_name% %onlysleep_sleeping%
/papi parse %player_name% %onlysleep_progress_bar%
/papi parse %player_name% %onlysleep_status%
```

---

## Placeholder Logic Details

### `%onlysleep_status%`

Returns one of three values based on the player's current state:

| Value | Condition |
|-------|-----------|
| `Sleeping` | Player is currently counted as sleeping (in bed and registered) |
| `AFK` | Player is AFK according to built-in tracker, EssentialsX, or CMI |
| `Awake` | Player is active and not sleeping |

### `%onlysleep_is_sleepable%`

Returns `true` if the player's world is currently in a state where sleeping would work:

| Condition | Time Range | Returns |
|-----------|------------|---------|
| Night time | 12542 ≤ time ≤ 23458 | `true` |
| Storm / Thunder | Any time during storm | `true` |
| Day + Clear weather | Everything else | `false` |

### `%onlysleep_progress%`

Calculated as: `(sleepingCount / requiredCount) × 100`, capped at 100.

| Sleeping | Required | Progress |
|----------|----------|----------|
| 0 | 10 | 0% |
| 5 | 10 | 50% |
| 10 | 10 | 100% |
| 12 | 10 | 100% |

### `%onlysleep_progress_bar%`

A visual bar using the configured progress bar symbol and length from `config.yml`. For example, with 5 out of 10 players sleeping and a length of 20:

```
■■■■■■■■■■░░░░░░░░░░
```

### `%onlysleep_sleeping_names%`

Returns comma-separated names of currently sleeping players:

```
Steve, Alex
```

Returns `None` if no players are sleeping.

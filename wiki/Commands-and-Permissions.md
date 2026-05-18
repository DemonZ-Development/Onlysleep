# 🎮 Commands & Permissions

## Commands

### `/onlysleep help`

Shows the help page with available commands.

- **Usage:** `/onlysleep help`
- **Permission:** `onlysleep.command` (default: everyone)
- **Aliases:** `/os help`, `/sleep help`

**Output:**
```
=== Onlysleep Commands ===
/onlysleep reload - Reload configuration
/onlysleep info   - Show plugin information
/onlysleep status - Show plugin status
/onlysleep help   - Show this help
========================
```

### `/onlysleep info`

Displays plugin version information and current settings.

- **Usage:** `/onlysleep info`
- **Permission:** `onlysleep.command` (default: everyone)
- **Aliases:** `/os info`, `/sleep info`

**Output:**
```
=== Onlysleep v2.0.0 ===
Version: 2.0.0
Author: Demonz Development
Status: Enabled | Paper
Sleep Required: 50%
Platform: Paper
============================
```

### `/onlysleep status`

Shows detailed plugin status including platform, version, and all major settings.

- **Usage:** `/onlysleep status`
- **Permission:** `onlysleep.status` (default: OP)
- **Aliases:** `/os status`, `/sleep status`

**Output:**
```
=== Onlysleep Status ===
Platform: Paper
Minecraft: 1.21.4
Version: 2.0.0
Sleep %: 50%
Per-World: Yes
Boss Bar: Enabled
Skip Type: instant
========================
```

### `/onlysleep reload`

Reloads the plugin configuration from disk without restarting the server.

- **Usage:** `/onlysleep reload`
- **Permission:** `onlysleep.reload` (default: OP)
- **Aliases:** `/os reload`, `/sleep reload`

This reloads:
- `config.yml` — All sleep, weather, UI, and sound settings
- `messages.yml` — All customizable messages
- AFK tracker — Re-initialized with updated timeout (if enabled)
- Offline player tracker — Re-initialized based on updated config (if enabled)

**Output on success:**
```
Configuration reloaded successfully!
```

**Output on failure:**
```
Failed to reload configuration. Check console for errors.
```

---

## Tab Completion

The `/onlysleep` command includes tab completion support. Typing `/onlysleep ` and pressing Tab will show available subcommands:

```
/onlysleep <reload|info|status|help>
```

---

## Permissions

### Permission Tree

```
onlysleep.* (OP)
├── onlysleep.command (Everyone)
├── onlysleep.reload (OP)
├── onlysleep.status (OP)
├── onlysleep.exempt (OP)
└── onlysleep.update (OP)
```

### Permission Reference

| Permission | Default | Description |
|------------|---------|-------------|
| `onlysleep.*` | OP | Grants all Onlysleep permissions (wildcard) |
| `onlysleep.command` | **Everyone** | Allows use of `/onlysleep` command and all subcommands except `status` |
| `onlysleep.reload` | OP | Allows using `/onlysleep reload` to reload the configuration |
| `onlysleep.status` | OP | Allows using `/onlysleep status` to view detailed plugin status |
| `onlysleep.exempt` | OP | Exempts the player from sleep calculations entirely |
| `onlysleep.update` | OP | Receives update notifications when a new version is available |

---

## Permission Details

### `onlysleep.exempt`

Players with this permission are completely excluded from sleep mechanics:

- They are **not counted** in the total eligible player count
- They **cannot** contribute to skipping the night by sleeping
- They are **not shown** boss bars during night skip
- They do **not** receive sleep-related messages

This is useful for:
- Staff members who should not affect sleep mechanics
- Players in creative mode on creative/survival hybrid servers
- Special players who should be ignored by the system

### `onlysleep.update`

Players with this permission receive a direct message when Onlysleep detects a new version is available on startup:

```
✨ A new version is available: 2.1.0 (Current: 2.0.0) - /modrinth download
```

---

## Permission Setup Examples

### Grant everyone access to status

```yaml
permissions:
  onlysleep.status:
    default: true
```

### Give a specific player exemption

```
/lp user Steve permission set onlysleep.exempt true
```

### Create a staff role with all permissions

```
/lp group staff permission set onlysleep.* true
```

### Allow non-ops to reload

```
/lp group admin permission set onlysleep.reload true
```

> **Note:** Permission examples above use [LuckPerms](https://luckperms.net/) syntax. Adjust for your permissions plugin.

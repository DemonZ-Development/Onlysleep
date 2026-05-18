# 💬 Messages

Onlysleep's messages are fully customizable in `plugins/Onlysleep/messages.yml`. You can change every message the plugin sends to players.

---

## Color Codes

Messages support Minecraft's standard color and formatting codes using the `&` symbol:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&1` | Dark Blue |
| `&2` | Dark Green | `&3` | Dark Aqua |
| `&4` | Dark Red | `&5` | Dark Purple |
| `&6` | Gold | `&7` | Gray |
| `&8` | Dark Gray | `&9` | Blue |
| `&a` | Green | `&b` | Aqua |
| `&c` | Red | `&d` | Light Purple |
| `&e` | Yellow | `&f` | White |

**Formatting codes:**

| Code | Effect |
|------|--------|
| `&l` | **Bold** |
| `&o` | *Italic* |
| `&n` | <u>Underline</u> |
| `&m` | ~~Strikethrough~~ |
| `&k` | Magic (random characters) |
| `&r` | Reset |

**Example:** `&bGood Morning!` displays as aqua text: <span style="color: #55FFFF">Good Morning!</span>

---

## Placeholders

Available placeholders for messages:

| Placeholder | Description |
|-------------|-------------|
| `%player%` | Player name / display name |
| `%count%` | Current number of sleeping players |
| `%required%` | Number of players required to skip |
| `%bar%` | Visual progress bar |
| `%version%` | Plugin version |
| `%author%` | Plugin author |
| `%percent%` | Configured sleep percentage |
| `%platform%` | Server platform (Folia, Paper, Spigot, Bukkit) |
| `%new%` | Latest version (update messages only) |
| `%current%` | Current version (update messages only) |
| `%cmd%` | Command name (help messages only) |

---

## Message Reference

### Default Messages

Here is the complete default `messages.yml` with explanations of each message:

```yaml
prefix: "&8[&bOnlysleep&8] &r"
```

The prefix is prepended to all messages automatically.

### Sleep Messages

```yaml
sleep:
  start-sleep: "&a%player% &eis sleeping... &7(&b%count%&7/&b%required%&7)"
```
Sent to all players in the world when someone enters a bed.

```yaml
  enough-sleeping: "&a✅ Good morning! Night skipped by &b%player%"
```
Broadcast to all players when the night is successfully skipped.

```yaml
  already-day: "&cYou can only sleep during the night or a thunderstorm."
```
Sent to a player who tries to sleep during the day with clear weather.

```yaml
  not-safe: "&cYou cannot sleep now - monsters are nearby."
```
Sent to a player who tries to sleep with monsters nearby.

```yaml
  not-required: "&eNo one needs to sleep right now."
```
Sent to a player who tries to sleep when the night skip is already scheduled.

```yaml
  cancelled: "&c%player% &ehas stopped sleeping."
```
Sent to all players when someone gets out of bed while a skip is scheduled, cancelling it.

```yaml
  progress-bar: "&bSleeping... &7%bar% &7(&b%count%&7/&b%required%&7)"
```
Shown in the action bar to sleeping players while the skip is pending.

```yaml
  world-disabled: "&cSleep skipping is disabled in this world."
```
Sent to players trying to sleep in a disabled world.

### Weather Messages

```yaml
weather:
  clearing: "&a☀ The storm has been cleared by &b%player%"
```
Broadcast when a storm is cleared by sleeping.

```yaml
  already-clear: "&eWeather is already clear."
```
Sent to a player trying to sleep during a storm when weather is already clear.

### Boss Bar

```yaml
boss-bar:
  title: "&b🌙 Night is being skipped by &f%player%"
```
The boss bar title shown to all players during a pending night skip.

### Command Messages

```yaml
command:
  no-permission: "&cYou don't have permission to use this command."
```
Sent when a player lacks permission for a command.

```yaml
  reload-success: "&aConfiguration reloaded successfully!"
```
Sent after `/onlysleep reload` succeeds.

```yaml
  reload-fail: "&cFailed to reload configuration. Check console for errors."
```
Sent after `/onlysleep reload` fails.

```yaml
  info:
    header: "&8=== &bOnlysleep v%version% &8==="
    version: "&7Version: &b%version%"
    author: "&7Author: &b%author%"
    status-enabled: "&7Status: &aEnabled &7| &b%platform%"
    sleep-pct: "&7Sleep Required: &b%percent%%"
    platform: "&7Platform: &b%platform%"
    footer: "&8============================"
```
Messages for `/onlysleep info`.

### Update Messages

```yaml
update:
  available: "&e✨ A new version is available: &b%new% &e(Current: &7%current%&e) &f- &b/modrinth download"
  current: "&aYou are running the latest version."
  check-fail: "&cFailed to check for updates."
```
Messages for the update checker.

### Help Messages

```yaml
help:
  header: "&8=== &bOnlysleep Commands &8==="
  reload: "&7/%cmd% reload &8- &bReload configuration"
  info: "&7/%cmd% info &8- &bShow plugin information"
  status: "&7/%cmd% status &8- &bShow plugin status"
  help: "&7/%cmd% help &8- &bShow this help"
  footer: "&8========================"
```
Messages for `/onlysleep help`. The `%cmd%` placeholder is replaced with the command alias used (e.g., `onlysleep`, `os`, `sleep`).

---

## Example Customizations

### Minimalist Style

```yaml
prefix: "&8[&bSleep&8] "
sleep:
  start-sleep: "&b%player% &7is sleeping... &8(&7%count%&8/&7%required%&8)"
  enough-sleeping: "&a☀ Good morning!"
```

### Colorful Theme

```yaml
prefix: "&dᴏɴʟʏsʟᴇᴇᴘ &7» "
sleep:
  start-sleep: "&d%player% &5is sleeping... &7%bar%"
  enough-sleeping: "&d✦ &5Good Morning! &d✦ &7Skipped by &5%player%"
  progress-bar: "&5Sleeping... &d%bar%"
```

### Double Prefix (Custom Prefix + Default Prefix Separator)

Set an empty prefix and include your own in each message:

```yaml
prefix: ""
sleep:
  start-sleep: "&8[&bOnlysleep&8] &7→ &a%player% &eis sleeping..."
```

> **Tip:** Changes to `messages.yml` take effect after running `/onlysleep reload`.

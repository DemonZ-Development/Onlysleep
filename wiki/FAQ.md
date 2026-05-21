# ❓ FAQ & Troubleshooting

## Frequently Asked Questions

### How does the sleep percentage work?

The plugin calculates the number of players needed to skip based on the `sleep-percentage` setting and the total eligible players in the world.

Formula: `required = max(1, ceil(total × percentage / 100))`

Example with 10 eligible players:

| Percentage | Required |
|------------|----------|
| 0% (one-player sleep) | 1 |
| 50% (default) | 5 |
| 75% | 8 |
| 100% | 10 |

Players with `onlysleep.exempt` permission are excluded from the total. See [Player Filtering](Configuration#player-filtering) for who counts.

### How do I make it one-player sleep?

Set `sleep-percentage: 0` in `config.yml`. Any single eligible player can skip the night.

### How do I make ALL players sleep?

Set `sleep-percentage: 100` in `config.yml` — all eligible players in the world must sleep.

> **Note:** If `per-world-sleep: true`, only players in the same world count. Exempted players don't count either.

### Why can't my players sleep during the day?

By default, players can only sleep during the night (Minecraft time 12542-23458) or during a storm. The message `"You can only sleep during the night or a thunderstorm."` will appear. This is vanilla Minecraft behavior.

### Why is the night skip cancelling when someone gets out of bed?

This is intentional. If a player gets out of bed while the night skip is still pending (during the `skip-delay-ticks` delay), the skip is cancelled. This prevents griefing where one player sleeps and another immediately gets up.

> **Tip:** Set `skip-delay-ticks: 0` for an instant skip that can't be interrupted.

### Does Onlysleep work with Folia?

**Yes.** Onlysleep has full Folia support. It uses the Folia region scheduler for world-specific tasks and the global region scheduler for broadcast tasks. On non-Folia servers, it falls back to the standard Bukkit scheduler.

### Does Onlysleep work with Purpur, Pufferfish, Airplane, etc.?

**Yes.** Onlysleep works on any server software derived from Bukkit/Spigot/Paper. It automatically detects the platform and adjusts its behavior accordingly.

### How do I disable Onlysleep in certain worlds?

Edit `config.yml` and add worlds to the `disabled-worlds` list:

```yaml
disabled-worlds:
  - "world_nether"
  - "world_the_end"
```

### What's the difference between `count-afk-as-sleeping` and `exclude-afk-from-total`?

- **`count-afk-as-sleeping: true`** — AFK players are counted as if they're sleeping (helps reach the threshold)
- **`exclude-afk-from-total: true`** — AFK players are removed from the total count (lowers the threshold)

The most common configuration is `count-afk-as-sleeping: false` + `exclude-afk-from-total: true`, which means AFK players are completely ignored — they don't count toward the total required and they don't count as sleeping. This prevents AFK players from blocking night skip.

### How do I make it so night skipping requires ALL players online?

Enable `require-all-players-online: true` in `config.yml`. When enabled, if any eligible player is offline, the required count is set to a very high number (effectively preventing skip).

> **Note:** Onlysleep uses an intelligent offline player tracker that caches the offline player count asynchronously to avoid lag on bed enter.

### How do I add a cooldown to prevent spamming night skip?

Onlysleep doesn't have a built-in cooldown, but you can achieve a similar effect by:

1. Setting `skip-delay-ticks` to a higher value (e.g., `100` for 5 seconds)
2. The skip prevents re-skipping while pending (players see "No one needs to sleep right now")

### Can I use Onlysleep with other sleep plugins?

It's **not recommended** to use Onlysleep alongside other sleep plugins. They will conflict over who controls the night skip mechanic. Onlysleep is a complete sleep management solution and doesn't need other sleep plugins.

---

## Troubleshooting

### Plugin doesn't enable

**Symptoms:** Plugin jar loads but fails with a red error in console.

**Causes & Solutions:**
1. **Outdated Minecraft version** — Onlysleep requires Minecraft 1.16.5+
2. **Incompatible Java version** — Onlysleep requires Java 21+
3. **Corrupted jar file** — Re-download the jar file
4. **Missing dependencies** — Onlysleep has no hard dependencies, but ensure you're running a supported server software

Check the full error in console for specific details.

### Commands don't work

**Symptoms:** `/onlysleep` returns "Unknown command" or no response.

**Causes & Solutions:**
1. **Plugin didn't load** — Check console for errors
2. **Permission denied** — Ensure you have `onlysleep.command` permission (default: everyone)
3. **Command conflict** — Another plugin might be registering the same `/sleep` alias
4. **Server didn't restart** — Try a full restart instead of `/reload`

### Placeholders aren't working

**Symptoms:** `%onlysleep_sleeping%` shows as plain text instead of a number.

**Causes & Solutions:**
1. **PlaceholderAPI not installed** — Onlysleep's placeholders require [PlaceholderAPI](https://placeholderapi.com/)
2. **PlaceholderAPI loaded after Onlysleep** — Try restarting the server
3. **Expansion not registered** — Check console for `"PlaceholderAPI expansion registered!"`
4. **Wrong placeholder syntax** — Ensure you're using `%onlysleep_sleeping%` (not `{onlysleep_sleeping}` — though some plugins support both formats)

### Night isn't being skipped

**Symptoms:** Players sleep but nothing happens.

**Causes & Solutions:**
1. **Not enough players sleeping** — Check the required count with `/onlysleep info`
2. **World is disabled** — Check `disabled-worlds` in config.yml
3. **All players need to sleep** — Set `sleep-percentage: 0` for one-player sleep
4. **Require all players online** — If enabled, offline players block the skip
5. **Boss bar isn't showing but skip works** — Check `ui.boss-bar.enabled: true`
6. **Permission issue** — Players might have `onlysleep.exempt` set

Run `/onlysleep status` to see current settings.

### Config changes aren't applying

**Symptoms:** You edited `config.yml` but nothing changed.

1. **You need to reload** — Run `/onlysleep reload`
2. **YAML syntax error** — Check for missing spaces or tabs. YAML is strict about indentation
3. **Wrong file location** — Ensure you're editing `plugins/Onlysleep/config.yml`, not the one in the jar
4. **Plugin didn't load** — Check console for errors on startup

### Server lags when players sleep

**Symptoms:** Lag spike when someone enters a bed.

**Causes & Solutions:**
1. **Large server with many offline players** — If `require-all-players-online: true` is enabled, the offline player tracker loads the player count asynchronously. This should not cause lag
2. **Other plugins interfering** — Try disabling other plugins to identify conflicts
3. **Outdated server software** — Update to the latest version of your server software

### I found a bug

If you encounter a bug:

1. **Check** that you're running the latest version of Onlysleep
2. **Check** the console for any error messages
3. **Create an issue** on [GitHub Issues](https://github.com/DemonZ-Development/Onlysleep/issues) with:
   - Plugin version and server software version
   - Steps to reproduce the bug
   - Any relevant console errors
   - Your `config.yml` (if relevant)
4. **Join** the [Discord](https://discord.gg/demonzdevelopment) for faster support

---

## Still Having Issues?

- Check the [Configuration Reference](Configuration) for all available settings
- Review the [Commands & Permissions](Commands-and-Permissions) to ensure proper setup
- Join the [Discord](https://discord.gg/demonzdevelopment) for community support
- Open a [GitHub Issue](https://github.com/DemonzDevelopment/Onlysleep/issues)

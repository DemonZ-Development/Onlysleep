# 📥 Installation

## Requirements

- **Java 21+** (compiled for the latest Paper API)
- **Minecraft 1.16.5+** (Bukkit, Spigot, Paper, Folia, or any compatible server)
- **No other plugins required!** Onlysleep works standalone
- **[Optional] PlaceholderAPI** for placeholder expansion support

---

## Step-by-Step Installation

### 1. Download the Plugin

Get the latest `Onlysleep-*.jar` from one of these sources:

| Platform | Link |
|----------|------|
| **Modrinth** | [https://modrinth.com/plugin/onlysleep](https://modrinth.com/plugin/onlysleep) |
| **Hangar** | [https://hangar.papermc.io/DemonzDevelopment/Onlysleep](https://hangar.papermc.io/DemonzDevelopment/Onlysleep) |
| **Spigot** | [https://www.spigotmc.org/resources/onlysleep.12345/](https://www.spigotmc.org/resources/onlysleep.12345/) |
| **GitHub Releases** | [https://github.com/DemonZ-Development/Onlysleep/releases](https://github.com/DemonZ-Development/Onlysleep/releases) |

### 2. Install the Plugin

1. Stop your server (or use `/reload` — though a full restart is recommended)
2. Place the downloaded `Onlysleep-*.jar` file into your server's `plugins/` folder
3. Start your server

### 3. Verify Installation

Check the server console for a message like:

```
[Onlysleep] Detected platform: Paper
[Onlysleep] bStats metrics initialized (ID: 31415)
[Onlysleep] Onlysleep v2.0.0 by Demonz Development enabled!
[Onlysleep] Running on Paper 1.21.4
```

### 4. Configure

Edit `plugins/Onlysleep/config.yml` to customize the plugin to your needs.

After making changes, run `/onlysleep reload` to apply them without restarting the server.

---

## Optional: PlaceholderAPI

If you want to use Onlysleep's placeholders in other plugins (like scoreboards, nametags, etc.):

1. Download and install [PlaceholderAPI](https://placeholderapi.com/)
2. Restart your server
3. Onlysleep will automatically detect PlaceholderAPI and register its placeholders
4. Use any of the [available placeholders](Placeholders) in your other plugins

---

## Optional: Building from Source

See the [Building from Source](Building) guide for instructions on compiling the plugin yourself.

---

## Updating

To update Onlysleep:

1. Download the latest version
2. Stop your server
3. Replace the old `Onlysleep-*.jar` with the new one
4. Start your server

> **Note:** Configuration files are preserved across updates as long as you don't delete the `plugins/Onlysleep/` folder.

---

## Troubleshooting

**Problem:** Plugin doesn't load / "Plugin Onlysleep vX has failed to load"
- Ensure you're running Minecraft 1.16.5 or newer
- Check that Java 8+ is installed (run `java -version` on your server)
- Check the console for specific error messages

**Problem:** `/onlysleep` command not found
- Ensure the plugin loaded successfully (check console)
- Try restarting your server

**Problem:** Placeholders not working
- Ensure PlaceholderAPI is installed
- Ensure PlaceholderAPI loaded before Onlysleep (check plugin load order)
- Check that you're using the correct placeholder syntax (`%onlysleep_sleeping%`)

See the [FAQ](FAQ) page for more common issues.

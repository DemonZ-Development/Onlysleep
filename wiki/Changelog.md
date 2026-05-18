# 📋 Changelog

---

## [1.0.0] - 2025-06-01

### 🚀 Major Changes
- **Complete rewrite** with modular package structure
- **Folia support** — Full compatibility with Folia's regionized scheduler
- **bStats integration** — Anonymous usage statistics (ID: 31415)
- **Platform detection** — Auto-detects Bukkit, Spigot, Paper, Folia

### ✨ New Features
- **One-Player Sleep** — Default mode, or configure any percentage
- **Custom skip types** — Support for `instant`, `speed`, and `gradual` night skips
- **Per-World Sleep** — Per-world or global sleep counting
- **Weather Skip** — Automatically clear storms and thunderstorms
- **Title/subtitle support** — Configurable titles when night is skipped
- **Sound system** — Fully configurable sounds for skip events
- **Visual Feedback** — Boss bar, action bar, progress bar, and title support
- **AFK detection** — EssentialsX and CMI integration
- **Disabled worlds** — Per-world sleep disabling
- **Advanced player filtering** — Creative mode, flying, spectator, and exempt controls
- **Better command system** — `/onlysleep status`, `/onlysleep update`, tab completion
- **Update checker** — Automatic update checks via Modrinth API with `/onlysleep update`
- **PlaceholderAPI** — 19+ placeholders for integrations
- **Progress bar customization** — Configurable symbols and length
- **Smart Player Filtering** — Ignores AFK, spectators, exempt players, and more

### 🛠️ Technical Improvements
- **SchedulerAdapter** — Folia-compatible task scheduling with fallback
- **Multi-version compatible** — Works on Minecraft 1.16.5+
- **Dedicated config package** — ConfigManager in `config` package
- **Command package** — Clean command separation
- **Listener package** — Event handling in dedicated package
- **Manager package** — Core logic separation
- **Utility classes** — PlatformAdapter, SchedulerAdapter, UpdateChecker, AfkTracker
- **bStats shading** — Proper bStats relocation to avoid conflicts
- **Resource filtering** — Dynamic version in plugin.yml

### 📝 Documentation
- Comprehensive README.md with banner image
- Modrinth description (MODRINTH.md)
- Spigot BBCode listing (SPIGOT.md)
- Hangar description (HANGAR.md)
- Full GitHub Wiki with 12 pages
- CHANGELOG.md

---

## Initial Release

- Basic one-player sleep functionality
- Configurable sleep percentage
- Per-world support
- Boss bar display
- Basic command system

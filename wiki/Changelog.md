# 📋 Changelog

---

## [1.3.0] - 2026-07-11

This beta upgrades the Paper API target, fixes gradual/speed skipping, expands sleep feedback, and makes Onlysleep fully own vanilla sleep calculations while enabled.

### Added

- **Automatic gamerule management**: enabled worlds temporarily use `playersSleepingPercentage: 101`, with their original values restored when management stops, the world unloads, or the plugin disables.
- **World sleep sounds**: configurable sounds now play on bed entry, weather clearing, and completed night skips.

### Changed

- The Paper API target is upgraded from 1.21.4 to 1.21.11.
- Rain and thunder clearing can be configured independently.
- Disabled gamemodes are excluded from both sleeping and eligible-player counts.

### Bug Fixes

- Reloading with gamerule management disabled, or with a newly disabled world, now restores the original gamerule immediately.
- Worlds loaded after startup receive the configured override; unloading worlds are restored and removed from tracked state.
- **Boss bar no longer shows "Unknown" when the initiating sleeper logs off mid-skip**
  - The boss bar now reads from a snapshot of the initiating player's name (populated at skip-start) instead of a live `Bukkit.getPlayer(uuid)` lookup that returned `null` after logout.
- **Sleeping players no longer wake up mid-skip in gradual mode**
  - World time now stays parked at the original night value for the entire gradual animation; `setTime()` is called exactly once at the final tick to snap to morning. This prevents vanilla's wake-up threshold (`world.getTime() > 23458`) from being crossed mid-animation.

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

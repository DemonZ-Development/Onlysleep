# 📋 Changelog

> For the full changelog, visit the [Onlysleep Wiki](https://github.com/DemonZ-Development/Onlysleep/wiki/Changelog).

---

## [1.1.0] - 2026-05-21

This release brings massive stability, compatibility, and user-experience improvements to Onlysleep.

### 🚀 Key Improvements & Bug Fixes

- **100% Folia/Paper Multi-threaded Stability**:
  - Rewrote internal `SchedulerAdapter` to resolve reflection lookup issues and method signatures for Folia/Paper multi-threaded environments, preventing region-scheduler crashes.
  - Redesigned task self-cancellation on Folia to interact directly and safely with the region's returned `ScheduledTask` object via a thread-safe `AtomicReference` wrapper.
- **Fixed Operator Sleep Exclusion**:
  - Removed `onlysleep.exempt` from the default OP wildcard permission list in `plugin.yml`. Administrators and OPs now contribute to sleeping by default instead of being permanently exempt, resolving issues where OPs were unable to trigger night skip.
- **Enhanced Gradual & Speed Transition Modes**:
  - Resolved a critical bug in `gradual` mode where the skip task would continue running and fast-forwarding time all day.
  - Implemented the `"speed"` mode as a rapid timelapse fast-forward (150 ticks per tick) utilizing the self-canceling gradual skip engine.
- **PlaceholderAPI Null-Player Safety**:
  - Re-engineered `SleepPlaceholderExpansion` to process all player-independent placeholders first (e.g. `version`, `platform`, `percentage`, and all `world_` statistics), making it completely null-player safe when resolved from consoles, plugins, or non-player contexts.
- **Robust Player Trackers & Initialization**:
  - Fixed unique player count estimation in `OfflinePlayerTracker` by checking for first-time players (`!player.hasPlayedBefore()`), preventing `require-all-players-online` from bypassing offline check.
  - Integrated direct `PlayerJoinEvent` and `PlayerQuitEvent` hooks in the built-in `AfkTracker` to instantly initialize player activity on join and clean up on quit, solving a bug where silent/idle players could never become AFK.
- **Strict Bed-Enter Validation**:
  - Restructured `SleepListener` to only proceed with sleep registrations when `BedEnterResult` is explicitly `OK` (or custom-handled `NOT_SAFE`), avoiding false-positive skips caused by other failed bed enter attempts.
- **Asynchronous Recurring Update Checker**:
  - Automatically schedules asynchronous update checks every 4 hours (non-blocking) when automatic checking is enabled, rather than only checking once on startup.
- **Smart Configuration Updater**:
  - Implemented an automatic, stateful configuration merger that updates `config.yml` and `messages.yml` with new options and comments from the jar resources, fully preserving custom user values, comments, and formatting without overwriting user customization.
- **Safe Update Checks & Null-Safe Commands**:
  - Fixed unescaped brackets and double quotes in the Update Checker query parameters to comply with modern strict Java runtimes, preventing `URISyntaxException`.
  - Guaranteed null-safe execution of `/onlysleep update` by always instantiating `UpdateChecker` on plugin load, regardless of whether auto-checking is enabled in the config.

### 📝 Listings & Documentation (Java 21+)
- Fully updated server listings (`README.md`, `SPIGOT.md`, `HANGAR.md`, and `MODRINTH.md`) and the entire GitHub Wiki (`Home.md`, `FAQ.md`, `Installation.md`, `Commands-and-Permissions.md`) to reflect that Onlysleep requires **Java 21+** and clarify standard sleep permissions.

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

# Changelog

## [2.0.0] - 2025-06-01

### 🚀 Major Changes
- **Complete rewrite** with modular package structure
- **Folia support** — Full compatibility with Folia's regionized scheduler
- **bStats integration** — Anonymous usage statistics (ID: 31415)
- **Platform detection** — Auto-detects Bukkit, Spigot, Paper, Folia

### ✨ New Features
- **Custom skip types** — Support for `instant`, `speed`, and `gradual` night skips
- **Title/subtitle support** — Configurable titles when night is skipped
- **Sound system** — Fully configurable sounds for skip events
- **AFK detection** — EssentialsX and CMI integration
- **Disabled worlds** — Per-world sleep disabling
- **Advanced player filtering** — Creative mode, flying, and spectator controls
- **Better command system** — `/onlysleep status`, tab completion improvements
- **Update checker** — Automatic update checks via Modrinth API
- **Progress bar customization** — Configurable symbols and length

### 🛠️ Technical Improvements
- **SchedulerAdapter** — Folia-compatible task scheduling with fallback
- **Multi-version compatible** — Works on Minecraft 1.16.5+
- **Dedicated config package** — ConfigManager in `config` package
- **Command package** — Clean command separation
- **Listener package** — Event handling in dedicated package
- **Manager package** — Core logic separation
- **Utility classes** — PlatformAdapter, SchedulerAdapter, UpdateChecker
- **Maven Shade Plugin** — Proper bStats shading with relocation
- **Resource filtering** — Dynamic version in plugin.yml

### 📝 Documentation
- Comprehensive README.md
- Modrinth description (MODRINTH.md)
- Spigot description (SPIGOT.md)
- Hangar description (HANGAR.md)
- CHANGELOG.md

## [1.0.0] - Initial Release
- Basic one-player sleep functionality
- Configurable sleep percentage
- Per-world support
- Boss bar display
- Basic command system

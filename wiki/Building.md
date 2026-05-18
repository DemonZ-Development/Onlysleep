# 🏗️ Building from Source

## Prerequisites

- **Java 21+** (required for compilation)
- **Git** (for cloning the repository)

---

## Building with Gradle (Recommended)

### 1. Clone the Repository

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
```

### 2. Build the Plugin

```bash
./gradlew clean build
```

On Windows, use `gradlew.bat clean build`.

### 3. Find the Output

The compiled JAR will be in `build/libs/`:

```
build/libs/Onlysleep-1.0.0.jar
```

### 4. Install the Built JAR

Copy the JAR to your server's `plugins/` folder and restart.

---

## Building with Maven (Alternative)

### 1. Clone the Repository

```bash
git clone https://github.com/DemonZ-Development/Onlysleep.git
cd Onlysleep
```

### 2. Build the Plugin

```bash
mvn clean package
```

### 3. Find the Output

The compiled JAR will be in `target/`:

```
target/Onlysleep-1.0.0.jar
```

---

## Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.demonzdevelopment.onlysleep.config.ConfigManagerTest"

# Run with verbose output
./gradlew test --info

# Run tests without building first
./gradlew test --no-daemon
```

The project has **152+ unit tests** covering:

- ConfigManager (configuration loading and progress bar logic)
- SleepManager (sleep counting, player filtering, permissions)
- AfkTracker (AFK detection and timeout)
- PlatformAdapter (server platform detection)
- UpdateChecker (version comparison)
- SleepPlaceholderExpansion (all 19+ placeholders)
- OnlysleepIntegrationTest (end-to-end plugin lifecycle)

---

## Build Options

### Skipping Tests

```bash
./gradlew build -x test
```

### Building with Debug Information

```bash
./gradlew clean build --info
```

### Generating a Comprehensive Test Report

```bash
./gradlew clean test
```

HTML test reports are available at `build/reports/tests/test/index.html`.

---

## Project Structure

```
Onlysleep/
├── build.gradle.kts              # Gradle build configuration
├── settings.gradle.kts           # Gradle settings
├── pom.xml                       # Maven build configuration
├── gradlew / gradlew.bat         # Gradle wrapper scripts
├── CHANGELOG.md                  # Version history
├── README.md                     # Main README
├── MODRINTH.md                   # Modrinth description
├── SPIGOT.md                     # Spigot description
├── HANGAR.md                     # Hangar description
├── wiki/                         # Wiki documentation
├── src/
│   ├── main/
│   │   ├── java/com/demonzdevelopment/onlysleep/
│   │   │   ├── Onlysleep.java                    # Main plugin class
│   │   │   ├── config/ConfigManager.java          # Configuration
│   │   │   ├── command/OnlysleepCommand.java      # Commands
│   │   │   ├── listener/SleepListener.java        # Event listeners
│   │   │   ├── manager/SleepManager.java          # Core logic
│   │   │   └── util/
│   │   │       ├── AfkTracker.java                # AFK detection
│   │   │       ├── OfflinePlayerTracker.java      # Offline caching
│   │   │       ├── PlatformAdapter.java           # Platform detection
│   │   │       ├── SchedulerAdapter.java          # Folia compatibility
│   │   │       ├── SleepPlaceholderExpansion.java # PAPI expansion
│   │   │       └── UpdateChecker.java             # Version checking
│   │   └── resources/
│   │       ├── config.yml                         # Default config
│   │       ├── messages.yml                       # Default messages
│   │       └── plugin.yml                         # Plugin metadata
│   └── test/
│       └── java/com/demonzdevelopment/onlysleep/
│           ├── OnlysleepIntegrationTest.java
│           ├── config/ConfigManagerTest.java
│           ├── manager/SleepManagerTest.java
│           └── util/
│               ├── AfkTrackerTest.java
│               ├── PlatformAdapterTest.java
│               ├── SleepPlaceholderExpansionTest.java
│               └── UpdateCheckerTest.java
└── .github/workflows/
    ├── build.yml                  # CI build workflow
    ├── codeql.yml                 # Security analysis
    └── release.yml               # Release automation
```

---

## CI/CD

The project uses GitHub Actions for continuous integration:

- **build.yml** — Runs on every push and pull request. Builds the plugin with both Gradle and Maven, runs all tests, and caches dependencies.
- **codeql.yml** — Runs CodeQL security analysis on every push to main and weekly.
- **release.yml** — Triggered by tags matching `v*`. Builds the plugin, runs tests, creates a GitHub Release with the JAR artifact.

---

## Dependencies

### Build Dependencies

| Dependency | Purpose |
|------------|---------|
| Paper API (1.21.4) | Bukkit/Paper server API |
| PlaceholderAPI | Optional placeholder expansion |
| bStats | Anonymous usage metrics |
| Adventure API | Modern component-based chat |

### Test Dependencies

| Dependency | Purpose |
|------------|---------|
| JUnit 5 (Jupiter) | Test framework |
| Mockito 5 | Mocking Bukkit APIs |
| Mockito JUnit Jupiter | Mockito integration with JUnit 5 |

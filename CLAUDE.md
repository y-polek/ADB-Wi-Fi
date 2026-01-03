# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ADB Wi-Fi is an IntelliJ IDEA/Android Studio plugin that simplifies connecting to Android devices over Wi-Fi using ADB's "Connect over Wi-Fi" feature. Users can connect devices via USB, click Connect in the tool window, and then unplug the USB cable.

## Build Commands

```bash
# Run IDE with plugin installed (IntelliJ IDEA)
./gradlew runIde

# Run Android Studio with plugin installed
./gradlew runAndroidStudio

# Run all checks (linting + tests)
./gradlew check

# Run tests only
./gradlew test

# Run detekt linting only
./gradlew detekt

# Build distributable plugin zip
./gradlew buildPlugin
# Output: ./build/distributions/

# Run a single test class
./gradlew test --tests "dev.polek.adbwifi.adb.AdbTest"

# Run a single test method
./gradlew test --tests "dev.polek.adbwifi.adb.AdbTest.test multiple devices()"
```

## Architecture

### Core Components

- **Adb** (`adb/Adb.kt`): Low-level ADB command executor. Parses device lists, connects/disconnects devices, and queries device properties via shell commands. Uses `CommandExecutor` for process execution.

- **AdbService** (`services/AdbService.kt`): Application-level service that wraps Adb. Manages device polling (3-second interval), handles connect/disconnect operations, and coordinates with LogService and PinDeviceService.

- **ToolWindowPresenter** (`ui/presenter/ToolWindowPresenter.kt`): MVP presenter for the tool window. Subscribes to device list updates, handles user actions (connect, disconnect, share screen), and manages UI state.

### Key Services (IntelliJ @Service components)

- `PropertiesService`: Plugin settings (ADB location, port, scrcpy settings)
- `LogService`: Command history and log visibility state
- `PinDeviceService`: Persists previously connected devices for quick reconnection
- `DeviceNamesService`: Custom device name storage
- `ScrcpyService`: Optional screen sharing via scrcpy

### Data Flow

1. `AdbService` polls `Adb.devices()` every 3 seconds when tool window is open
2. `Adb` executes `adb devices` and queries each device for properties
3. Device list is emitted to `ToolWindowPresenter` via callback
4. Presenter converts `Device` models to `DeviceViewModel` and updates view

### Model Classes

- `Device`: Core device model with id, serialNumber, address, connectionType (USB/WIFI)
- `Address`: Network interface name + IP address
- `PinnedDevice`: Serialized device for persistence
- `DeviceViewModel`: UI-specific wrapper with display state

## Testing

Tests use mock command executors to simulate ADB output. See `MockCommandExecutor` for the pattern - it intercepts command strings and returns predefined output.

## Configuration

- `gradle.properties`: Plugin version, build compatibility settings
- `detekt/config.yml`: Custom detekt rules (relaxed magic numbers, wildcard imports)
- `detekt/baseline.xml`: Baseline for existing detekt issues

## Android Studio Version

To change which Android Studio version `runAndroidStudio` launches, modify the `version` property in `build.gradle.kts` under `intellijPlatformTesting.runIde`.
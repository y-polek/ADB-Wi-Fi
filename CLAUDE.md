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
- `AdbCommandsService`: Manages custom ADB shell commands (kill app, clear data, etc.). Supports import/export via JSON.

### Data Flow

1. `AdbService.devices` is a `StateFlow<List<Device>>` that polls `Adb.devices()` every 3 seconds while subscribed
2. `Adb` executes `adb devices` and queries each device for properties
3. `ToolWindowPresenter` collects from the StateFlow and updates the view
4. Presenter converts `Device` models to `DeviceViewModel` and updates view

### Reactive Patterns

The codebase uses Kotlin Coroutines `StateFlow` for reactive state management:

- `AdbService.devices: StateFlow<List<Device>>` - Device list with automatic polling (WhileSubscribed)
- `LogService.isLogVisible: StateFlow<Boolean>` - Log panel visibility state
- `CommandHistory.entries: StateFlow<List<LogEntry>>` - Command log entries
- `PropertiesService.isAdbLocationValid: StateFlow<Boolean>` - ADB location validation state
- `PropertiesService.isScrcpyEnabled: StateFlow<Boolean>` - Scrcpy feature toggle

Presenters subscribe to these flows using `launch { flow.collect { ... } }` and cancel the jobs when detaching.

### Model Classes

- `Device`: Core device model with id, serialNumber, address, connectionType (USB/WIFI)
- `Address`: Network interface name + IP address
- `PinnedDevice`: Serialized device for persistence
- `DeviceViewModel`: UI-specific wrapper with display state
- `AdbCommandConfig`: Custom ADB command with name, shell command, icon, and confirmation flag
- `ParameterPlaceholder`: Represents a `{param}` placeholder with optional name
- `AdbCommandsExportFile`: JSON format for import/export of commands

### UI Patterns

- **Popup menus**: Use `JBPopupFactory.createListPopup()` with `BaseListPopupStep` for context menus
- **Dialogs**: Extend `DialogWrapper` for modal dialogs
- **Settings panels**: Extend `Configurable` and use `ToolbarDecorator` for list editing
- **Clickable links**: Use `HyperlinkLabel` for inline clickable text

## Testing

Tests use mock command executors to simulate ADB output. See `MockCommandExecutor` for the pattern - it intercepts command strings and returns predefined output.

## UI Testing

UI tests use [IntelliJ Remote Robot](https://github.com/JetBrains/intellij-ui-test-robot) library.

### Running UI Tests

```bash
# Start IDE with Robot Server (in one terminal)
./gradlew runIdeForUiTests

# Run UI tests (in another terminal, after IDE is fully loaded)
./gradlew test --tests "*UiTest*"
```

Access http://127.0.0.1:8082/ to view component hierarchy and XPath locators.

### Fixture Pattern

UI tests use fixture classes in `src/test/kotlin/dev/polek/adbwifi/uitest/fixtures/`:

- `IdeFrameFixture`: Main IDE window
- `AdbWifiToolWindowFixture`: Plugin tool window
- `SettingsDialogFixture`: Settings dialog
- `AdbCommandsSettingsFixture`: ADB Commands settings panel
- `AdbCommandEditorDialogFixture`: Add/Edit command dialog
- `ConfirmationDialogFixture`: Yes/No confirmation dialogs

Each fixture has a companion object with a factory function:
```kotlin
adbCommandsSettingsFixture(robot) {
    clickCommand("Kill app")
    clickRemove()
}
```

### Important Patterns

**Finding elements**: Use `byXpath()` with `@tooltiptext`, `@text`, `@class`, or `@accessiblename` attributes:
```kotlin
find<JButtonFixture>(byXpath("//div[@tooltiptext='Add']")).click()
```

**Waiting for fixtures**: Use `waitForFixture<T>()` helper in `FixtureUtils.kt`.

**Assertions**: Use Google Truth (`assertThat`) for assertions in fixtures.

### Known Issues and Workarounds

**JList text truncation**: `JListFixture.collectItems()` returns truncated text. Use `findAllText()` or access the model via JS:
```kotlin
val names: List<String> = listFixture.callJs("""
    var model = component.getModel();
    var result = new java.util.ArrayList();
    for (var i = 0; i < model.getSize(); i++) {
        result.add(model.getElementAt(i).getText());
    }
    result;
""")
```

**CheckBoxList states**: No built-in API to get checkbox states. Use JS to access the model:
```kotlin
val selected: Boolean = listFixture.callJs("""
    component.getModel().getElementAt(0).isSelected()
""")
```

**JS variable scoping**: Remote Robot uses Mozilla Rhino. Use `var` instead of `const`/`let`, and avoid storing values in intermediate variables inside loops.

**Text fields with special characters**: `JTextFieldFixture.text` setter uses JS internally. Avoid single quotes in values or they will break the JS string.

**Double-click on list items**: `JListFixture` has no double-click method. Use click + Edit button instead:
```kotlin
fun editCommand(name: String) {
    clickCommand(name)
    clickEdit()
}
```

**Clicking on CheckBoxList checkbox**: Click near the left edge of the item (x + 10) to toggle the checkbox.

## Configuration

- `gradle.properties`: Plugin version, build compatibility settings
- `detekt/config.yml`: Custom detekt rules (relaxed magic numbers, wildcard imports)
- `detekt/baseline.xml`: Baseline for existing detekt issues

## Android Studio Version

To change which Android Studio version `runAndroidStudio` launches, modify the `version` property in `build.gradle.kts` under `intellijPlatformTesting.runIde`.
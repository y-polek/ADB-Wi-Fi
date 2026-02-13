package dev.polek.adbwifi.uitest

import dev.polek.adbwifi.uitest.fixtures.AdbCommandEditorDialogFixture.Companion.addAdbCommandDialog
import dev.polek.adbwifi.uitest.fixtures.AdbCommandEditorDialogFixture.Companion.editAdbCommandDialog
import dev.polek.adbwifi.uitest.fixtures.AdbCommandsSettingsFixture.AdbCommand
import dev.polek.adbwifi.uitest.fixtures.AdbCommandsSettingsFixture.Companion.adbCommandsSettingsFixture
import dev.polek.adbwifi.uitest.fixtures.AdbWifiToolWindowFixture.Companion.adbWiFiToolWindow
import dev.polek.adbwifi.uitest.fixtures.ConfirmationDialogFixture.Companion.deleteAdbCommandConfirmationDialog
import dev.polek.adbwifi.uitest.fixtures.ConfirmationDialogFixture.Companion.resetAdbCommandsConfirmationDialog
import dev.polek.adbwifi.uitest.fixtures.IdeFrameFixture.Companion.ideFrame
import dev.polek.adbwifi.uitest.fixtures.SettingsDialogFixture.Companion.settingsDialog
import org.junit.Test

class AdbCommandsSettingsUiTest : BaseUiTest() {

    companion object {
        private const val TEST_COMMAND_NAME = "Test Command"
        private const val TEST_COMMAND = "shell echo test"
        private const val RENAMED_RESTART_APP_COMMAND = "Restart"
    }

    @Test
    fun `changes are persisted on apply`() {
        // Step 1: Open "ADB Wi-Fi" tool window
        ideFrame(robot) {
            openAdbWifiToolWindow()
        }

        // Step 2: Open "ADB Commands" settings panel
        adbWiFiToolWindow(robot) {
            clickOpenSettings()
        }

        settingsDialog(robot) {
            search("ADB Commands")
        }

        adbCommandsSettingsFixture(robot) {
            // Verify initial list of all default commands
            assertCommands(
                AdbCommand("Kill app"),
                AdbCommand("Start app"),
                AdbCommand("Restart app"),
                AdbCommand("Clear app data"),
                AdbCommand("Clear app data and restart"),
                AdbCommand("Uninstall app"),
                AdbCommand("Uninstall app (keep data)"),
                AdbCommand("Install APK"),
                AdbCommand("Reboot device"),
                AdbCommand("Simulate incoming SMS (emulator only)"),
                AdbCommand("Enable TalkBack (emulator only)"),
                AdbCommand("Disable TalkBack"),
                AdbCommand("Show touches"),
                AdbCommand("Hide touches"),
                AdbCommand("Grant all permissions"),
                AdbCommand("Revoke all permissions"),
            )

            // Step 3: Disable some commands
            toggleCommand("Start app")
            toggleCommand("Clear app data")

            // Step 4: Remove a command (remove "Reboot device")
            clickCommand("Reboot device")
            clickRemove()
        }

        deleteAdbCommandConfirmationDialog(robot) {
            clickYes()
        }

        // Step 5: Add a new command
        adbCommandsSettingsFixture(robot) {
            clickAdd()
        }

        addAdbCommandDialog(robot) {
            setName(TEST_COMMAND_NAME)
            setCommand(TEST_COMMAND)
            clickOk()
        }

        // Step 6: Rename a command and move it down
        adbCommandsSettingsFixture(robot) {
            editCommand("Restart app")
        }

        editAdbCommandDialog(robot) {
            setName(RENAMED_RESTART_APP_COMMAND)
            clickOk()
        }

        adbCommandsSettingsFixture(robot) {
            clickCommand(RENAMED_RESTART_APP_COMMAND)
            clickMoveDown()
        }

        // Step 7: Rearrange commands - move "Test Command" up
        adbCommandsSettingsFixture(robot) {
            // New command is at the end, move it up twice
            clickCommand(TEST_COMMAND_NAME)
            clickMoveUp()
            clickMoveUp()
        }

        // Step 8: Click "Apply" button
        settingsDialog(robot) {
            clickApply()
        }

        // Step 9: Close settings
        settingsDialog(robot) {
            clickCancel()
        }

        // Step 10: Open "ADB Commands" settings again
        adbWiFiToolWindow(robot) {
            clickOpenSettings()
        }

        settingsDialog(robot) {
            search("ADB Commands")
        }

        // Step 11: Verify the list of commands contain all commands in correct order and enabled state
        adbCommandsSettingsFixture(robot) {
            assertCommands(
                AdbCommand("Kill app"),
                AdbCommand("Start app", isEnabled = false),
                AdbCommand("Clear app data", isEnabled = false),
                AdbCommand(RENAMED_RESTART_APP_COMMAND),
                AdbCommand("Clear app data and restart"),
                AdbCommand("Uninstall app"),
                AdbCommand("Uninstall app (keep data)"),
                AdbCommand("Install APK"),
                AdbCommand("Simulate incoming SMS (emulator only)"),
                AdbCommand("Enable TalkBack (emulator only)"),
                AdbCommand("Disable TalkBack"),
                AdbCommand("Show touches"),
                AdbCommand("Hide touches"),
                AdbCommand(TEST_COMMAND_NAME),
                AdbCommand("Grant all permissions"),
                AdbCommand("Revoke all permissions"),
            )

            // Clean up: Reset to defaults
            clickReset()
        }

        resetAdbCommandsConfirmationDialog(robot) {
            clickYes()
        }

        settingsDialog(robot) {
            clickOk()
        }
    }
}

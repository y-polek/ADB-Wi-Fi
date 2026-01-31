package dev.polek.adbwifi.uitest

import com.intellij.remoterobot.utils.waitFor
import dev.polek.adbwifi.uitest.fixtures.ideFrame
import dev.polek.adbwifi.uitest.fixtures.settingsDialog
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration

/**
 * UI tests for the ADB Wi-Fi settings panel.
 */
class SettingsPanelUiTest : BaseUiTest() {

    @Test
    fun `settings panel opens and closes correctly`() {
        // Open tool window and navigate to settings
        robot.ideFrame {
            val toolWindow = openAdbWifiToolWindow()
            toolWindow.clickOpenSettings()
        }

        // Wait for settings dialog to open
        waitFor(Duration.ofSeconds(10)) {
            runCatching { robot.settingsDialog() }.isSuccess
        }

        robot.settingsDialog {
            // Navigate to ADB Wi-Fi settings
            navigateToAdbWifiSettings()

            // Wait for settings panel to load and verify we can find the checkbox
            waitFor(Duration.ofSeconds(5)) {
                runCatching { getConfirmDeviceRemovalCheckbox() }.isSuccess
            }

            // Verify checkbox exists and can be interacted with
            val checkbox = getConfirmDeviceRemovalCheckbox()
            assertThat(checkbox).isNotNull

            // Cancel to close without saving
            clickCancel()
        }
    }
}

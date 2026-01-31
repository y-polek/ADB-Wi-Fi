package dev.polek.adbwifi.uitest

import com.intellij.remoterobot.utils.waitFor
import dev.polek.adbwifi.uitest.fixtures.connectDeviceDialog
import dev.polek.adbwifi.uitest.fixtures.ideFrame
import dev.polek.adbwifi.uitest.fixtures.settingsDialog
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration

/**
 * UI tests for the ADB Wi-Fi tool window.
 */
class ToolWindowUiTest : BaseUiTest() {

    @Test
    fun `tool window and toolbar actions work correctly`() {
        robot.ideFrame {
            // Open tool window
            val toolWindow = openAdbWifiToolWindow()

            // Verify tool window is visible (either empty state or device list)
            waitFor(Duration.ofSeconds(5)) {
                toolWindow.isEmptyStateVisible() || toolWindow.isDeviceListVisible()
            }
            assertThat(
                toolWindow.isEmptyStateVisible() || toolWindow.isDeviceListVisible()
            ).isTrue()

            // Verify Connect Device button opens dialog
            toolWindow.clickConnectDevice()
        }

        // Verify dialog opened and close it
        robot.connectDeviceDialog {
            assertThat(ipTextField).isNotNull
            close()
        }

        robot.ideFrame {
            val toolWindow = openAdbWifiToolWindow()

            // Verify Open Settings button opens settings dialog
            toolWindow.clickOpenSettings()
        }

        // Verify settings dialog opened and close it
        waitFor(Duration.ofSeconds(10)) {
            runCatching { robot.settingsDialog() }.isSuccess
        }
        robot.settingsDialog {
            clickCancel()
        }
    }
}

package dev.polek.adbwifi.uitest

import com.intellij.remoterobot.utils.waitFor
import dev.polek.adbwifi.uitest.fixtures.connectDeviceDialog
import dev.polek.adbwifi.uitest.fixtures.ideFrame
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration

/**
 * UI tests for the Connect Device dialog.
 */
class ConnectDeviceDialogUiTest : BaseUiTest() {

    @Test
    fun `connect device dialog works correctly`() {
        // Open tool window and click Connect Device button
        robot.ideFrame {
            val toolWindow = openAdbWifiToolWindow()
            toolWindow.clickConnectDevice()
        }

        robot.connectDeviceDialog {
            // Verify default port is pre-filled
            assertThat(portTextField.text).isEqualTo("5555")

            // Verify Connect button is disabled when IP field is empty
            ipTextField.text = ""
            assertThat(isConnectButtonEnabled()).isFalse()

            // Verify Connect button is enabled when IP is entered
            enterIpAddress("192.168.1.100")
            waitFor(Duration.ofSeconds(2)) {
                isConnectButtonEnabled()
            }
            assertThat(isConnectButtonEnabled()).isTrue()

            // Verify port can be changed
            portTextField.text = ""
            enterPort("5556")
            assertThat(portTextField.text).isEqualTo("5556")

            close()
        }
    }
}

package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

/**
 * Fixture for the Connect Device dialog (ConnectDeviceDialogWrapper).
 */
@FixtureName("Connect Device Dialog")
class ConnectDeviceDialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    /**
     * The IP address text field.
     */
    val ipTextField: JTextFieldFixture
        get() = find(byXpath("//div[@class='JBTextField'][1]"))

    /**
     * The port text field.
     */
    val portTextField: JTextFieldFixture
        get() = find(byXpath("//div[@class='JBTextField'][2]"))

    /**
     * The Connect button.
     */
    val connectButton: JButtonFixture
        get() = find(byXpath("//div[@text='Connect']"))


    /**
     * Enters IP address.
     */
    fun enterIpAddress(ip: String) {
        ipTextField.text = ip
    }

    /**
     * Enters port number.
     */
    fun enterPort(port: String) {
        portTextField.text = port
    }

    /**
     * Checks if Connect button is enabled.
     */
    fun isConnectButtonEnabled(): Boolean {
        return connectButton.isEnabled()
    }

    /**
     * Closes the dialog by pressing Escape.
     * Note: This dialog has no Cancel button - only Connect and X close.
     */
    fun close() {
        runJs("robot.pressAndReleaseKey(java.awt.event.KeyEvent.VK_ESCAPE)")
    }
}

/**
 * Extension function to find the Connect Device dialog.
 */
fun RemoteRobot.connectDeviceDialog(
    timeout: Duration = Duration.ofSeconds(10),
    function: ConnectDeviceDialogFixture.() -> Unit = {}
): ConnectDeviceDialogFixture {
    // Dialog title is "ADB Wi-Fi" (plugin name), try multiple patterns
    val dialogXPaths = listOf(
        "//div[@title='ADB Wi-Fi' and @class='MyDialog']",
        "//div[contains(@title, 'ADB') and @class='MyDialog']",
        "//div[@class='MyDialog']"
    )

    for (xpath in dialogXPaths) {
        try {
            return find<ConnectDeviceDialogFixture>(byXpath(xpath)).apply(function)
        } catch (_: Exception) {
            // Try next pattern
        }
    }

    // Fallback with original timeout
    return find<ConnectDeviceDialogFixture>(
        byXpath("//div[@class='MyDialog']"),
        timeout
    ).apply(function)
}

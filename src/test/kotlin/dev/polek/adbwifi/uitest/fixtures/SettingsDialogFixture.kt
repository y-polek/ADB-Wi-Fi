package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.*
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

/**
 * Fixture for the Settings dialog with ADB Wi-Fi settings.
 */
@FixtureName("Settings Dialog")
class SettingsDialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    /**
     * Navigates to ADB Wi-Fi settings page by searching.
     */
    fun navigateToAdbWifiSettings() {
        // Find the search field in settings and type "ADB Wi-Fi"
        val searchField = find<JTextFieldFixture>(
            byXpath("//div[@class='SettingsSearch']//div[@class='TextFieldWithProcessing']")
        )
        searchField.text = "ADB Wi-Fi"
    }

    fun getConfirmDeviceRemovalCheckbox(): JCheckboxFixture {
        return findCheckbox("Show confirmation dialog when removing a previously connected device")
    }

    /**
     * Finds a checkbox by its text.
     */
    fun findCheckbox(text: String): JCheckboxFixture {
        return find(
            byXpath("//div[@text='$text']")
        )
    }

    /**
     * Clicks Cancel to close settings without applying.
     */
    fun clickCancel() {
        find<JButtonFixture>(byXpath("//div[@text='Cancel']")).click()
    }
}

/**
 * Extension function to find the Settings dialog.
 */
fun RemoteRobot.settingsDialog(
    timeout: Duration = Duration.ofSeconds(10),
    function: SettingsDialogFixture.() -> Unit = {}
): SettingsDialogFixture {
    return find<SettingsDialogFixture>(
        byXpath("//div[@class='MyDialog' and contains(@title, 'Settings')]"),
        timeout
    ).apply(function)
}

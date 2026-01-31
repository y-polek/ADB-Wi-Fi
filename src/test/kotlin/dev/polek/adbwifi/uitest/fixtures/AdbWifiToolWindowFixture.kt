package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath

/**
 * Fixture for the ADB Wi-Fi tool window.
 */
@FixtureName("ADB Wi-Fi Tool Window")
class AdbWifiToolWindowFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    /**
     * Clicks the "Connect Device" action button in the toolbar.
     */
    fun clickConnectDevice() {
        find<JButtonFixture>(byXpath("//div[@tooltiptext='Connect Device']")).click()
    }

    /**
     * Clicks the "Open Settings" action button in the toolbar.
     */
    fun clickOpenSettings() {
        find<JButtonFixture>(byXpath("//div[@tooltiptext='Open Settings']")).click()
    }

    /**
     * Checks if the empty state panel is visible.
     */
    fun isEmptyStateVisible(): Boolean {
        return runCatching {
            find<ComponentFixture>(byXpath("//div[@class='EmptyStatePanel']"))
        }.isSuccess
    }

    /**
     * Checks if device list is visible.
     */
    fun isDeviceListVisible(): Boolean {
        return runCatching {
            find<ComponentFixture>(byXpath("//div[@class='DeviceListPanel']"))
        }.isSuccess
    }
}

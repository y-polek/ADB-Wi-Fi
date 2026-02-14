package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("ADB Wi-Fi Tool Window")
class AdbWifiToolWindowFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    fun clickOpenSettings() {
        find<JButtonFixture>(byXpath("//div[@tooltiptext='Open Settings']")).click()
    }

    companion object {
        fun adbWiFiToolWindow(
            robot: RemoteRobot,
            function: AdbWifiToolWindowFixture.() -> Unit
        ): AdbWifiToolWindowFixture {
            return waitForFixture<AdbWifiToolWindowFixture>(
                robot = robot,
                locator = byXpath("//div[@class='AdbWiFiToolWindow']")
            ).apply(function)
        }
    }
}

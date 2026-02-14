package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

/**
 * Fixture for the main IDE frame.
 */
@FixtureName("IDE Frame")
class IdeFrameFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    private val toolWindowXPath =
        "//div[contains(@accessiblename, 'ADB Wi-Fi') and contains(@class, 'InternalDecoratorImpl')]"

    /**
     * Opens the ADB Wi-Fi tool window if not already open.
     */
    fun openAdbWifiToolWindow() {
        try {
            // First, check if tool window is already open
            find<AdbWifiToolWindowFixture>(byXpath(toolWindowXPath))
        } catch (_: Exception) {
            // Tool window not open, need to click the button
            find<ComponentFixture>(byXpath("//div[contains(@tooltiptext, 'ADB Wi-Fi')]")).click()
        }
    }

    companion object {
        fun ideFrame(
            robot: RemoteRobot,
            function: IdeFrameFixture.() -> Unit
        ): IdeFrameFixture {
            return waitForFixture<IdeFrameFixture>(robot, locator = byXpath("//div[@class='IdeFrameImpl']"))
                .apply(function)
        }
    }
}

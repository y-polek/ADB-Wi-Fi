package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

/**
 * Fixture for the main IDE frame.
 */
@FixtureName("IDE Frame")
class IdeFrameFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    private val toolWindowXPath = "//div[contains(@accessiblename, 'ADB Wi-Fi') and contains(@class, 'InternalDecoratorImpl')]"

    /**
     * Opens the ADB Wi-Fi tool window if not already open, or returns the existing one.
     */
    fun openAdbWifiToolWindow(): AdbWifiToolWindowFixture {
        try {
            // First, check if tool window is already open
            return find(byXpath(toolWindowXPath))
        } catch (_: Exception) {
            // Tool window not open, need to click the button
            find<ComponentFixture>(byXpath("//div[contains(@tooltiptext, 'ADB Wi-Fi')]")).click()
        }
        return find(byXpath(toolWindowXPath))
    }
}

/**
 * Extension function to find the IDE frame.
 */
fun RemoteRobot.ideFrame(
    timeout: Duration = Duration.ofSeconds(20),
    function: IdeFrameFixture.() -> Unit = {}
): IdeFrameFixture {
    return find<IdeFrameFixture>(
        byXpath("//div[@class='IdeFrameImpl']"),
        timeout
    ).apply(function)
}

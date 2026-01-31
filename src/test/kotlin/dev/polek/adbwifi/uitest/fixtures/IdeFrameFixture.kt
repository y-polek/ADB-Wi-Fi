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
        // First, check if tool window is already open
        try {
            return find(byXpath(toolWindowXPath), Duration.ofSeconds(2))
        } catch (e: Exception) {
            // Tool window not open, need to click the button
        }

        // Try different XPath patterns for the tool window button
        val toolWindowButtonXPaths = listOf(
            "//div[contains(@accessiblename, 'ADB Wi-Fi') and contains(@class, 'StripeButton')]",
            "//div[contains(@text, 'ADB Wi-Fi') and contains(@class, 'StripeButton')]",
            "//div[@accessiblename='ADB Wi-Fi' and @class='StripeButton']",
            "//div[contains(@tooltiptext, 'ADB Wi-Fi')]",
            "//div[@text='ADB Wi-Fi']"
        )

        var clicked = false
        for (xpath in toolWindowButtonXPaths) {
            try {
                find<ComponentFixture>(byXpath(xpath), Duration.ofSeconds(2)).click()
                clicked = true
                break
            } catch (e: Exception) {
                // Try next pattern
            }
        }

        if (!clicked) {
            throw AssertionError(
                "Could not find ADB Wi-Fi tool window button. " +
                    "Use http://127.0.0.1:8082/ to explore the UI hierarchy and find the correct XPath."
            )
        }

        // Wait for and return the tool window fixture
        return find(byXpath(toolWindowXPath), Duration.ofSeconds(10))
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

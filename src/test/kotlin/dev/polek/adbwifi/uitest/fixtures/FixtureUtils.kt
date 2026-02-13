package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.Fixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.utils.waitFor

inline fun <reified T : Fixture> waitForFixture(
    robot: RemoteRobot,
    locator: Locator
): T {
    var fixture: T? = null
    waitFor {
        val result = runCatching<T> { robot.find<T>(locator) }
        fixture = result.getOrNull()
        result.isSuccess
    }
    return fixture!!
}

package dev.polek.adbwifi.uitest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.stepsProcessing.StepLogger
import com.intellij.remoterobot.stepsProcessing.StepWorker
import com.intellij.remoterobot.utils.waitFor
import org.junit.After
import org.junit.Assume
import org.junit.Before
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration

/**
 * Base class for Remote Robot UI tests.
 * Provides connection to the IDE and common utilities.
 *
 * To run UI tests:
 * 1. Start IDE with Robot Server: ./gradlew runIdeForUiTests
 * 2. Wait for IDE to fully load
 * 3. Run tests: ./gradlew test --tests "*UiTest*"
 */
abstract class BaseUiTest {

    companion object {
        const val ROBOT_PORT = 8082
        const val ROBOT_URL = "http://127.0.0.1:$ROBOT_PORT"
        val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(20)

        private fun isRobotServerRunning(): Boolean {
            return try {
                val url = URL(ROBOT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 1000
                connection.readTimeout = 1000
                connection.requestMethod = "GET"
                connection.responseCode == 200
            } catch (e: Exception) {
                false
            }
        }
    }

    protected lateinit var robot: RemoteRobot

    @Before
    fun setUp() {
        // Skip tests if Robot Server is not running
        Assume.assumeTrue(
            "Robot Server is not running. Start the IDE first with: ./gradlew runIdeForUiTests",
            isRobotServerRunning()
        )

        StepWorker.registerProcessor(StepLogger())
        robot = RemoteRobot(ROBOT_URL)

        // Wait for IDE to be ready
        waitFor(Duration.ofSeconds(30)) {
            try {
                robot.callJs<Boolean>("true")
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    @After
    fun tearDown() {
        closeAllDialogs()
    }

    /**
     * Close any open dialogs to clean up test state.
     */
    protected fun closeAllDialogs() {
        try {
            val dialogs = robot.findAll<CommonContainerFixture>(
                byXpath("//div[@class='MyDialog']")
            )
            if (dialogs.isNotEmpty()) {
                // Press Escape to close dialogs (works for dialogs without Cancel button)
                robot.runJs("robot.pressAndReleaseKey(java.awt.event.KeyEvent.VK_ESCAPE)")
            }
        } catch (e: Exception) {
            // No dialogs to close
        }
    }
}

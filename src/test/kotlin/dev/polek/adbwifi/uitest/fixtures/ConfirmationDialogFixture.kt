package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("Confirmation Dialog")
class ConfirmationDialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    fun clickYes() {
        find<JButtonFixture>(byXpath("//div[@text='Yes']")).click()
    }

    fun clickNo() {
        find<JButtonFixture>(byXpath("//div[@text='No']")).click()
    }

    companion object {
        fun deleteAdbCommandConfirmationDialog(
            robot: RemoteRobot,
            function: ConfirmationDialogFixture.() -> Unit
        ): ConfirmationDialogFixture {
            return waitForFixture<ConfirmationDialogFixture>(
                robot,
                locator = byXpath("//div[@title='Delete Command']")
            ).apply(function)
        }

        fun resetAdbCommandsConfirmationDialog(
            robot: RemoteRobot,
            function: ConfirmationDialogFixture.() -> Unit
        ): ConfirmationDialogFixture {
            return waitForFixture<ConfirmationDialogFixture>(
                robot,
                locator = byXpath("//div[@title='Reset Commands']")
            ).apply(function)
        }
    }
}

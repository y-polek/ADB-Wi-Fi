package dev.polek.adbwifi.uitest.fixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("Settings Dialog")
class SettingsDialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    fun search(query: String) {
        val searchField = find<JTextFieldFixture>(
            byXpath("//div[@class='SettingsSearch']//div[@class='TextFieldWithProcessing']")
        )
        searchField.text = query
    }

    fun clickOk() {
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()
    }

    fun clickCancel() {
        find<JButtonFixture>(byXpath("//div[@text='Cancel']")).click()
    }

    fun clickApply() {
        find<JButtonFixture>(byXpath("//div[@text='Apply']")).click()
    }

    companion object {
        fun settingsDialog(
            robot: RemoteRobot,
            function: SettingsDialogFixture.() -> Unit
        ): SettingsDialogFixture {
            return waitForFixture<SettingsDialogFixture>(
                robot = robot,
                locator = byXpath("//div[@class='MyDialog' and contains(@title, 'Settings')]")
            ).apply(function)
        }
    }
}

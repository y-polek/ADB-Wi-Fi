package dev.polek.adbwifi.uitest.fixtures

import com.google.common.truth.Truth.assertThat
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JTextFieldFixture
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("ADB Command Editor Dialog")
class AdbCommandEditorDialogFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    fun setName(name: String) {
        findNameField().text = name
    }

    fun assertName(expected: String) {
        assertThat(findNameField().text).isEqualTo(expected)
    }

    fun setCommand(command: String) {
        findCommandArea().text = command
    }

    fun assertCommand(expected: String) {
        assertThat(findCommandArea().text).isEqualTo(expected)
    }

    fun toggleConfirmation() {
        findConfirmationCheckbox().click()
    }

    fun assertConfirmationSelected(expected: Boolean) {
        val isSelected: Boolean = findConfirmationCheckbox().callJs("component.isSelected()")
        assertThat(isSelected).isEqualTo(expected)
    }

    fun clickOk() {
        find<JButtonFixture>(byXpath("//div[@text='OK']")).click()
    }

    fun clickCancel() {
        find<JButtonFixture>(byXpath("//div[@text='Cancel']")).click()
    }

    private fun findNameField(): JTextFieldFixture {
        return find(byXpath("//div[@class='JBTextField']"))
    }

    private fun findCommandArea(): JTextFieldFixture {
        return find(byXpath("//div[@accessiblename='Command:' and @class='JBScrollPane']//div[@class='JBTextArea']"))
    }

    private fun findConfirmationCheckbox(): ComponentFixture {
        return find(byXpath("//div[@text='Ask for confirmation before executing']"))
    }

    companion object {
        fun addAdbCommandDialog(
            robot: RemoteRobot,
            function: AdbCommandEditorDialogFixture.() -> Unit
        ): AdbCommandEditorDialogFixture {
            return waitForFixture<AdbCommandEditorDialogFixture>(
                robot,
                locator = byXpath("//div[@title='Add Command']")
            ).apply(function)
        }

        fun editAdbCommandDialog(
            robot: RemoteRobot,
            function: AdbCommandEditorDialogFixture.() -> Unit
        ): AdbCommandEditorDialogFixture {
            return waitForFixture<AdbCommandEditorDialogFixture>(
                robot,
                locator = byXpath("//div[@title='Edit Command']")
            ).apply(function)
        }
    }
}

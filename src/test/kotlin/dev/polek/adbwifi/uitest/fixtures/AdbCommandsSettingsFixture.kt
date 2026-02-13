package dev.polek.adbwifi.uitest.fixtures

import com.google.common.truth.Truth.assertThat
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.fixtures.JButtonFixture
import com.intellij.remoterobot.fixtures.JListFixture
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("ADB Commands Settings")
class AdbCommandsSettingsFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : CommonContainerFixture(remoteRobot, remoteComponent) {

    fun assertCommands(vararg commands: AdbCommand) {
        val commandsList = findCommandsList()
        val actual: List<List<Any>> = commandsList.callJs(
            """
            var model = component.getModel();
            var size = model.getSize();
            var result = new java.util.ArrayList();
            for (var i = 0; i < size; i++) {
                var pair = new java.util.ArrayList();
                pair.add(model.getElementAt(i).getText());
                pair.add(model.getElementAt(i).isSelected());
                result.add(pair);
            }
            result;
            """
        )
        val actualCommands = actual.map { AdbCommand(it[0] as String, it[1] as Boolean) }
        assertThat(actualCommands).isEqualTo(commands.toList())
    }

    fun clickCommand(index: Int) {
        findCommandsList().clickItemAtIndex(index)
    }

    fun clickCommand(name: String) {
        findCommandsList().clickItem(name, fullMatch = false)
    }

    fun toggleCommand(name: String) {
        val commandsList = findCommandsList()
        commandsList.runJs(
            """
            var model = component.getModel();
            var size = model.getSize();
            for (var i = 0; i < size; i++) {
                if (model.getElementAt(i).getText() == "$name") {
                    var bounds = component.getCellBounds(i, i);
                    var x = bounds.x + 10;
                    var y = bounds.y + bounds.height / 2;
                    robot.click(component, new java.awt.Point(x, y));
                    break;
                }
            }
            """
        )
    }

    fun editCommand(name: String) {
        clickCommand(name)
        clickEdit()
    }

    fun clickAdd() {
        clickButton("Add")
    }

    fun clickRemove() {
        clickButton("Remove")
    }

    fun clickMoveUp() {
        clickButton("Up")
    }

    fun clickMoveDown() {
        clickButton("Down")
    }

    fun clickEdit() {
        clickButton("Edit")
    }

    fun clickDuplicate() {
        clickButton("Duplicate")
    }

    fun clickReset() {
        clickButton("Reset to defaults")
    }

    fun clickExport() {
        clickButton("Export")
    }

    fun clickImport() {
        clickButton("Import")
    }

    private fun clickButton(tooltip: String) {
        find<JButtonFixture>(byXpath("//div[@tooltiptext='$tooltip']")).click()
    }

    private fun findCommandsList(): JListFixture {
        return find(byXpath("//div[@accessiblename='ADB Commands List']"))
    }

    data class AdbCommand(
        val name: String,
        val isEnabled: Boolean = true
    )

    companion object {
        fun adbCommandsSettingsFixture(
            robot: RemoteRobot,
            function: AdbCommandsSettingsFixture.() -> Unit
        ): AdbCommandsSettingsFixture {
            return waitForFixture<AdbCommandsSettingsFixture>(
                robot,
                locator = byXpath("//div[@class='AdbCommandsSettingsPanel']")
            ).apply(function)
        }
    }
}


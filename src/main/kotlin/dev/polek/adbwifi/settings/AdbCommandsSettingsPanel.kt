package dev.polek.adbwifi.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.Messages
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ActionIconsProvider
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.ui.view.AdbCommandEditorDialog
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class AdbCommandsSettingsPanel : JBPanel<AdbCommandsSettingsPanel>(BorderLayout()) {

    private var commands: MutableList<AdbCommandConfig> = mutableListOf()

    private val checkBoxList = object : CheckBoxList<AdbCommandConfig>() {
        override fun getSecondaryText(index: Int): String? = null

        override fun isEnabled(index: Int): Boolean = true

        init {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        val index = locationToIndex(e.point)
                        if (index < 0 || index >= itemsCount) return
                        selectedIndex = index
                        editCommand()
                    }
                }
            })
        }
    }.apply {
        setCellRenderer(CommandCellRenderer())
    }

    init {
        val decorator = ToolbarDecorator.createDecorator(checkBoxList)
            .setAddAction { addCommand() }
            .setRemoveAction { removeCommand() }
            .setMoveUpAction { moveUp() }
            .setMoveDownAction { moveDown() }
            .addExtraAction(object : ToolbarDecorator.ElementActionButton(
                PluginBundle.message("adbCommandEditButton"),
                AllIcons.Actions.Edit
            ) {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) {
                    editCommand()
                }

                override fun isEnabled(): Boolean {
                    return checkBoxList.selectedIndex >= 0
                }
            })
            .addExtraAction(object : ToolbarDecorator.ElementActionButton(
                PluginBundle.message("adbCommandResetButton"),
                AllIcons.Actions.ForceRefresh
            ) {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) {
                    resetToDefaults()
                }
            })
            .setRemoveActionUpdater { canRemoveSelected() }

        add(decorator.createPanel(), BorderLayout.CENTER)
    }

    fun loadFromService(service: AdbCommandsService) {
        commands = service.commands.sortedBy { it.order }.toMutableList()
        refreshList()
    }

    fun applyToService(service: AdbCommandsService) {
        // Update enabled state from checkboxes
        commands.forEachIndexed { index, config ->
            val isEnabled = checkBoxList.isItemSelected(index)
            if (config.isEnabled != isEnabled) {
                commands[index] = config.copy(isEnabled = isEnabled)
            }
        }
        // Update order based on list position
        commands = commands.mapIndexed { index, config ->
            config.copy(order = index)
        }.toMutableList()

        service.commands = commands.toList()
    }

    fun isModified(service: AdbCommandsService): Boolean {
        val serviceCommands = service.commands.sortedBy { it.order }
        if (commands.size != serviceCommands.size) return true

        commands.forEachIndexed { index, config ->
            val isEnabled = checkBoxList.isItemSelected(index)
            val original = serviceCommands.getOrNull(index) ?: return true
            if (config.id != original.id) return true
            if (isEnabled != original.isEnabled) return true
            if (config.name != original.name) return true
            if (config.command != original.command) return true
            if (config.iconId != original.iconId) return true
        }
        return false
    }

    private fun syncCheckboxStatesToCommands() {
        commands.forEachIndexed { index, config ->
            val isChecked = checkBoxList.isItemSelected(index)
            if (config.isEnabled != isChecked) {
                commands[index] = config.copy(isEnabled = isChecked)
            }
        }
    }

    private fun refreshList() {
        checkBoxList.clear()
        commands.forEach { config ->
            checkBoxList.addItem(config, config.name, config.isEnabled)
        }
    }

    private fun addCommand() {
        syncCheckboxStatesToCommands()
        val dialog = AdbCommandEditorDialog()
        if (dialog.showAndGet()) {
            val maxOrder = commands.maxOfOrNull { it.order } ?: -1
            val newCommand = AdbCommandConfig(
                id = java.util.UUID.randomUUID().toString(),
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                isEnabled = true,
                order = maxOrder + 1
            )
            commands.add(newCommand)
            refreshList()
        }
    }

    private fun editCommand() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex < 0) return

        syncCheckboxStatesToCommands()
        val config = commands[selectedIndex]
        val dialog = AdbCommandEditorDialog(config)
        if (dialog.showAndGet()) {
            commands[selectedIndex] = config.copy(
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId
            )
            refreshList()
            checkBoxList.selectedIndex = selectedIndex
        }
    }

    private fun removeCommand() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex < 0) return

        syncCheckboxStatesToCommands()
        val config = commands[selectedIndex]

        val result = Messages.showYesNoDialog(
            PluginBundle.message("adbCommandDeleteConfirmation", config.name),
            PluginBundle.message("adbCommandDeleteTitle"),
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            commands.removeAt(selectedIndex)
            refreshList()
        }
    }

    private fun canRemoveSelected(): Boolean {
        val selectedIndex = checkBoxList.selectedIndex
        return selectedIndex >= 0
    }

    private fun moveUp() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex <= 0) return

        syncCheckboxStatesToCommands()
        val temp = commands[selectedIndex]
        commands[selectedIndex] = commands[selectedIndex - 1]
        commands[selectedIndex - 1] = temp
        refreshList()
        checkBoxList.selectedIndex = selectedIndex - 1
    }

    private fun moveDown() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex < 0 || selectedIndex >= commands.lastIndex) return

        syncCheckboxStatesToCommands()
        val temp = commands[selectedIndex]
        commands[selectedIndex] = commands[selectedIndex + 1]
        commands[selectedIndex + 1] = temp
        refreshList()
        checkBoxList.selectedIndex = selectedIndex + 1
    }

    private fun resetToDefaults() {
        val result = Messages.showYesNoDialog(
            PluginBundle.message("adbCommandResetConfirmation"),
            PluginBundle.message("adbCommandResetTitle"),
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            commands = AdbCommandsService.defaultCommands().toMutableList()
            refreshList()
        }
    }

    private inner class CommandCellRenderer : ListCellRenderer<JCheckBox> {
        private val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply { isOpaque = true }
        private val checkBox = JCheckBox().apply { isOpaque = false }
        private val iconLabel = JLabel().apply { border = JBUI.Borders.empty(0, 4) }
        private val textLabel = JLabel()

        init {
            panel.add(checkBox)
            panel.add(iconLabel)
            panel.add(textLabel)
        }

        override fun getListCellRendererComponent(
            list: JList<out JCheckBox>,
            value: JCheckBox?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val bg = if (isSelected) list.selectionBackground else list.background
            val fg = if (isSelected) list.selectionForeground else list.foreground

            panel.background = bg
            checkBox.background = bg
            textLabel.foreground = fg
            textLabel.background = bg

            if (value != null) {
                checkBox.isSelected = value.isSelected
                checkBox.isEnabled = value.isEnabled
                textLabel.text = value.text
                textLabel.isEnabled = value.isEnabled
            }

            if (index >= 0 && index < commands.size) {
                val config = commands[index]
                iconLabel.icon = ActionIconsProvider.getIconById(config.iconId)?.icon
            }

            return panel
        }
    }
}

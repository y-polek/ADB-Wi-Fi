package dev.polek.adbwifi.settings

import com.intellij.openapi.ui.Messages
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.CommandIcon
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.ui.view.AdbCommandEditorDialog
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

class AdbCommandsSettingsPanel : JBPanel<AdbCommandsSettingsPanel>(BorderLayout()) {

    private var commands: MutableList<AdbCommandConfig> = mutableListOf()

    private val checkBoxList = object : CheckBoxList<AdbCommandConfig>() {
        override fun getSecondaryText(index: Int): String? = null

        override fun isEnabled(index: Int): Boolean = true

        init {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val index = locationToIndex(e.point)
                    if (index < 0 || index >= itemsCount) return

                    val cellBounds = getCellBounds(index, index) ?: return
                    val relativeX = e.x - cellBounds.x

                    when {
                        e.clickCount == 2 -> {
                            // Double-click opens edit dialog
                            selectedIndex = index
                            editCommand()
                        }
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
                CommandIcon.EDIT.icon
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
                CommandIcon.FORCE_REFRESH.icon
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

    private fun refreshList() {
        checkBoxList.clear()
        commands.forEach { config ->
            val icon = CommandIcon.fromId(config.iconId)?.icon
            checkBoxList.addItem(config, config.name, config.isEnabled)
        }
    }

    private fun addCommand() {
        val dialog = AdbCommandEditorDialog()
        if (dialog.showAndGet()) {
            val maxOrder = commands.maxOfOrNull { it.order } ?: -1
            val newCommand = AdbCommandConfig(
                id = java.util.UUID.randomUUID().toString(),
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                isBuiltIn = false,
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

        val config = commands[selectedIndex]
        if (config.isBuiltIn) return

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
        if (selectedIndex < 0) return false
        return !commands[selectedIndex].isBuiltIn
    }

    private fun moveUp() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex <= 0) return

        val temp = commands[selectedIndex]
        commands[selectedIndex] = commands[selectedIndex - 1]
        commands[selectedIndex - 1] = temp
        refreshList()
        checkBoxList.selectedIndex = selectedIndex - 1
    }

    private fun moveDown() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex < 0 || selectedIndex >= commands.lastIndex) return

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
                iconLabel.icon = CommandIcon.fromId(config.iconId)?.icon
            }

            return panel
        }
    }
}

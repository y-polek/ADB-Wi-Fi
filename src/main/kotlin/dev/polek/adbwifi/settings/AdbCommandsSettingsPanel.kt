package dev.polek.adbwifi.settings

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ActionIconsProvider
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.AdbCommandsExportFile
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.ui.view.AdbCommandEditorDialog
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
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
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandEditButton"),
                null,
                AllIcons.Actions.Edit
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    editCommand()
                }

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = checkBoxList.selectedIndex >= 0
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            })
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandDuplicateButton"),
                null,
                AllIcons.Actions.Copy
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    duplicateCommand()
                }

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = checkBoxList.selectedIndex >= 0
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            })
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandResetButton"),
                null,
                AllIcons.Actions.ForceRefresh
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    resetToDefaults()
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            })
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandExportButton"),
                null,
                AllIcons.ToolbarDecorator.Export
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    showExportPopup(e)
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            })
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandImportButton"),
                null,
                AllIcons.ToolbarDecorator.Import
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    importCommands()
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
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
            if (isEnabled != original.isEnabled) return true
            if (config.name != original.name) return true
            if (config.command != original.command) return true
            if (config.iconId != original.iconId) return true
            if (config.requiresConfirmation != original.requiresConfirmation) return true
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
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                isEnabled = true,
                order = maxOrder + 1,
                requiresConfirmation = dialog.requiresConfirmation
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
                iconId = dialog.iconId,
                requiresConfirmation = dialog.requiresConfirmation
            )
            refreshList()
            checkBoxList.selectedIndex = selectedIndex
        }
    }

    private fun duplicateCommand() {
        val selectedIndex = checkBoxList.selectedIndex
        if (selectedIndex < 0) return

        syncCheckboxStatesToCommands()
        val config = commands[selectedIndex]
        val duplicatedConfig = config.copy(name = "${config.name} (copy)")
        val dialog = AdbCommandEditorDialog(duplicatedConfig)
        if (dialog.showAndGet()) {
            val maxOrder = commands.maxOfOrNull { it.order } ?: -1
            val newCommand = AdbCommandConfig(
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                isEnabled = true,
                order = maxOrder + 1,
                requiresConfirmation = dialog.requiresConfirmation
            )
            commands.add(newCommand)
            refreshList()
            checkBoxList.selectedIndex = commands.lastIndex
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

    private sealed class ExportMenuItem(val text: String) {
        data object ExportAll : ExportMenuItem(PluginBundle.message("adbCommandExportAll"))
        data object ExportEnabled : ExportMenuItem(PluginBundle.message("adbCommandExportEnabled"))
    }

    private fun showExportPopup(e: AnActionEvent) {
        val items = listOf(ExportMenuItem.ExportAll, ExportMenuItem.ExportEnabled)

        val step = object : BaseListPopupStep<ExportMenuItem>(null, items) {
            override fun getTextFor(value: ExportMenuItem): String = value.text

            override fun onChosen(selectedValue: ExportMenuItem, finalChoice: Boolean): PopupStep<*>? {
                return doFinalStep {
                    when (selectedValue) {
                        ExportMenuItem.ExportAll -> exportCommands(exportEnabledOnly = false)
                        ExportMenuItem.ExportEnabled -> exportCommands(exportEnabledOnly = true)
                    }
                }
            }
        }

        val popup = JBPopupFactory.getInstance().createListPopup(step)
        e.inputEvent?.component?.let { popup.showUnderneathOf(it) }
            ?: popup.showInFocusCenter()
    }

    private fun exportCommands(exportEnabledOnly: Boolean) {
        syncCheckboxStatesToCommands()

        val commandsToExport = if (exportEnabledOnly) {
            commands.filter { it.isEnabled }
        } else {
            commands
        }.sortedBy { it.order }

        if (commandsToExport.isEmpty()) {
            Messages.showInfoMessage(
                PluginBundle.message("adbCommandExportNoCommands"),
                PluginBundle.message("adbCommandExportTitle")
            )
            return
        }

        val exportData = AdbCommandsExportFile(
            version = 1,
            commands = commandsToExport
        )

        val descriptor = FileSaverDescriptor(
            PluginBundle.message("adbCommandExportTitle"),
            PluginBundle.message("adbCommandExportDescription"),
            "json"
        )

        val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, null as Project?)
        val wrapper = saveFileDialog.save(null as VirtualFile?, "adb-commands.json")

        if (wrapper != null) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(exportData)
            wrapper.file.writeText(json)
        }
    }

    private fun importCommands() {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension == "json" }
            .withTitle(PluginBundle.message("adbCommandImportTitle"))

        FileChooser.chooseFile(descriptor, null, this, null) { selectedFile ->
            @Suppress("TooGenericExceptionCaught")
            try {
                val json = File(selectedFile.path).readText()
                val gson = Gson()
                val exportFile = gson.fromJson(json, AdbCommandsExportFile::class.java)

                if (exportFile?.commands.isNullOrEmpty()) {
                    Messages.showWarningDialog(
                        PluginBundle.message("adbCommandImportEmpty"),
                        PluginBundle.message("adbCommandImportTitle")
                    )
                    return@chooseFile
                }

                showImportModeDialog(exportFile.commands)
            } catch (e: Exception) {
                Messages.showErrorDialog(
                    PluginBundle.message("adbCommandImportError", e.message ?: "Unknown error"),
                    PluginBundle.message("adbCommandImportTitle")
                )
            }
        }
    }

    private fun showImportModeDialog(importedCommands: List<AdbCommandConfig>) {
        val options = arrayOf(
            PluginBundle.message("adbCommandImportReplace"),
            PluginBundle.message("adbCommandImportMerge"),
            PluginBundle.message("cancelButton")
        )

        val result = Messages.showDialog(
            PluginBundle.message("adbCommandImportModeMessage"),
            PluginBundle.message("adbCommandImportTitle"),
            options,
            0,
            Messages.getQuestionIcon()
        )

        when (result) {
            0 -> mergeCommands(importedCommands)
            1 -> replaceCommands(importedCommands)
        }
    }

    private fun replaceCommands(importedCommands: List<AdbCommandConfig>) {
        commands = importedCommands.mapIndexed { index, config ->
            config.copy(order = index)
        }.toMutableList()
        refreshList()
    }

    private fun mergeCommands(importedCommands: List<AdbCommandConfig>) {
        syncCheckboxStatesToCommands()

        var nextOrder = (commands.maxOfOrNull { it.order } ?: -1) + 1
        val newCommands = importedCommands.map { it.copy(order = nextOrder++) }

        commands.addAll(newCommands)
        refreshList()
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

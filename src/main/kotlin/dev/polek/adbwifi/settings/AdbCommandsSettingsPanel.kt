package dev.polek.adbwifi.settings

import com.google.gson.GsonBuilder
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.*
import dev.polek.adbwifi.model.CommandListItem.AdbCommand
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.ui.view.AdbCommandEditorDialog
import dev.polek.adbwifi.ui.view.GroupEditorDialog
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*

class AdbCommandsSettingsPanel : JBPanel<AdbCommandsSettingsPanel>(BorderLayout()) {

    private var items: MutableList<CommandListItem> = mutableListOf()

    private val listModel = DefaultListModel<SettingsListItem>()
    private val itemList = JBList(listModel).apply {
        cellRenderer = SettingsListCellRenderer()
        accessibleContext?.accessibleName = "ADB Commands List"
        selectionMode = ListSelectionModel.SINGLE_SELECTION
    }

    private sealed class SettingsListItem {
        data class GroupHeader(val group: AdbCommandGroup) : SettingsListItem()
        data class CommandItem(
            val config: AdbCommand,
            val group: AdbCommandGroup?
        ) : SettingsListItem()
    }

    init {
        // Handle double-click to edit and checkbox toggling
        itemList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val index = itemList.locationToIndex(e.point)
                if (index < 0) return
                val cellBounds = itemList.getCellBounds(index, index) ?: return
                if (!cellBounds.contains(e.point)) return

                val item = listModel.getElementAt(index)

                if (e.clickCount == 2) {
                    itemList.selectedIndex = index
                    when (item) {
                        is SettingsListItem.GroupHeader -> editGroup()
                        is SettingsListItem.CommandItem -> editCommand()
                    }
                } else if (e.clickCount == 1 && item is SettingsListItem.CommandItem) {
                    // Check if click is in checkbox area
                    val relativeX = e.point.x - cellBounds.x
                    val indent = if (item.group != null) JBUI.scale(INDENT_WIDTH) else 0
                    if (relativeX >= indent && relativeX < indent + JBUI.scale(CHECKBOX_WIDTH)) {
                        toggleCommandEnabled(item.config, item.group)
                    }
                }
            }
        })

        val decorator = ToolbarDecorator.createDecorator(itemList)
            .setAddAction { showAddPopup(it.preferredPopupPoint) }
            .setRemoveAction { removeSelected() }
            .setMoveUpAction { moveUp() }
            .setMoveDownAction { moveDown() }
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandEditButton"),
                null,
                AllIcons.Actions.Edit
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val item = getSelectedItem()
                    when (item) {
                        is SettingsListItem.GroupHeader -> editGroup()
                        is SettingsListItem.CommandItem -> editCommand()
                        null -> {}
                    }
                }

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = itemList.selectedIndex >= 0
                }

                override fun getActionUpdateThread() = ActionUpdateThread.EDT
            })
            .addExtraAction(object : DumbAwareAction(
                PluginBundle.message("adbCommandDuplicateButton"),
                null,
                AllIcons.Actions.Copy
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    val item = getSelectedItem()
                    when (item) {
                        is SettingsListItem.GroupHeader -> duplicateGroup()
                        is SettingsListItem.CommandItem -> duplicateCommand()
                        null -> {}
                    }
                }

                override fun update(e: AnActionEvent) {
                    e.presentation.isEnabled = itemList.selectedIndex >= 0
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
                    exportCommands(e.project)
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
            .setRemoveActionUpdater { itemList.selectedIndex >= 0 }

        add(decorator.createPanel(), BorderLayout.CENTER)
    }

    fun loadFromService(service: AdbCommandsService) {
        items = service.items.toMutableList()
        refreshList()
    }

    fun applyToService(service: AdbCommandsService) {
        service.items = items.toList()
    }

    fun isModified(service: AdbCommandsService): Boolean {
        return items != service.items
    }

    // region List building

    private fun refreshList() {
        val selectedIndex = itemList.selectedIndex
        listModel.clear()

        for (item in items) {
            when (item) {
                is AdbCommand -> {
                    listModel.addElement(SettingsListItem.CommandItem(item, group = null))
                }
                is AdbCommandGroup -> {
                    listModel.addElement(SettingsListItem.GroupHeader(item))
                    item.commands.forEach { cmd ->
                        listModel.addElement(SettingsListItem.CommandItem(cmd, group = item))
                    }
                }
            }
        }

        if (selectedIndex in 0 until listModel.size()) {
            itemList.selectedIndex = selectedIndex
        }
    }

    // endregion

    // region Checkbox handling

    private fun toggleCommandEnabled(config: AdbCommand, group: AdbCommandGroup?) {
        val toggled = config.copy(isEnabled = !config.isEnabled)
        if (group != null) {
            replaceCommandInGroup(group, config, toggled)
        } else {
            val idx = items.indexOf(config)
            if (idx >= 0) items[idx] = toggled
        }
        refreshList()
    }

    // endregion

    // region Add

    private fun showAddPopup(point: com.intellij.ui.awt.RelativePoint?) {
        val popupItems = listOf(
            PluginBundle.message("adbCommandAddCommand"),
            PluginBundle.message("adbCommandAddGroup")
        )
        val step = object : BaseListPopupStep<String>(null, popupItems) {
            override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
                return doFinalStep {
                    when (selectedValue) {
                        popupItems[0] -> addCommand()
                        popupItems[1] -> addGroup()
                    }
                }
            }
        }
        val popup = JBPopupFactory.getInstance().createListPopup(step)
        if (point != null) popup.show(point) else popup.showInFocusCenter()
    }

    private fun addCommand() {
        val groups = items.filterIsInstance<AdbCommandGroup>()
        val dialog = AdbCommandEditorDialog(availableGroups = groups)
        if (dialog.showAndGet()) {
            val newCommand = AdbCommand(
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                iconColor = dialog.iconColor,
                isEnabled = true,
                requiresConfirmation = dialog.requiresConfirmation
            )
            val targetGroup = dialog.selectedGroup
            if (targetGroup != null) {
                val groupIdx = items.indexOf(targetGroup)
                if (groupIdx >= 0) {
                    items[groupIdx] = targetGroup.copy(commands = targetGroup.commands + newCommand)
                }
            } else {
                items.add(newCommand)
            }
            refreshList()
        }
    }

    private fun addGroup() {
        val dialog = GroupEditorDialog()
        if (dialog.showAndGet()) {
            val newGroup = AdbCommandGroup(
                name = dialog.groupName,
                iconId = dialog.iconId,
                iconColor = dialog.iconColor
            )
            items.add(newGroup)
            refreshList()
        }
    }

    // endregion

    // region Edit

    private fun editCommand() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0) return
        val item = listModel.getElementAt(selectedIndex) as? SettingsListItem.CommandItem ?: return

        val config = item.config
        val currentGroup = item.group
        val groups = items.filterIsInstance<AdbCommandGroup>()
        val dialog = AdbCommandEditorDialog(config, groups, currentGroup)
        if (!dialog.showAndGet()) return

        val updatedConfig = config.copy(
            name = dialog.commandName,
            command = dialog.command,
            iconId = dialog.iconId,
            iconColor = dialog.iconColor,
            requiresConfirmation = dialog.requiresConfirmation
        )

        // Remove from old location
        removeCommandFromLocation(config, currentGroup)

        // Add to new location
        addCommandToLocation(updatedConfig, dialog.selectedGroup)

        refreshList()
        itemList.selectedIndex = selectedIndex.coerceAtMost(listModel.size() - 1)
    }

    private fun removeCommandFromLocation(config: AdbCommand, group: AdbCommandGroup?) {
        if (group != null) {
            val groupIdx = items.indexOf(group)
            if (groupIdx >= 0) {
                items[groupIdx] = group.copy(commands = group.commands - config)
            }
        } else {
            items.remove(config)
        }
    }

    private fun addCommandToLocation(config: AdbCommand, group: AdbCommandGroup?) {
        if (group != null) {
            // Re-find group (may have been updated by removal)
            val freshGroup = items.filterIsInstance<AdbCommandGroup>()
                .find { it === group || it.name == group.name }
            if (freshGroup != null) {
                val groupIdx = items.indexOf(freshGroup)
                if (groupIdx >= 0) {
                    items[groupIdx] = freshGroup.copy(commands = freshGroup.commands + config)
                }
            }
        } else {
            items.add(config)
        }
    }

    private fun editGroup() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0) return
        val item = listModel.getElementAt(selectedIndex) as? SettingsListItem.GroupHeader ?: return

        val group = item.group
        val dialog = GroupEditorDialog(group)
        if (dialog.showAndGet()) {
            val groupIndex = items.indexOf(group)
            if (groupIndex >= 0) {
                items[groupIndex] = group.copy(
                    name = dialog.groupName,
                    iconId = dialog.iconId,
                    iconColor = dialog.iconColor
                )
                refreshList()
                itemList.selectedIndex = selectedIndex
            }
        }
    }

    // endregion

    // region Duplicate

    private fun duplicateCommand() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0) return
        val item = listModel.getElementAt(selectedIndex) as? SettingsListItem.CommandItem ?: return

        val config = item.config
        val currentGroup = item.group
        val groups = items.filterIsInstance<AdbCommandGroup>()
        val duplicatedConfig = config.copy(name = "${config.name} (copy)")
        val dialog = AdbCommandEditorDialog(duplicatedConfig, groups, currentGroup)
        if (dialog.showAndGet()) {
            val newCommand = AdbCommand(
                name = dialog.commandName,
                command = dialog.command,
                iconId = dialog.iconId,
                iconColor = dialog.iconColor,
                isEnabled = true,
                requiresConfirmation = dialog.requiresConfirmation
            )
            val targetGroup = dialog.selectedGroup
            if (targetGroup != null) {
                val groupIdx = items.indexOf(targetGroup)
                if (groupIdx >= 0) {
                    items[groupIdx] = targetGroup.copy(commands = targetGroup.commands + newCommand)
                }
            } else {
                items.add(newCommand)
            }
            refreshList()
        }
    }

    private fun duplicateGroup() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0) return
        val item = listModel.getElementAt(selectedIndex) as? SettingsListItem.GroupHeader ?: return

        val group = item.group
        val newGroup = group.copy(name = "${group.name} (copy)")
        items.add(newGroup)
        refreshList()
    }

    // endregion

    // region Remove

    private fun removeSelected() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0) return

        when (val item = listModel.getElementAt(selectedIndex)) {
            is SettingsListItem.CommandItem -> removeCommand(item.config, item.group)
            is SettingsListItem.GroupHeader -> removeGroup(item.group)
        }
    }

    private fun removeCommand(config: AdbCommand, group: AdbCommandGroup?) {
        val result = Messages.showYesNoDialog(
            PluginBundle.message("adbCommandDeleteConfirmation", config.name),
            PluginBundle.message("adbCommandDeleteTitle"),
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            if (group != null) {
                val groupIdx = items.indexOf(group)
                if (groupIdx >= 0) {
                    items[groupIdx] = group.copy(commands = group.commands - config)
                }
            } else {
                items.remove(config)
            }
            refreshList()
        }
    }

    private fun removeGroup(group: AdbCommandGroup) {
        val result = Messages.showYesNoDialog(
            PluginBundle.message("adbCommandGroupDeleteConfirmation", group.name),
            PluginBundle.message("adbCommandGroupDeleteTitle"),
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            val groupIdx = items.indexOf(group)
            if (groupIdx >= 0) {
                // Replace group with its ungrouped commands
                items.removeAt(groupIdx)
                group.commands.reversed().forEach { cmd ->
                    items.add(groupIdx, cmd)
                }
            }
            refreshList()
        }
    }

    // endregion

    // region Move

    private fun moveUp() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex <= 0) return

        val item = listModel.getElementAt(selectedIndex)
        when (item) {
            is SettingsListItem.CommandItem -> moveCommandUp(selectedIndex, item.config, item.group)
            is SettingsListItem.GroupHeader -> moveGroupUp(item.group)
        }
    }

    private fun moveDown() {
        val selectedIndex = itemList.selectedIndex
        if (selectedIndex < 0 || selectedIndex >= listModel.size() - 1) return

        val item = listModel.getElementAt(selectedIndex)
        when (item) {
            is SettingsListItem.CommandItem -> moveCommandDown(selectedIndex, item.config, item.group)
            is SettingsListItem.GroupHeader -> moveGroupDown(item.group)
        }
    }

    private fun moveCommandUp(listIndex: Int, config: AdbCommand, group: AdbCommandGroup?) {
        if (group != null) {
            // Command is in a group - swap with previous command in same group
            val cmdIdx = group.commands.indexOf(config)
            if (cmdIdx > 0) {
                val newCommands = group.commands.toMutableList()
                newCommands[cmdIdx] = newCommands[cmdIdx - 1].also { newCommands[cmdIdx - 1] = newCommands[cmdIdx] }
                val groupIdx = items.indexOf(group)
                if (groupIdx >= 0) {
                    items[groupIdx] = group.copy(commands = newCommands)
                }
                refreshList()
                itemList.selectedIndex = listIndex - 1
            }
        } else {
            // Ungrouped command (top-level) - swap with previous top-level item
            val idx = items.indexOf(config)
            if (idx > 0) {
                items[idx] = items[idx - 1].also { items[idx - 1] = items[idx] }
                refreshList()
                selectTopLevelItem(items[idx - 1])
            }
        }
    }

    private fun moveCommandDown(listIndex: Int, config: AdbCommand, group: AdbCommandGroup?) {
        if (group != null) {
            // Command is in a group - swap with next command in same group
            val cmdIdx = group.commands.indexOf(config)
            if (cmdIdx >= 0 && cmdIdx < group.commands.size - 1) {
                val newCommands = group.commands.toMutableList()
                newCommands[cmdIdx] = newCommands[cmdIdx + 1].also { newCommands[cmdIdx + 1] = newCommands[cmdIdx] }
                val groupIdx = items.indexOf(group)
                if (groupIdx >= 0) {
                    items[groupIdx] = group.copy(commands = newCommands)
                }
                refreshList()
                itemList.selectedIndex = listIndex + 1
            }
        } else {
            // Ungrouped command (top-level) - swap with next top-level item
            val idx = items.indexOf(config)
            if (idx >= 0 && idx < items.size - 1) {
                items[idx] = items[idx + 1].also { items[idx + 1] = items[idx] }
                refreshList()
                selectTopLevelItem(items[idx + 1])
            }
        }
    }

    private fun moveGroupUp(group: AdbCommandGroup) {
        val idx = items.indexOf(group)
        if (idx > 0) {
            items[idx] = items[idx - 1].also { items[idx - 1] = items[idx] }
            refreshList()
            selectGroupHeader(group)
        }
    }

    private fun moveGroupDown(group: AdbCommandGroup) {
        val idx = items.indexOf(group)
        if (idx >= 0 && idx < items.size - 1) {
            items[idx] = items[idx + 1].also { items[idx + 1] = items[idx] }
            refreshList()
            selectGroupHeader(group)
        }
    }

    private fun selectTopLevelItem(item: CommandListItem) {
        for (i in 0 until listModel.size()) {
            val listItem = listModel.getElementAt(i)
            when {
                item is AdbCommand && listItem is SettingsListItem.CommandItem && listItem.config === item -> {
                    itemList.selectedIndex = i
                    return
                }
                item is AdbCommandGroup && listItem is SettingsListItem.GroupHeader && listItem.group === item -> {
                    itemList.selectedIndex = i
                    return
                }
            }
        }
    }

    private fun selectGroupHeader(group: AdbCommandGroup) {
        for (i in 0 until listModel.size()) {
            val item = listModel.getElementAt(i)
            if (item is SettingsListItem.GroupHeader && item.group.name == group.name) {
                itemList.selectedIndex = i
                return
            }
        }
    }

    // endregion

    // region Reset

    private fun resetToDefaults() {
        val result = Messages.showYesNoDialog(
            PluginBundle.message("adbCommandResetConfirmation"),
            PluginBundle.message("adbCommandResetTitle"),
            Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            items = AdbCommandsService.defaultItems.toMutableList()
            refreshList()
        }
    }

    // endregion

    // region Export / Import

    private fun exportCommands(project: Project?) {
        if (items.isEmpty()) {
            Messages.showInfoMessage(
                PluginBundle.message("adbCommandExportNoCommands"),
                PluginBundle.message("adbCommandExportTitle")
            )
            return
        }

        val exportData = AdbCommandsExportFile(
            version = 1,
            commands = commandListItemsToExportItems(items)
        )

        val descriptor = FileSaverDescriptor(
            PluginBundle.message("adbCommandExportTitle"),
            PluginBundle.message("adbCommandExportDescription"),
            "json"
        )

        val saveFileDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project)
        val wrapper = saveFileDialog.save(null as VirtualFile?, "adb-commands.json")

        if (wrapper != null) {
            val gson = GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ExportItem::class.java, ExportItemAdapter())
                .create()
            val json = gson.toJson(exportData)
            val exportedFile = wrapper.file
            exportedFile.writeText(json)

            ApplicationManager.getApplication().invokeLater {
                val openProject = ProjectManager.getInstance().openProjects.firstOrNull()
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("adb_wifi_notification_group")
                    .createNotification(
                        PluginBundle.message("adbCommandExportTitle"),
                        PluginBundle.message("adbCommandExportSuccess"),
                        NotificationType.INFORMATION
                    )
                notification.addAction(object : NotificationAction(PluginBundle.message("adbCommandExportOpenFile")) {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        RevealFileAction.openFile(exportedFile)
                    }
                })
                Notifications.Bus.notify(notification, openProject)
            }
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
                val gson = GsonBuilder()
                    .registerTypeAdapter(ExportItem::class.java, ExportItemAdapter())
                    .create()
                val exportFile = gson.fromJson(json, AdbCommandsExportFile::class.java)

                if (exportFile?.commands.isNullOrEmpty()) {
                    Messages.showWarningDialog(
                        PluginBundle.message("adbCommandImportEmpty"),
                        PluginBundle.message("adbCommandImportTitle")
                    )
                    return@chooseFile
                }

                val importedItems = exportItemsToCommandListItems(exportFile.commands)
                showImportModeDialog(importedItems)
            } catch (e: Exception) {
                Messages.showErrorDialog(
                    PluginBundle.message("adbCommandImportError", e.message ?: "Unknown error"),
                    PluginBundle.message("adbCommandImportTitle")
                )
            }
        }
    }

    private fun showImportModeDialog(importedItems: List<CommandListItem>) {
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
            0 -> {
                items = importedItems.toMutableList()
                refreshList()
            }
            1 -> {
                items.addAll(importedItems)
                refreshList()
            }
        }
    }

    // endregion

    // region Helpers

    private fun getSelectedItem(): SettingsListItem? {
        val index = itemList.selectedIndex
        if (index < 0 || index >= listModel.size()) return null
        return listModel.getElementAt(index)
    }

    private fun replaceCommandInGroup(group: AdbCommandGroup, oldCmd: AdbCommand, newCmd: AdbCommand) {
        val groupIdx = items.indexOf(group)
        if (groupIdx >= 0) {
            val newCommands = group.commands.map { if (it === oldCmd) newCmd else it }
            items[groupIdx] = group.copy(commands = newCommands)
        }
    }

    // endregion

    // region Cell Renderer

    private inner class SettingsListCellRenderer : ListCellRenderer<SettingsListItem> {
        // Group header components
        private val groupPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply { isOpaque = true }
        private val groupIconLabel = JLabel().apply { border = JBUI.Borders.empty(0, 4) }
        private val groupTextLabel = JLabel()

        // Command components
        private val commandPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply { isOpaque = true }
        private val commandCheckBox = JCheckBox().apply { isOpaque = false }
        private val commandIconLabel = JLabel().apply { border = JBUI.Borders.empty(0, 4) }
        private val commandTextLabel = JLabel()

        init {
            groupPanel.add(groupIconLabel)
            groupPanel.add(groupTextLabel)

            commandPanel.add(commandCheckBox)
            commandPanel.add(commandIconLabel)
            commandPanel.add(commandTextLabel)
        }

        override fun getListCellRendererComponent(
            list: JList<out SettingsListItem>,
            value: SettingsListItem?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            return when (value) {
                is SettingsListItem.GroupHeader -> renderGroupHeader(list, value, isSelected)
                is SettingsListItem.CommandItem -> renderCommandItem(list, value, isSelected)
                null -> JLabel()
            }
        }

        private fun renderGroupHeader(
            list: JList<out SettingsListItem>,
            item: SettingsListItem.GroupHeader,
            isSelected: Boolean
        ): Component {
            val bg = if (isSelected) list.selectionBackground else list.background
            val fg = if (isSelected) list.selectionForeground else list.foreground

            groupPanel.background = bg
            groupTextLabel.foreground = fg
            groupTextLabel.font = list.font.deriveFont(Font.BOLD)
            groupTextLabel.text = item.group.name
            groupIconLabel.icon = CommandIconsProvider.getIcon(
                item.group.iconId,
                IconColor.fromString(item.group.iconColor)
            )
            groupPanel.border = JBUI.Borders.empty(2, 4)

            return groupPanel
        }

        private fun renderCommandItem(
            list: JList<out SettingsListItem>,
            item: SettingsListItem.CommandItem,
            isSelected: Boolean
        ): Component {
            val bg = if (isSelected) list.selectionBackground else list.background
            val fg = if (isSelected) list.selectionForeground else list.foreground

            commandPanel.background = bg
            commandCheckBox.background = bg
            commandCheckBox.isSelected = item.config.isEnabled
            commandTextLabel.foreground = fg
            commandTextLabel.font = list.font
            commandTextLabel.text = item.config.name
            commandIconLabel.icon = CommandIconsProvider.getIcon(
                item.config.iconId,
                IconColor.fromString(item.config.iconColor)
            )

            // Indent commands that belong to a group
            val leftPad = if (item.group != null) INDENT_WIDTH else 4
            commandPanel.border = JBUI.Borders.emptyLeft(leftPad)

            return commandPanel
        }
    }

    // endregion

    private companion object {
        private const val CHECKBOX_WIDTH = 24
        private const val INDENT_WIDTH = 24
    }
}

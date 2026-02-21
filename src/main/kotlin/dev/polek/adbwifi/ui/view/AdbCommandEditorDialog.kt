package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.CommandIcon
import dev.polek.adbwifi.model.CommandIconsProvider
import dev.polek.adbwifi.model.CommandListItem.AdbCommand
import dev.polek.adbwifi.model.CommandListItem.AdbCommandGroup
import dev.polek.adbwifi.model.IconColor
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.CompoundBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AdbCommandEditorDialog(
    existingCommand: AdbCommand? = null,
    availableGroups: List<AdbCommandGroup> = emptyList(),
    existingGroup: AdbCommandGroup? = null
) : DialogWrapper(true) {

    private val nameField = JBTextField()
    private val groupComboBox = ComboBox<GroupItem>()
    private val commandArea = JBTextArea(4, 40)
    private var selectedIcon: CommandIcon? = null
    private var selectedColor: IconColor = IconColor.PRIMARY
    private val iconButton = createIconButton()
    private val confirmationCheckbox = JBCheckBox(PluginBundle.message("adbCommandEditorConfirmationLabel"))
    private val previewArea = JBTextArea(3, 40)

    val commandName: String get() = nameField.text.trim()
    val command: String get() = commandArea.text.trim()
    val iconId: String get() = selectedIcon?.id ?: ""
    val iconColor: String get() = selectedColor.colorName
    val requiresConfirmation: Boolean get() = confirmationCheckbox.isSelected
    val selectedGroup: AdbCommandGroup?
        get() = (groupComboBox.selectedItem as? GroupItem)?.group

    private data class GroupItem(val group: AdbCommandGroup?, val displayName: String) {
        override fun toString() = displayName
    }

    init {
        title = if (existingCommand != null) {
            PluginBundle.message("adbCommandEditorTitleEdit")
        } else {
            PluginBundle.message("adbCommandEditorTitleAdd")
        }

        // Populate group combo box
        groupComboBox.addItem(GroupItem(null, PluginBundle.message("adbCommandEditorGroupNone")))
        availableGroups.sortedBy { it.name }.forEach { group ->
            groupComboBox.addItem(GroupItem(group, group.name))
        }

        init()

        existingCommand?.let { config ->
            nameField.text = config.name
            commandArea.text = config.command
            confirmationCheckbox.isSelected = config.requiresConfirmation
            if (config.iconId.isNotEmpty()) {
                selectedIcon = CommandIconsProvider.getIconById(config.iconId)
                selectedColor = IconColor.fromString(config.iconColor)
                updateIconButton()
            }
            // Pre-select existing group
            existingGroup?.let { g ->
                for (i in 0 until groupComboBox.itemCount) {
                    if (groupComboBox.getItemAt(i).group === g) {
                        groupComboBox.selectedIndex = i
                        break
                    }
                }
            }
        }

        updatePreview()
    }

    override fun createCenterPanel(): JComponent {
        commandArea.lineWrap = true
        commandArea.wrapStyleWord = true
        commandArea.margin = JBUI.insets(8)
        commandArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updatePreview()
            override fun removeUpdate(e: DocumentEvent) = updatePreview()
            override fun changedUpdate(e: DocumentEvent) = updatePreview()
        })

        val commandScrollPane = JBScrollPane(commandArea)
        commandScrollPane.preferredSize = Dimension(600, 150)

        val hintLabel = JBLabel(PluginBundle.message("adbCommandEditorCommandHint"))
        hintLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

        previewArea.isEditable = false
        previewArea.lineWrap = true
        previewArea.wrapStyleWord = true
        previewArea.margin = JBUI.insets(4)
        previewArea.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        previewArea.font = JBUI.Fonts.smallFont()
        val previewScrollPane = JBScrollPane(previewArea)
        previewScrollPane.preferredSize = Dimension(600, 100)

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(PluginBundle.message("adbCommandEditorNameLabel"), nameField)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorGroupLabel"), groupComboBox)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorIconLabel"), iconButton)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorCommandLabel"), commandScrollPane)
            .addComponentToRightColumn(hintLabel)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorPreviewLabel"), previewScrollPane)
            .addComponent(confirmationCheckbox)
            .panel
    }

    private fun updatePreview() {
        val commands = commandArea.text.split("\n").filter { it.isNotBlank() }
        val preview = commands.joinToString("\n") { cmd ->
            var result = cmd.trim().replace("{package}", "<app package>")
            FILE_REGEX.findAll(result).forEach { match ->
                val extension = match.groupValues[1].trim().takeIf { it.isNotEmpty() }
                val name = match.groupValues[2].trim().takeIf { it.isNotEmpty() }
                val placeholder = when {
                    name != null -> "<$name>"
                    extension != null -> "<$extension file>"
                    else -> "<file>"
                }
                result = result.replace(match.value, placeholder)
            }
            PARAM_DIR_REGEX.findAll(result).forEach { match ->
                val type = match.groupValues[1]
                val name = match.groupValues[2].trim().takeIf { it.isNotEmpty() }
                val placeholder = when {
                    name != null -> "<$name>"
                    type == "dir" -> "<directory>"
                    else -> "<user input>"
                }
                result = result.replace(match.value, placeholder)
            }
            "adb -s <device ID> $result"
        }
        previewArea.text = preview.ifEmpty { "adb -s <device ID> <command>" }
    }

    private companion object {
        private val FILE_REGEX = """\{file(\s+\.[a-zA-Z0-9]+)?(\s+[^}]+)?}""".toRegex()
        private val PARAM_DIR_REGEX = """\{(param|dir)(\s+[^}]+)?}""".toRegex()
    }

    override fun doValidate(): ValidationInfo? {
        if (commandName.isBlank()) {
            return ValidationInfo(
                PluginBundle.message("adbCommandEditorNameRequired"),
                nameField
            )
        }
        if (command.isBlank()) {
            return ValidationInfo(
                PluginBundle.message("adbCommandEditorCommandRequired"),
                commandArea
            )
        }
        return null
    }

    private fun createIconButton(): JPanel {
        val panel = JPanel(BorderLayout(JBUI.scale(8), 0))
        panel.isOpaque = true
        panel.background = JBColor.background()
        panel.border = CompoundBorder(
            JBUI.Borders.customLine(JBColor.border()),
            JBUI.Borders.empty(4, 8)
        )
        panel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        panel.preferredSize = Dimension(0, JBUI.scale(28))

        panel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                openIconPicker()
            }

            override fun mouseEntered(e: MouseEvent) {
                panel.background = JBUI.CurrentTheme.List.Hover.background(true)
            }

            override fun mouseExited(e: MouseEvent) {
                panel.background = JBColor.background()
            }
        })

        // Add initial icon content
        addIconContent(panel)
        return panel
    }

    private fun updateIconButton() {
        iconButton.removeAll()
        addIconContent(iconButton)
        iconButton.revalidate()
        iconButton.repaint()
    }

    private fun addIconContent(panel: JPanel) {
        val icon = selectedIcon
        if (icon != null) {
            val coloredIcon = CommandIconsProvider.getIcon(icon.id, selectedColor)
            if (coloredIcon != null) {
                panel.add(JBLabel(coloredIcon), BorderLayout.WEST)
            }
            panel.add(JBLabel(icon.displayName), BorderLayout.CENTER)

            val clearButton = object : JBLabel(com.intellij.icons.AllIcons.Actions.Close) {
                private var hovered = false
                private var pressed = false

                init {
                    toolTipText = PluginBundle.message("iconPickerClearIcon")
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    isOpaque = false
                    border = JBUI.Borders.empty(2)
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent) {
                            e.consume()
                            selectedIcon = null
                            updateIconButton()
                        }
                        override fun mouseEntered(e: MouseEvent) { hovered = true; repaint() }
                        override fun mouseExited(e: MouseEvent) { hovered = false; pressed = false; repaint() }
                        override fun mousePressed(e: MouseEvent) { pressed = true; repaint() }
                        override fun mouseReleased(e: MouseEvent) { pressed = false; repaint() }
                    })
                }

                override fun paintComponent(g: java.awt.Graphics) {
                    if (hovered || pressed) {
                        val g2 = g as java.awt.Graphics2D
                        g2.setRenderingHint(
                            java.awt.RenderingHints.KEY_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                        )
                        g2.color = if (pressed) {
                            JBUI.CurrentTheme.ActionButton.pressedBackground()
                        } else {
                            JBUI.CurrentTheme.ActionButton.hoverBackground()
                        }
                        g2.fillRoundRect(0, 0, width, height, JBUI.scale(6), JBUI.scale(6))
                    }
                    super.paintComponent(g)
                }
            }
            panel.add(clearButton, BorderLayout.EAST)
        } else {
            panel.add(JBLabel(PluginBundle.message("iconPickerNoIcon")), BorderLayout.CENTER)
        }
    }

    private fun openIconPicker() {
        val dialog = IconPickerDialog(selectedIcon?.id, selectedColor)
        if (dialog.showAndGet()) {
            selectedIcon = dialog.selectedIcon
            selectedColor = dialog.selectedColor
            updateIconButton()
        }
    }
}

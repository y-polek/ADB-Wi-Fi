package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ActionIcon
import dev.polek.adbwifi.model.ActionIconsProvider
import dev.polek.adbwifi.model.AdbCommandConfig
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
    existingCommand: AdbCommandConfig? = null
) : DialogWrapper(true) {

    private val nameField = JBTextField()
    private val commandArea = JBTextArea(4, 40)
    private var selectedIcon: ActionIcon? = null
    private val iconButton = createIconButton()
    private val confirmationCheckbox = JBCheckBox(PluginBundle.message("adbCommandEditorConfirmationLabel"))
    private val previewArea = JBTextArea(3, 40)

    val commandName: String get() = nameField.text.trim()
    val command: String get() = commandArea.text.trim()
    val iconId: String get() = selectedIcon?.id ?: ""
    val requiresConfirmation: Boolean get() = confirmationCheckbox.isSelected

    init {
        title = if (existingCommand != null) {
            PluginBundle.message("adbCommandEditorTitleEdit")
        } else {
            PluginBundle.message("adbCommandEditorTitleAdd")
        }
        init()

        existingCommand?.let { config ->
            nameField.text = config.name
            commandArea.text = config.command
            confirmationCheckbox.isSelected = config.requiresConfirmation
            if (config.iconId.isNotEmpty()) {
                selectedIcon = ActionIconsProvider.getIconById(config.iconId)
                updateIconButton()
            }
        }

        updatePreview()
    }

    override fun createCenterPanel(): JComponent {
        commandArea.lineWrap = true
        commandArea.wrapStyleWord = true
        commandArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updatePreview()
            override fun removeUpdate(e: DocumentEvent) = updatePreview()
            override fun changedUpdate(e: DocumentEvent) = updatePreview()
        })

        val commandScrollPane = JBScrollPane(commandArea)
        commandScrollPane.preferredSize = Dimension(400, 100)

        val hintLabel = JBLabel(PluginBundle.message("adbCommandEditorCommandHint"))
        hintLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

        previewArea.isEditable = false
        previewArea.lineWrap = true
        previewArea.wrapStyleWord = true
        previewArea.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
        previewArea.font = JBUI.Fonts.smallFont()
        val previewScrollPane = JBScrollPane(previewArea)
        previewScrollPane.preferredSize = Dimension(400, 60)

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(PluginBundle.message("adbCommandEditorNameLabel"), nameField)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorCommandLabel"), commandScrollPane)
            .addComponentToRightColumn(hintLabel)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorPreviewLabel"), previewScrollPane)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorIconLabel"), iconButton)
            .addComponent(confirmationCheckbox)
            .panel
    }

    private fun updatePreview() {
        val commands = commandArea.text.split("\n").filter { it.isNotBlank() }
        val preview = commands.joinToString("\n") { cmd ->
            "adb -s <device ID> ${cmd.trim().replace("{package}", "<app package name>")}"
        }
        previewArea.text = preview.ifEmpty { "adb -s <device ID> <command>" }
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
        panel.background = JBUI.CurrentTheme.List.Hover.background(false)
        panel.border = CompoundBorder(
            JBUI.Borders.customLine(JBUI.CurrentTheme.Focus.defaultButtonColor()),
            JBUI.Borders.empty(4, 8)
        )
        panel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        panel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                openIconPicker()
            }

            override fun mouseEntered(e: MouseEvent) {
                panel.background = JBUI.CurrentTheme.List.Hover.background(true)
            }

            override fun mouseExited(e: MouseEvent) {
                panel.background = JBUI.CurrentTheme.List.Hover.background(false)
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
            panel.add(JBLabel(icon.icon), BorderLayout.WEST)
            panel.add(JBLabel(icon.displayName), BorderLayout.CENTER)
        } else {
            panel.add(JBLabel(PluginBundle.message("iconPickerNoIcon")), BorderLayout.CENTER)
        }
    }

    private fun openIconPicker() {
        val dialog = IconPickerDialog(selectedIcon?.id)
        if (dialog.showAndGet()) {
            selectedIcon = dialog.selectedIcon
            updateIconButton()
        }
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.CommandIcon
import java.awt.Dimension
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class AdbCommandEditorDialog(
    existingCommand: AdbCommandConfig? = null
) : DialogWrapper(true) {

    private val nameField = JBTextField()
    private val commandArea = JBTextArea(4, 40)
    private val iconComboBox = ComboBox(DefaultComboBoxModel(CommandIcon.entries.toTypedArray()))

    val commandName: String get() = nameField.text.trim()
    val command: String get() = commandArea.text.trim()
    val iconId: String get() = (iconComboBox.selectedItem as CommandIcon).id

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
            CommandIcon.fromId(config.iconId)?.let { icon ->
                iconComboBox.selectedItem = icon
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        commandArea.lineWrap = true
        commandArea.wrapStyleWord = true

        iconComboBox.renderer = IconComboBoxRenderer()

        val commandScrollPane = JBScrollPane(commandArea)
        commandScrollPane.preferredSize = Dimension(400, 100)

        val hintLabel = JBLabel(PluginBundle.message("adbCommandEditorCommandHint"))
        hintLabel.foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND

        return FormBuilder.createFormBuilder()
            .addLabeledComponent(PluginBundle.message("adbCommandEditorNameLabel"), nameField)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorCommandLabel"), commandScrollPane)
            .addComponentToRightColumn(hintLabel)
            .addLabeledComponent(PluginBundle.message("adbCommandEditorIconLabel"), iconComboBox)
            .panel
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

    private class IconComboBoxRenderer : ListCellRenderer<CommandIcon> {
        private val label = JBLabel()

        override fun getListCellRendererComponent(
            list: JList<out CommandIcon>,
            value: CommandIcon?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): JComponent {
            value?.let {
                label.icon = it.icon
                label.text = it.displayName
            }
            label.isOpaque = true
            if (isSelected) {
                label.background = list.selectionBackground
                label.foreground = list.selectionForeground
            } else {
                label.background = list.background
                label.foreground = list.foreground
            }
            return label
        }
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.DeviceNamesService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.makeMonospaced
import java.awt.GridBagConstraints
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class RenameDeviceDialogWrapper(
    private val device: DeviceViewModel
) : DialogWrapper(true) {

    private val deviceNamesService = service<DeviceNamesService>()

    private lateinit var textField: JBTextField
    private lateinit var renameButton: JButton
    private lateinit var resetNameCheckbox: JBCheckBox

    init {
        init()
        isResizable = false
        title = PluginBundle.message("renameDeviceTitle")
    }

    override fun createCenterPanel(): JComponent {
        val panel = GridBagLayoutPanel()

        textField = JBTextField(device.titleText, 25)
        textField.selectionStart = 0
        textField.selectionEnd = textField.text.length
        textField.makeMonospaced()
        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateUi()
            override fun removeUpdate(e: DocumentEvent) = updateUi()
            override fun changedUpdate(e: DocumentEvent) = updateUi()
        })
        textField.addActionListener {
            renameDeviceAndDismiss()
        }
        panel.add(
            textField,
            GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insetsRight(10)
            }
        )

        renameButton = JButton(PluginBundle.message("renameButton"))
        renameButton.addActionListener {
            renameDeviceAndDismiss()
        }
        panel.add(
            renameButton,
            GridBagConstraints().apply {
                gridx = 3
            }
        )

        resetNameCheckbox = JBCheckBox(PluginBundle.message("resetToOriginalName", device.device.name))
        resetNameCheckbox.addActionListener {
            updateUi()
        }
        panel.add(
            resetNameCheckbox,
            GridBagConstraints().apply {
                gridy = 1
                gridwidth = 4
                insets = JBUI.insetsTop(5)
            }
        )

        updateUi()

        return panel
    }

    override fun createActions(): Array<Action> = emptyArray()

    private fun renameDeviceAndDismiss() {
        if (resetNameCheckbox.isSelected) {
            deviceNamesService.removeName(device.serialNumber)
        } else {
            val newName = textField.text.trim()
            deviceNamesService.setName(device.serialNumber, newName)
        }
        dispose()
    }

    private fun updateUi() {
        renameButton.isEnabled = textField.text.isNotBlank() || resetNameCheckbox.isSelected
        renameButton.text = if (resetNameCheckbox.isSelected) {
            PluginBundle.message("saveButton")
        } else {
            PluginBundle.message("renameButton")
        }
        textField.isEnabled = !resetNameCheckbox.isSelected
    }
}

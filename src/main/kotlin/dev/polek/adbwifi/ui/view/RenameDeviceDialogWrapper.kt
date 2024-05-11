package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
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
            override fun insertUpdate(e: DocumentEvent) = updateRenameButton()
            override fun removeUpdate(e: DocumentEvent) = updateRenameButton()
            override fun changedUpdate(e: DocumentEvent) = updateRenameButton()
        })
        textField.addActionListener {
            renameDeviceAndDismiss()
        }
        panel.add(
            textField,
            GridBagConstraints().apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insets(0, 5)
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

        updateRenameButton()

        return panel
    }

    override fun createActions(): Array<Action> = emptyArray()

    private fun renameDeviceAndDismiss() {
        val newName = textField.text.trim()
        deviceNamesService.setName(device.serialNumber, newName)
        dispose()
    }

    private fun updateRenameButton() {
        renameButton.isEnabled = textField.text.isNotBlank()
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.DeviceNamesService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.makeMonospaced
import dev.polek.adbwifi.utils.onTextChanged
import java.awt.GridBagConstraints
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent

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
        textField.onTextChanged(::updateUi)
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

        val resetLink = HyperlinkLabel(PluginBundle.message("resetToOriginalName", device.device.name))
        resetLink.addHyperlinkListener {
            textField.text = device.device.name
            textField.selectAll()
            textField.requestFocus()
            updateUi()
        }
        panel.add(
            resetLink,
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
        val newName = textField.text.trim()
        if (newName == device.device.name) {
            deviceNamesService.removeName(device.serialNumber)
        } else {
            deviceNamesService.setName(device.serialNumber, newName)
        }
        dispose()
    }

    private fun updateUi() {
        renameButton.isEnabled = textField.text.isNotBlank()
    }
}

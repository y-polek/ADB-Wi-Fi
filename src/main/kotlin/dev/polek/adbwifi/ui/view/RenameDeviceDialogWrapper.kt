package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.DeviceNamesService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.makeMonospaced
import dev.polek.adbwifi.utils.onTextChanged
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class RenameDeviceDialogWrapper(
    private val device: DeviceViewModel
) : DialogWrapper(true) {

    private val deviceNamesService = service<DeviceNamesService>()

    private lateinit var textField: JBTextField

    init {
        init()
        isResizable = false
        title = PluginBundle.message("renameDeviceTitle")
        setOKButtonText(PluginBundle.message("renameButton"))
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(0, 0, 8, 0)

        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        // Text field
        textField = JBTextField(device.titleText, 30)
        textField.selectionStart = 0
        textField.selectionEnd = textField.text.length
        textField.makeMonospaced()
        textField.onTextChanged(::updateUi)
        textField.alignmentX = JComponent.LEFT_ALIGNMENT
        contentPanel.add(textField)

        contentPanel.add(Box.createVerticalStrut(4))

        // Reset link
        val resetLink = HyperlinkLabel(PluginBundle.message("resetToOriginalName", device.device.name))
        resetLink.addHyperlinkListener {
            textField.text = device.device.name
            textField.selectAll()
            textField.requestFocus()
            updateUi()
        }
        resetLink.alignmentX = JComponent.LEFT_ALIGNMENT
        contentPanel.add(resetLink)

        panel.add(contentPanel, BorderLayout.CENTER)

        updateUi()

        return panel
    }

    override fun doOKAction() {
        renameDevice()
        super.doOKAction()
    }

    private fun renameDevice() {
        val newName = textField.text.trim()
        val isOriginalName = newName == device.device.name

        if (isOriginalName) {
            // Reset to original name - remove all custom names (unique ID and legacy serial-based)
            deviceNamesService.removeAllNames(device.serialNumber, device.uniqueId)
        } else {
            // Set custom name for this specific device
            deviceNamesService.setNameByUniqueId(device.uniqueId, newName)
        }
    }

    private fun updateUi() {
        isOKActionEnabled = textField.text.isNotBlank()
    }
}

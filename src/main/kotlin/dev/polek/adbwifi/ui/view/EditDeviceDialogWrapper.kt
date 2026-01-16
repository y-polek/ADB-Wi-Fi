package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.DeviceNamesService
import dev.polek.adbwifi.services.PinDeviceService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.makeMonospaced
import dev.polek.adbwifi.utils.onTextChanged
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class EditDeviceDialogWrapper(
    private val device: DeviceViewModel
) : DialogWrapper(true) {

    private val deviceNamesService = service<DeviceNamesService>()
    private val pinDeviceService = service<PinDeviceService>()

    private lateinit var nameField: JBTextField
    private lateinit var ipField: JBTextField
    private lateinit var portField: JBTextField

    private val originalAddress = device.address ?: ""
    private val originalPort = device.device.port

    init {
        init()
        isResizable = false
        title = PluginBundle.message("editDeviceTitle")
        setOKButtonText(PluginBundle.message("saveButton"))
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.border = JBUI.Borders.emptyBottom(8)

        val gbc = GridBagConstraints()
        gbc.insets = JBUI.insets(4, 0, 4, 8)
        gbc.anchor = GridBagConstraints.WEST

        // Name label
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.fill = GridBagConstraints.NONE
        panel.add(JBLabel(PluginBundle.message("editDeviceNameLabel")), gbc)

        // Name field
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        nameField = JBTextField(device.titleText, 25)
        nameField.makeMonospaced()
        nameField.onTextChanged(::updateUi)
        panel.add(nameField, gbc)

        // Reset link
        gbc.gridx = 1
        gbc.gridy = 1
        gbc.insets = JBUI.insets(0, 0, 8, 8)
        val resetLink = HyperlinkLabel(PluginBundle.message("resetToOriginalName", device.device.name))
        resetLink.addHyperlinkListener {
            nameField.text = device.device.name
            nameField.selectAll()
            nameField.requestFocus()
            updateUi()
        }
        panel.add(resetLink, gbc)

        // IP Address label
        gbc.gridx = 0
        gbc.gridy = 2
        gbc.insets = JBUI.insets(4, 0, 4, 8)
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        panel.add(JBLabel(PluginBundle.message("editDeviceIpLabel")), gbc)

        // IP Address field
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        ipField = JBTextField(device.address ?: "", 25)
        ipField.makeMonospaced()
        ipField.onTextChanged(::updateUi)
        panel.add(ipField, gbc)

        // Port label
        gbc.gridx = 0
        gbc.gridy = 3
        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        panel.add(JBLabel(PluginBundle.message("editDevicePortLabel")), gbc)

        // Port field
        gbc.gridx = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        portField = JBTextField(device.device.port.toString(), 25)
        portField.makeMonospaced()
        portField.onTextChanged(::updateUi)
        panel.add(portField, gbc)

        updateUi()

        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = nameField

    override fun doOKAction() {
        saveDevice()
        super.doOKAction()
    }

    private fun saveDevice() {
        val newName = nameField.text.trim()
        val newIp = ipField.text.trim()
        val newPort = portField.text.trim().toIntOrNull() ?: originalPort

        // Update name
        val isOriginalName = newName == device.device.name
        if (isOriginalName) {
            deviceNamesService.removeAllNames(device.serialNumber, device.uniqueId)
        } else {
            deviceNamesService.setNameByUniqueId(device.uniqueId, newName)
        }

        // Update IP address and port if changed
        if (newIp != originalAddress || newPort != originalPort) {
            pinDeviceService.updatePinnedDevice(
                serialNumber = device.serialNumber,
                oldAddress = originalAddress,
                newAddress = newIp,
                newPort = newPort
            )
        }
    }

    private fun updateUi() {
        val nameValid = nameField.text.isNotBlank()
        val ipValid = ipField.text.isNotBlank() && isValidIpAddress(ipField.text.trim())
        val portValid = portField.text.isNotBlank() && isValidPort(portField.text.trim())

        isOKActionEnabled = nameValid && ipValid && portValid
    }

    private fun isValidIpAddress(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            val num = part.toIntOrNull() ?: return false
            num in 0..255
        }
    }

    private fun isValidPort(port: String): Boolean {
        val num = port.toIntOrNull() ?: return false
        return num in 1..65535
    }
}

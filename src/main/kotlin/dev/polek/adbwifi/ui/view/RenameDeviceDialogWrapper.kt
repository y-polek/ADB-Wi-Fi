package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.PinDeviceService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.appCoroutineScope
import dev.polek.adbwifi.utils.makeMonospaced
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.GridBagConstraints
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class RenameDeviceDialogWrapper(private val deviceViewModel: DeviceViewModel) : DialogWrapper(true) {

    private lateinit var customNameLabel: JBLabel
    private lateinit var customNameField: JBTextField
    private lateinit var renameButton: JButton

    private var renameDeviceJob: Job? = null

    init {
        init()
        isResizable = false
        title = PluginBundle.message("name")
    }

    override fun createCenterPanel(): JComponent {
        val panel = GridBagLayoutPanel()

        customNameLabel = JBLabel(PluginBundle.message("renameDeviceLabel"))
        panel.add(
            customNameLabel,
            GridBagConstraints().apply {
                gridx = 0
            }
        )

        customNameField = JBTextField(25)
        customNameField.makeMonospaced()
        customNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateRenameButton()
            override fun removeUpdate(e: DocumentEvent) = updateRenameButton()
            override fun changedUpdate(e: DocumentEvent) = updateRenameButton()
        })
        panel.add(
            customNameField,
            GridBagConstraints().apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insets(0, 5)
            }
        )
        renameButton = JButton(PluginBundle.message("renameButton"))
        renameButton.addActionListener {
            renameDevice()
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

    private fun renameDevice() {
        val pinService = service<PinDeviceService>()
        renameDeviceJob = appCoroutineScope.launch(Dispatchers.IO) {
            pinService.removePreviouslyConnectedDevice(deviceViewModel.device)
            val customName = customNameField.text.trim()
            withContext(Dispatchers.Main) {
                customNameField.text = customName
                customNameField.requestFocusInWindow()
                deviceViewModel.device.customName = customName
                pinService.addPreviouslyConnectedDevice(deviceViewModel.device)
            }
        }
        dispose()
    }

    private fun updateRenameButton() {
        renameButton.isEnabled = customNameField.text.isNotBlank()
    }
}

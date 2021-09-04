package dev.polek.adbwifi.ui.view

import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.utils.ADB_DEFAULT_PORT
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.MaxLengthNumberDocument
import dev.polek.adbwifi.utils.makeMonospaced
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ConnectDeviceDialogWrapper : DialogWrapper(true) {

    private lateinit var ipLabel: JBLabel
    private lateinit var ipTextField: JBTextField
    private lateinit var portTextField: JBTextField
    private lateinit var connectButton: JButton
    private lateinit var outputLabel: JBLabel

    private var connectJob: Job? = null

    init {
        init()
        setResizable(false)
        title = PluginBundle.message("name")
    }

    override fun createCenterPanel(): JComponent {
        val panel = GridBagLayoutPanel()

        ipLabel = JBLabel(PluginBundle.message("deviceIpLabel"))
        panel.add(
            ipLabel,
            GridBagConstraints().apply {
                gridx = 0
            }
        )

        ipTextField = JBTextField(25)
        ipTextField.makeMonospaced()
        ipTextField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateConnectButton()
            override fun removeUpdate(e: DocumentEvent) = updateConnectButton()
            override fun changedUpdate(e: DocumentEvent) = updateConnectButton()
        })
        ipTextField.addActionListener {
            connectDevice()
        }
        panel.add(
            ipTextField,
            GridBagConstraints().apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = Insets(0, 5, 0, 5)
            }
        )

        portTextField = JBTextField(7)
        portTextField.document = MaxLengthNumberDocument(5)
        portTextField.text = ADB_DEFAULT_PORT.toString()
        portTextField.makeMonospaced()
        portTextField.addActionListener {
            connectDevice()
        }
        panel.add(
            portTextField,
            GridBagConstraints().apply {
                gridx = 2
                insets = Insets(0, 0, 0, 5)
            }
        )

        connectButton = JButton(PluginBundle.message("connectButton"))
        connectButton.addActionListener {
            connectDevice()
        }
        panel.add(
            connectButton,
            GridBagConstraints().apply {
                gridx = 3
            }
        )

        outputLabel = JBLabel()
        outputLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        outputLabel.foreground = OUTPUT_TEXT_COLOR
        outputLabel.isVisible = false
        panel.add(
            outputLabel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 3
                fill = GridBagConstraints.BOTH
                weighty = 1.0
                insets = Insets(10, 0, 0, 0)
            }
        )

        updateConnectButton()

        return panel
    }

    override fun dispose() {
        connectJob?.cancel()
        connectJob = null
        super.dispose()
    }

    override fun createActions(): Array<Action> = emptyArray()

    private fun connectDevice() {
        showConnectionProgress()
        outputLabel.text = ""

        val ip = ipTextField.text.trim()
        ipTextField.text = ip
        val port = portTextField.text.toIntOrNull() ?: ADB_DEFAULT_PORT

        val adbService = service<AdbService>()
        connectJob = GlobalScope.launch(IO) {
            val output = adbService.connect(ip, port)
            withContext(Main) {
                hideConnectionProgress()
                outputLabel.text = output
                outputLabel.isVisible = true
                ipTextField.requestFocusInWindow()
            }
        }
    }

    private fun showConnectionProgress() {
        ipLabel.isEnabled = false
        ipTextField.isEnabled = false
        connectButton.isEnabled = false
        connectButton.icon = AnimatedIcon.Default()
    }

    private fun hideConnectionProgress() {
        ipLabel.isEnabled = true
        ipTextField.isEnabled = true
        connectButton.isEnabled = true
        connectButton.icon = null
    }

    private fun updateConnectButton() {
        connectButton.isEnabled = ipTextField.text.isNotBlank()
    }

    private companion object {
        private val OUTPUT_TEXT_COLOR = JBColor(0x787878, 0xBBBBBB)
    }
}

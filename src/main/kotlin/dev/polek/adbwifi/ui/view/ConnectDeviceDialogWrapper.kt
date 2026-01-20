package dev.polek.adbwifi.ui.view

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.MaxLengthNumberDocument
import dev.polek.adbwifi.utils.appCoroutineScope
import dev.polek.adbwifi.utils.makeMonospaced
import dev.polek.adbwifi.utils.onTextChanged
import kotlinx.coroutines.*
import java.awt.GridBagConstraints
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent

class ConnectDeviceDialogWrapper : DialogWrapper(true) {

    private val properties = service<PropertiesService>()

    private lateinit var ipLabel: JBLabel
    private lateinit var ipTextField: JBTextField
    private lateinit var portTextField: JBTextField
    private lateinit var connectButton: JButton
    private lateinit var outputLabel: JBLabel
    private lateinit var progressBar: JBLabel

    private var connectJob: Job? = null

    init {
        init()
        isResizable = false
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
        ipTextField.onTextChanged(::updateConnectButton)
        ipTextField.addActionListener {
            connectDevice()
        }
        panel.add(
            ipTextField,
            GridBagConstraints().apply {
                gridx = 1
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
                insets = JBUI.insets(0, 5)
            }
        )

        portTextField = JBTextField(7)
        portTextField.document = MaxLengthNumberDocument(5)
        portTextField.text = properties.adbPort.toString()
        portTextField.makeMonospaced()
        portTextField.addActionListener {
            connectDevice()
        }
        panel.add(
            portTextField,
            GridBagConstraints().apply {
                gridx = 2
                insets = JBUI.insetsRight(5)
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
                insets = JBUI.insetsTop(10)
            }
        )

        progressBar = JBLabel(AnimatedIcon.Default())
        progressBar.isVisible = false
        panel.add(
            progressBar,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                gridwidth = 1
                weighty = 1.0
                anchor = GridBagConstraints.LINE_START
                insets = JBUI.insetsTop(10)
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
        val port = portTextField.text.toIntOrNull() ?: properties.adbPort

        val adbService = service<AdbService>()
        connectJob = appCoroutineScope.launch(Dispatchers.EDT + ModalityState.current().asContextElement()) {
            val output = withContext(Dispatchers.IO) {
                adbService.connect(ip, port)
            }
            hideConnectionProgress()
            outputLabel.text = output
            outputLabel.isVisible = true
            ipTextField.requestFocusInWindow()
        }
    }

    private fun showConnectionProgress() {
        ipLabel.isEnabled = false
        ipTextField.isEnabled = false
        portTextField.isEnabled = false
        connectButton.isEnabled = false
        progressBar.isVisible = true
    }

    private fun hideConnectionProgress() {
        ipLabel.isEnabled = true
        ipTextField.isEnabled = true
        portTextField.isEnabled = true
        connectButton.isEnabled = true
        progressBar.isVisible = false
    }

    private fun updateConnectButton() {
        connectButton.isEnabled = ipTextField.text.isNotBlank()
    }

    private companion object {
        private val OUTPUT_TEXT_COLOR = JBColor(0x787878, 0xBBBBBB)
    }
}

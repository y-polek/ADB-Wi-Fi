package dev.polek.adbwifi.ui

import com.intellij.ide.plugins.newui.InstallButton
import com.intellij.openapi.components.ServiceManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.AdbService
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import javax.swing.JProgressBar

class DevicePanel(device: Device) : JBPanel<DevicePanel>(GridBagLayout()) {

    private val adbService = ServiceManager.getService(AdbService::class.java)

    private val button: JButton

    init {
        background = JBColor.background()

        minimumSize = Dimension(0, LIST_ITEM_HEIGHT)
        maximumSize = Dimension(Int.MAX_VALUE, LIST_ITEM_HEIGHT)
        preferredSize = Dimension(0, LIST_ITEM_HEIGHT)

        val nameLabel = JBLabel(device.name)
        nameLabel.componentStyle = UIUtil.ComponentStyle.LARGE
        nameLabel.makeBold()
        add(nameLabel, GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.PAGE_START
            weightx = 1.0
            insets = Insets(5, 10, 0, 0)
        })

        val addressLabel = JBLabel("Android ${device.androidVersion} (API ${device.apiLevel}) - ${device.address}")
        addressLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        addressLabel.fontColor = UIUtil.FontColor.BRIGHTER
        add(panel(top = addressLabel), GridBagConstraints().apply {
            gridx = 0
            gridy = 2
            gridwidth = 3
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.PAGE_END
            weightx = 1.0
            weighty = 1.0
            insets = Insets(0, 10, 0, 10)
        })

        button = object : InstallButton(device.isConnected) {
            override fun setTextAndSize() {}
        }
        button.text = if (device.isConnected) "Disconnect" else "Connect"
        button.addActionListener {
            showProgressBar()
            if (device.isConnected) {
                adbService.disconnect(device)
            } else {
                adbService.connect(device)
            }
        }
        add(button, GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.PAGE_START
            insets = Insets(10, 10, 0, 10)
        })

        val hoverListener = object : MouseListener {
            override fun mouseEntered(e: MouseEvent) {
                background = HOVER_COLOR
            }

            override fun mouseExited(e: MouseEvent) {
                background = JBColor.background()
            }

            override fun mouseClicked(e: MouseEvent) {}

            override fun mousePressed(e: MouseEvent) {}

            override fun mouseReleased(e: MouseEvent) {}
        }
        addMouseListener(hoverListener)
        button.addMouseListener(hoverListener)
    }

    private fun showProgressBar() {
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true

        val inset = (button.height - progressBar.height) / 2

        remove(button)
        add(progressBar, GridBagConstraints().apply {
            gridx = 2
            gridy = 0
            gridwidth = 1
            gridheight = 1
            anchor = GridBagConstraints.PAGE_START
            insets = Insets(6 + inset, 10, inset, 10)
        })

        revalidate()
        repaint()
    }

    companion object {
        private const val LIST_ITEM_HEIGHT = 70
        private val HOVER_COLOR = JBColor.namedColor(
                "Plugins.lightSelectionBackground",
                JBColor(0xF5F9FF, 0x36393B))
    }
}

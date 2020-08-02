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

class DevicePanel(device: Device) : JBPanel<DevicePanel>(GridBagLayout()) {

    private val adbService = ServiceManager.getService(AdbService::class.java)

    init {
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
            insets = Insets(0, 10, 0, 0)
        })

        val addressLabel = JBLabel(device.address)
        addressLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        addressLabel.fontColor = UIUtil.FontColor.BRIGHTER
        add(panel(top = addressLabel), GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 3
            gridheight = 1
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.PAGE_END
            weightx = 1.0
            weighty = 1.0
            insets = Insets(0, 10, 0, 10)
        })

        val button = object : InstallButton(device.isConnected) {
            override fun setTextAndSize() {}
        }
        button.text = if (device.isConnected) "Disconnect" else "Connect"
        button.addActionListener {
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
            insets = Insets(5, 10, 0, 10)
        })

        background = JBColor.background()

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

    companion object {
        private const val LIST_ITEM_HEIGHT = 60
        private val HOVER_COLOR = JBColor.namedColor(
                "Plugins.lightSelectionBackground",
                JBColor(0xF5F9FF, 0x36393B))
    }
}

package dev.polek.adbwifi.ui

import com.intellij.ide.plugins.newui.InstallButton
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.utils.AbstractMouseListener
import dev.polek.adbwifi.utils.makeBold
import dev.polek.adbwifi.utils.panel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseEvent
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

        val iconLabel = JBLabel()
        iconLabel.icon = when (device.connectionType) {
            Device.ConnectionType.USB -> ICON_USB
            Device.ConnectionType.WIFI -> ICON_WIFI
        }
        add(
            iconLabel,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 1
                gridheight = 2
                fill = GridBagConstraints.VERTICAL
                weighty = 1.0
                insets = Insets(0, 10, 0, 10)
            }
        )

        val nameLabel = JBLabel(device.name)
        nameLabel.componentStyle = UIUtil.ComponentStyle.LARGE
        nameLabel.makeBold()
        add(
            nameLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                gridwidth = 2
                gridheight = 1
                fill = GridBagConstraints.BOTH
                anchor = GridBagConstraints.PAGE_START
                weightx = 1.0
                insets = Insets(5, 0, 0, 0)
            }
        )

        val addressLabel = JBLabel("Android ${device.androidVersion} (API ${device.apiLevel}) - ${device.address}")
        addressLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        addressLabel.fontColor = UIUtil.FontColor.BRIGHTER
        add(
            panel(top = addressLabel),
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 3
                gridheight = 1
                fill = GridBagConstraints.BOTH
                anchor = GridBagConstraints.PAGE_END
                weightx = 1.0
                weighty = 1.0
                insets = Insets(0, 0, 0, 10)
            }
        )

        button = object : InstallButton(device.isConnected) {
            override fun setTextAndSize() {
                /* no-op */
            }
        }
        button.text = if (device.isWifiDevice) "Disconnect" else "Connect"
        button.isEnabled = device.isWifiDevice || !device.isConnected
        button.addActionListener {
            showProgressBar()
            if (device.isConnected) {
                adbService.disconnect(device)
            } else {
                adbService.connect(device)
            }
        }
        add(
            button,
            GridBagConstraints().apply {
                gridx = 3
                gridy = 0
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.PAGE_START
                insets = Insets(10, 10, 0, 10)
            }
        )

        val hoverListener = object : AbstractMouseListener() {
            override fun mouseEntered(e: MouseEvent) {
                background = HOVER_COLOR
            }

            override fun mouseExited(e: MouseEvent) {
                background = JBColor.background()
            }
        }
        addMouseListener(hoverListener)
        button.addMouseListener(hoverListener)
    }

    private fun showProgressBar() {
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true

        val inset = (button.height - progressBar.height) / 2

        remove(button)
        add(
            progressBar,
            GridBagConstraints().apply {
                gridx = 3
                gridy = 0
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.PAGE_START
                insets = Insets(6 + inset, 10, inset, 10)
            }
        )

        revalidate()
        repaint()
    }

    private companion object {
        private const val LIST_ITEM_HEIGHT = 70
        private val HOVER_COLOR = JBColor.namedColor(
            "Plugins.lightSelectionBackground",
            JBColor(0xF5F9FF, 0x36393B)
        )
        private val ICON_USB = IconLoader.getIcon("/icons/usbIcon.svg")
        private val ICON_WIFI = IconLoader.getIcon("/icons/wifiIcon.svg")
    }
}

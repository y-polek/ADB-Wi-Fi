package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.MyBundle
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.model.DeviceViewModel.ButtonType
import dev.polek.adbwifi.utils.AbstractMouseListener
import dev.polek.adbwifi.utils.flowPanel
import dev.polek.adbwifi.utils.makeBold
import dev.polek.adbwifi.utils.panel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JProgressBar

class DevicePanel(device: DeviceViewModel) : JBPanel<DevicePanel>(GridBagLayout()) {

    private val button: JButton

    var listener: Listener? = null

    init {
        background = JBColor.background()

        minimumSize = Dimension(0, LIST_ITEM_HEIGHT)
        maximumSize = Dimension(Int.MAX_VALUE, LIST_ITEM_HEIGHT)
        preferredSize = Dimension(0, LIST_ITEM_HEIGHT)

        val iconLabel = JBLabel(device.icon)
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

        val titleLabel = JBLabel(device.titleText)
        titleLabel.componentStyle = UIUtil.ComponentStyle.LARGE
        titleLabel.makeBold()
        add(
            titleLabel,
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

        val subtitleLabel = JBLabel(device.subtitleText)
        subtitleLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        subtitleLabel.fontColor = UIUtil.FontColor.BRIGHTER
        add(
            panel(top = subtitleLabel),
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

        button = when (device.buttonType) {
            ButtonType.CONNECT -> Button.connectButton()
            ButtonType.CONNECT_DISABLED -> Button.connectButton(false)
            ButtonType.DISCONNECT -> Button.disconnectButton()
        }
        button.addActionListener {
            when (device.buttonType) {
                ButtonType.CONNECT, ButtonType.CONNECT_DISABLED -> listener?.onConnectButtonClicked(device)
                ButtonType.DISCONNECT -> listener?.onDisconnectButtonClicked(device)
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

        if (device.isInProgress) {
            showProgressBar()
        }

        val pinButton = IconButton(ICON_PIN, MyBundle.message("pinDeviceTooltip"))
        pinButton.onClickedListener = {
            listener?.onPinButtonClicked(device)
        }

        val menuButton = IconButton(ICON_MENU)
        menuButton.onClickedListener = { event ->
            openDeviceMenu(device, event)
        }

        val actionsPanel = flowPanel(pinButton, menuButton, hgap = 10)
        actionsPanel.isOpaque = false
        add(
            actionsPanel,
            GridBagConstraints().apply {
                gridx = 3
                gridy = 1
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.LINE_END
                insets = Insets(0, 0, 0, 5)
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
        actionsPanel.addMouseListener(hoverListener)
        pinButton.addMouseListener(hoverListener)
        menuButton.addMouseListener(hoverListener)
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

    private fun openDeviceMenu(device: DeviceViewModel, event: MouseEvent) {
        val menu = JBPopupMenu()

        val copyIdItem = JBMenuItem(MyBundle.message("copyDeviceIdMenuItem"), AllIcons.Actions.Copy)
        copyIdItem.addActionListener {
            listener?.onCopyDeviceIdClicked(device)
        }
        menu.add(copyIdItem)

        val copyAddressItem = JBMenuItem(MyBundle.message("copyIpAddressMenuItem"), AllIcons.Actions.Copy)
        copyAddressItem.addActionListener {
            listener?.onCopyDeviceAddressClicked(device)
        }
        copyAddressItem.isEnabled = device.hasAddress
        menu.add(copyAddressItem)

        menu.show(event.component, event.x, event.y)
    }

    interface Listener {
        fun onConnectButtonClicked(device: DeviceViewModel)
        fun onDisconnectButtonClicked(device: DeviceViewModel)
        fun onPinButtonClicked(device: DeviceViewModel)
        fun onCopyDeviceIdClicked(device: DeviceViewModel)
        fun onCopyDeviceAddressClicked(device: DeviceViewModel)
    }

    private companion object {
        private const val LIST_ITEM_HEIGHT = 70
        private val HOVER_COLOR = JBColor.namedColor(
            "Plugins.lightSelectionBackground",
            JBColor(0xF5F9FF, 0x36393B)
        )
        private val ICON_MENU = IconLoader.getIcon("/icons/menuIcon.svg")
        private val ICON_PIN = IconLoader.getIcon("/icons/pinIcon.svg")
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ActionIconsProvider
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.services.AdbCommandsService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.model.DeviceViewModel.ButtonType
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.flowPanel
import dev.polek.adbwifi.utils.makeBold
import dev.polek.adbwifi.utils.panel
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JProgressBar
import javax.swing.SwingConstants

class DevicePanel(device: DeviceViewModel) : JBPanel<DevicePanel>(GridBagLayout()) {

    var listener: Listener? = null

    private val hoverListener = object : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent) {
            background = HOVER_COLOR
        }

        override fun mouseExited(e: MouseEvent) {
            background = JBColor.background()
        }
    }

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
                insets = JBUI.insets(0, 10)
            }
        )

        val titleLabel = JBLabel(device.titleText)
        titleLabel.componentStyle = UIUtil.ComponentStyle.LARGE
        titleLabel.makeBold()
        titleLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    listener?.onRenameDeviceClicked(device)
                }
            }
        })
        add(
            titleLabel,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                gridwidth = 1
                fill = GridBagConstraints.BOTH
                anchor = GridBagConstraints.PAGE_START
                weightx = 1.0
                insets = JBUI.insetsTop(5)
            }
        )

        if (device.isInProgress) {
            val progressBar = JProgressBar()
            progressBar.isIndeterminate = true
            progressBar.preferredSize = Dimension(100, progressBar.preferredSize.height)
            progressBar.addMouseListener(hoverListener)
            val vInset = (BUTTON_CELL_HEIGHT - progressBar.preferredSize.height) / 2
            add(
                progressBar,
                GridBagConstraints().apply {
                    gridx = 2
                    gridy = 0
                    gridwidth = 2
                    weighty = 1.0
                    insets = JBUI.insets(vInset, 10, vInset, 10)
                }
            )
        } else {
            val button = when (device.buttonType) {
                ButtonType.CONNECT -> Button.connectButton()
                ButtonType.CONNECT_DISABLED -> Button.connectButton(false)
                ButtonType.DISCONNECT -> Button.disconnectButton()
            }
            val vInset = (BUTTON_CELL_HEIGHT - button.preferredSize.height) / 2
            button.addActionListener {
                when (device.buttonType) {
                    ButtonType.CONNECT, ButtonType.CONNECT_DISABLED -> listener?.onConnectButtonClicked(device)
                    ButtonType.DISCONNECT -> listener?.onDisconnectButtonClicked(device)
                }
            }
            button.addMouseListener(hoverListener)
            add(
                button,
                GridBagConstraints().apply {
                    gridx = 2
                    gridy = 0
                    gridwidth = 2
                    weighty = 1.0
                    insets = JBUI.insets(vInset, 10, vInset, 10)
                }
            )
        }

        val actionButtons = arrayListOf<JComponent>()

        if (device.isShareScreenButtonVisible) {
            val shareScreenButton = IconButton(Icons.SHARE_SCREEN, PluginBundle.message("shareScreenTooltip"))
            shareScreenButton.onClickedListener = {
                listener?.onShareScreenClicked(device)
                shareScreenButton.showProgressFor(2000)
            }
            shareScreenButton.addMouseListener(hoverListener)
            actionButtons.add(shareScreenButton)
        }

        if (device.isAdbCommandsButtonVisible) {
            val adbCommandsButton = IconButton(Icons.ADB_COMMANDS, PluginBundle.message("adbCommandsTooltip"))
            adbCommandsButton.onClickedListener = { event ->
                openAdbCommandsMenu(device, event)
            }
            adbCommandsButton.addMouseListener(hoverListener)
            actionButtons.add(adbCommandsButton)
        }

        if (device.isRemoveButtonVisible) {
            val removeButton = IconButton(Icons.DELETE, PluginBundle.message("removeDeviceTooltip"))
            removeButton.onClickedListener = {
                listener?.onRemoveDeviceClicked(device)
            }
            removeButton.addMouseListener(hoverListener)
            actionButtons.add(removeButton)
        }

        val menuButton = IconButton(Icons.MENU)
        menuButton.onClickedListener = { event ->
            openDeviceMenu(device, event)
        }
        menuButton.addMouseListener(hoverListener)
        actionButtons.add(menuButton)

        val actionsPanel = flowPanel(*actionButtons.toTypedArray(), menuButton, hgap = 10)
        actionsPanel.isOpaque = false
        add(
            actionsPanel,
            GridBagConstraints().apply {
                gridx = 3
                gridy = 1
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.LINE_END
            }
        )

        val subtitleLabel = JBLabel(device.subtitleText)
        subtitleLabel.icon = device.subtitleIcon
        subtitleLabel.horizontalTextPosition = SwingConstants.LEFT
        subtitleLabel.componentStyle = UIUtil.ComponentStyle.REGULAR
        subtitleLabel.fontColor = UIUtil.FontColor.BRIGHTER
        add(
            panel(top = subtitleLabel),
            GridBagConstraints().apply {
                gridx = 1
                gridy = 1
                gridwidth = 3
                fill = GridBagConstraints.BOTH
                anchor = GridBagConstraints.PAGE_END
                weightx = 1.0
                weighty = 1.0
                insets = JBUI.insetsRight(actionsPanel.preferredSize.width)
            }
        )

        addMouseListener(hoverListener)
        actionsPanel.addMouseListener(hoverListener)
    }

    private fun openDeviceMenu(device: DeviceViewModel, event: MouseEvent) {
        val menu = JBPopupMenu()

        val renameDeviceItem = JBMenuItem(PluginBundle.message("renameDeviceMenuItem"), AllIcons.Actions.Edit)
        renameDeviceItem.addActionListener {
            listener?.onRenameDeviceClicked(device)
        }
        menu.add(renameDeviceItem)

        val copyIdItem = JBMenuItem(PluginBundle.message("copyDeviceIdMenuItem"), AllIcons.Actions.Copy)
        copyIdItem.addActionListener {
            listener?.onCopyDeviceIdClicked(device)
        }
        menu.add(copyIdItem)

        val copyAddressItem = JBMenuItem(PluginBundle.message("copyIpAddressMenuItem"), AllIcons.Actions.Copy)
        copyAddressItem.addActionListener {
            listener?.onCopyDeviceAddressClicked(device)
        }
        copyAddressItem.isEnabled = device.hasAddress
        menu.add(copyAddressItem)

        menu.show(event.component, event.x, event.y)
    }

    private fun openAdbCommandsMenu(device: DeviceViewModel, event: MouseEvent) {
        val menu = JBPopupMenu()
        val commandsService = service<AdbCommandsService>()

        val packageName = device.packageName
        if (packageName != null) {
            val header = JBMenuItem("Package: $packageName")
            header.isEnabled = false
            menu.add(header)
            menu.addSeparator()
        } else {
            val header = JBMenuItem(PluginBundle.message("adbCommandNoPackage"))
            header.isEnabled = false
            menu.add(header)
            menu.addSeparator()
        }

        commandsService.getEnabledCommands().forEach { config ->
            val icon = ActionIconsProvider.getIconById(config.iconId)?.icon
            val item = JBMenuItem(config.name, icon)
            item.isEnabled = packageName != null
            item.addActionListener { listener?.onAdbCommandClicked(device, config) }
            menu.add(item)
        }

        menu.addSeparator()
        val customizeItem = JBMenuItem(
            PluginBundle.message("adbCommandsCustomize"),
            AllIcons.General.Settings
        )
        customizeItem.addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(null, PluginBundle.message("settingsPageName"))
        }
        menu.add(customizeItem)

        menu.show(event.component, event.x, event.y)
    }

    interface Listener {
        fun onConnectButtonClicked(device: DeviceViewModel)
        fun onDisconnectButtonClicked(device: DeviceViewModel)
        fun onShareScreenClicked(device: DeviceViewModel)
        fun onRemoveDeviceClicked(device: DeviceViewModel)
        fun onRenameDeviceClicked(device: DeviceViewModel)
        fun onCopyDeviceIdClicked(device: DeviceViewModel)
        fun onCopyDeviceAddressClicked(device: DeviceViewModel)
        fun onAdbCommandClicked(device: DeviceViewModel, command: AdbCommandConfig)
    }

    private companion object {
        private const val LIST_ITEM_HEIGHT = 71
        private const val BUTTON_CELL_HEIGHT = 32
        private val HOVER_COLOR = JBColor.namedColor(
            "Plugins.lightSelectionBackground",
            JBColor(0xF5F9FF, 0x36393B)
        )
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
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
import javax.swing.Icon
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
        val items = listOf(
            DeviceMenuItem.Rename,
            DeviceMenuItem.CopyId,
            DeviceMenuItem.CopyAddress(device.hasAddress)
        )

        val step = object : BaseListPopupStep<DeviceMenuItem>(null, items) {
            override fun getTextFor(value: DeviceMenuItem): String = when (value) {
                is DeviceMenuItem.Rename -> PluginBundle.message("renameDeviceMenuItem")
                is DeviceMenuItem.CopyId -> PluginBundle.message("copyDeviceIdMenuItem")
                is DeviceMenuItem.CopyAddress -> PluginBundle.message("copyIpAddressMenuItem")
            }

            override fun getIconFor(value: DeviceMenuItem): Icon? = when (value) {
                is DeviceMenuItem.Rename -> AllIcons.Actions.Edit
                is DeviceMenuItem.CopyId -> AllIcons.Actions.Copy
                is DeviceMenuItem.CopyAddress -> AllIcons.Actions.Copy
            }

            override fun isSelectable(value: DeviceMenuItem): Boolean = when (value) {
                is DeviceMenuItem.CopyAddress -> value.isEnabled
                else -> true
            }

            override fun onChosen(selectedValue: DeviceMenuItem, finalChoice: Boolean): PopupStep<*>? {
                return doFinalStep {
                    when (selectedValue) {
                        is DeviceMenuItem.Rename -> listener?.onRenameDeviceClicked(device)
                        is DeviceMenuItem.CopyId -> listener?.onCopyDeviceIdClicked(device)
                        is DeviceMenuItem.CopyAddress -> listener?.onCopyDeviceAddressClicked(device)
                    }
                }
            }
        }

        val fontMetrics = event.component.getFontMetrics(UIUtil.getLabelFont())
        val maxTextWidth = items.maxOfOrNull { item ->
            fontMetrics.stringWidth(step.getTextFor(item)) + JBUI.scale(16 + 8)
        } ?: 0
        val popupWidth = maxTextWidth + JBUI.scale(40)

        val popup = JBPopupFactory.getInstance().createListPopup(step)
        popup.setMinimumSize(Dimension(popupWidth, 0))
        popup.showUnderneathOf(event.component)
    }

    private fun openAdbCommandsMenu(device: DeviceViewModel, event: MouseEvent) {
        val commandsService = service<AdbCommandsService>()
        val selectedPackage = listener?.getSelectedPackage(device)
        val projectPackage = device.packageName
        val commands = commandsService.getEnabledCommands()

        // Build list of menu items
        val items = mutableListOf<AdbCommandMenuItem>()

        // Add package selector
        val packageText = selectedPackage ?: PluginBundle.message("adbCommandNoPackage")
        items.add(AdbCommandMenuItem.PackageSelector(packageText))

        // Add command items
        commands.forEach { config ->
            items.add(AdbCommandMenuItem.Command(config, selectedPackage != null))
        }

        // Add customize item
        items.add(AdbCommandMenuItem.Customize)

        val step = object : BaseListPopupStep<AdbCommandMenuItem>(null, items) {
            override fun getTextFor(value: AdbCommandMenuItem): String = when (value) {
                is AdbCommandMenuItem.PackageSelector -> value.text
                is AdbCommandMenuItem.Command -> value.config.name
                is AdbCommandMenuItem.Customize -> PluginBundle.message("adbCommandsCustomize")
            }

            override fun getIconFor(value: AdbCommandMenuItem): Icon? = when (value) {
                is AdbCommandMenuItem.PackageSelector -> null
                is AdbCommandMenuItem.Command ->
                    if (value.config.iconId.isNotEmpty()) {
                        ActionIconsProvider.getIconById(value.config.iconId)?.icon
                    } else {
                        null
                    }
                is AdbCommandMenuItem.Customize -> AllIcons.General.Settings
            }

            override fun isSelectable(value: AdbCommandMenuItem): Boolean = when (value) {
                is AdbCommandMenuItem.PackageSelector -> true
                is AdbCommandMenuItem.Command -> value.isEnabled
                is AdbCommandMenuItem.Customize -> true
            }

            override fun hasSubstep(selectedValue: AdbCommandMenuItem?): Boolean {
                return selectedValue is AdbCommandMenuItem.PackageSelector
            }

            override fun getSeparatorAbove(value: AdbCommandMenuItem): ListSeparator? = when (value) {
                is AdbCommandMenuItem.Customize -> ListSeparator()
                is AdbCommandMenuItem.Command ->
                    if (items.indexOf(value) == 1) ListSeparator() else null
                else -> null
            }

            override fun onChosen(selectedValue: AdbCommandMenuItem, finalChoice: Boolean): PopupStep<*>? {
                return when (selectedValue) {
                    is AdbCommandMenuItem.PackageSelector -> {
                        createPackageSelectionStep(device, projectPackage, selectedPackage)
                    }
                    is AdbCommandMenuItem.Command -> doFinalStep {
                        listener?.onAdbCommandClicked(device, selectedValue.config)
                    }
                    is AdbCommandMenuItem.Customize -> doFinalStep {
                        ShowSettingsUtil.getInstance().showSettingsDialog(
                            null,
                            PluginBundle.message("settingsPageName")
                        )
                    }
                }
            }
        }

        // Calculate width based on longest text
        val fontMetrics = event.component.getFontMetrics(UIUtil.getLabelFont())
        val maxTextWidth = items.maxOfOrNull { item ->
            val text = step.getTextFor(item)
            val hasIcon = step.getIconFor(item) != null
            val iconWidth = if (hasIcon) JBUI.scale(16 + 8) else 0 // icon + gap
            fontMetrics.stringWidth(text) + iconWidth
        } ?: 0
        val popupWidth = maxTextWidth + JBUI.scale(40) // padding for margins and selection insets

        val popup = JBPopupFactory.getInstance().createListPopup(step)
        popup.setMinimumSize(Dimension(popupWidth, 0))
        popup.showUnderneathOf(event.component)
    }

    private fun createPackageSelectionStep(
        device: DeviceViewModel,
        projectPackage: String?,
        selectedPackage: String?
    ): PopupStep<*> {
        val packageItems = mutableListOf<PackageMenuItem>()

        // Add project package option if available
        if (projectPackage != null) {
            packageItems.add(
                PackageMenuItem.ProjectPackage(
                    projectPackage,
                    isSelected = selectedPackage == projectPackage || selectedPackage == null
                )
            )
        }

        // Add installed packages
        val installedPackages = listener?.getInstalledPackages(device) ?: emptyList()
        if (installedPackages.isEmpty() && projectPackage == null) {
            packageItems.add(PackageMenuItem.NoPackages)
        } else {
            installedPackages.forEach { pkg ->
                if (pkg != projectPackage) {
                    packageItems.add(PackageMenuItem.InstalledPackage(pkg, isSelected = pkg == selectedPackage))
                }
            }
        }

        return object : BaseListPopupStep<PackageMenuItem>(
            PluginBundle.message("adbCommandSelectPackage"),
            packageItems
        ) {
            override fun getTextFor(value: PackageMenuItem): String = when (value) {
                is PackageMenuItem.ProjectPackage ->
                    PluginBundle.message("adbCommandPackageFromProject", value.packageName)
                is PackageMenuItem.InstalledPackage -> value.packageName
                is PackageMenuItem.NoPackages -> PluginBundle.message("adbCommandNoPackagesInstalled")
            }

            override fun getIconFor(value: PackageMenuItem): Icon? = when (value) {
                is PackageMenuItem.ProjectPackage ->
                    if (value.isSelected) AllIcons.Actions.Checked else null
                is PackageMenuItem.InstalledPackage ->
                    if (value.isSelected) AllIcons.Actions.Checked else null
                is PackageMenuItem.NoPackages -> null
            }

            override fun isSelectable(value: PackageMenuItem): Boolean = when (value) {
                is PackageMenuItem.NoPackages -> false
                else -> true
            }

            override fun getSeparatorAbove(value: PackageMenuItem): ListSeparator? = when {
                value is PackageMenuItem.InstalledPackage &&
                    packageItems.indexOf(value) == 1 &&
                    packageItems.firstOrNull() is PackageMenuItem.ProjectPackage -> ListSeparator()
                else -> null
            }

            override fun onChosen(selectedValue: PackageMenuItem, finalChoice: Boolean): PopupStep<*>? {
                return doFinalStep {
                    when (selectedValue) {
                        is PackageMenuItem.ProjectPackage -> {
                            listener?.onPackageSelected(device, null) // null means use project package
                        }
                        is PackageMenuItem.InstalledPackage -> {
                            listener?.onPackageSelected(device, selectedValue.packageName)
                        }
                        is PackageMenuItem.NoPackages -> {}
                    }
                }
            }
        }
    }

    private sealed class DeviceMenuItem {
        data object Rename : DeviceMenuItem()
        data object CopyId : DeviceMenuItem()
        data class CopyAddress(val isEnabled: Boolean) : DeviceMenuItem()
    }

    private sealed class AdbCommandMenuItem {
        data class PackageSelector(val text: String) : AdbCommandMenuItem()
        data class Command(val config: AdbCommandConfig, val isEnabled: Boolean) : AdbCommandMenuItem()
        data object Customize : AdbCommandMenuItem()
    }

    private sealed class PackageMenuItem {
        data class ProjectPackage(val packageName: String, val isSelected: Boolean) : PackageMenuItem()
        data class InstalledPackage(val packageName: String, val isSelected: Boolean) : PackageMenuItem()
        data object NoPackages : PackageMenuItem()
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
        fun onPackageSelected(device: DeviceViewModel, packageName: String?)
        fun getInstalledPackages(device: DeviceViewModel): List<String>
        fun getSelectedPackage(device: DeviceViewModel): String?
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

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
import dev.polek.adbwifi.utils.Colors
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.makeBold
import dev.polek.adbwifi.utils.makeMonospaced
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class DevicePanel(private val device: DeviceViewModel) : JBPanel<DevicePanel>(BorderLayout()) {

    var listener: Listener? = null

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw rounded background
        g2.color = Colors.CARD_BACKGROUND
        g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

        // Draw rounded border
        g2.color = Colors.CARD_BORDER
        g2.drawRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

        g2.dispose()
        super.paintComponent(g)
    }

    init {
        isOpaque = false // Don't paint default background - we paint rounded rect ourselves
        border = JBUI.Borders.empty(CARD_PADDING)

        // Main content panel
        val contentPanel = JPanel(BorderLayout(ICON_GAP, 0))
        contentPanel.isOpaque = false

        // Device icon (only show for non-pinned/previously connected devices)
        if (!device.isRemoveButtonVisible) {
            val iconLabel = JBLabel(device.icon)
            iconLabel.verticalAlignment = SwingConstants.TOP
            iconLabel.border = JBUI.Borders.emptyTop(2)
            contentPanel.add(iconLabel, BorderLayout.WEST)
        }

        // Device info panel (name, android version, IP)
        val infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.isOpaque = false
        infoPanel.alignmentX = LEFT_ALIGNMENT

        // Device name
        val nameLabel = JBLabel(device.titleText)
        nameLabel.foreground = Colors.PRIMARY_TEXT
        nameLabel.makeBold()
        nameLabel.alignmentX = LEFT_ALIGNMENT
        nameLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    listener?.onRenameDeviceClicked(device)
                }
            }
        })
        infoPanel.add(nameLabel)
        infoPanel.add(Box.createVerticalStrut(4))

        // Android version and API level
        val versionLabel = JBLabel("Android ${device.device.androidVersion} • API ${device.device.apiLevel}")
        versionLabel.foreground = Colors.SECONDARY_TEXT
        versionLabel.makeMonospaced()
        versionLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        versionLabel.alignmentX = LEFT_ALIGNMENT
        infoPanel.add(versionLabel)
        infoPanel.add(Box.createVerticalStrut(2))

        // IP address
        val addressText = device.address?.let { "$it:${device.device.port}" } ?: ""
        if (addressText.isNotEmpty()) {
            val addressLabel = JBLabel(addressText)
            addressLabel.foreground = Colors.SECONDARY_TEXT
            addressLabel.makeMonospaced()
            addressLabel.componentStyle = UIUtil.ComponentStyle.SMALL
            addressLabel.alignmentX = LEFT_ALIGNMENT
            // Make it slightly more transparent
            addressLabel.foreground = JBColor(
                Colors.SECONDARY_TEXT.rgb and 0x00FFFFFF or (0xB3 shl 24),
                Colors.SECONDARY_TEXT.rgb and 0x00FFFFFF or (0xB3 shl 24)
            )
            infoPanel.add(addressLabel)
        }

        contentPanel.add(infoPanel, BorderLayout.CENTER)
        add(contentPanel, BorderLayout.NORTH)

        // Separator
        val separatorPanel = JPanel(BorderLayout())
        separatorPanel.isOpaque = false
        separatorPanel.border = JBUI.Borders.emptyTop(SEPARATOR_MARGIN)
        val separator = JSeparator()
        separator.foreground = Colors.SEPARATOR
        separatorPanel.add(separator, BorderLayout.CENTER)
        add(separatorPanel, BorderLayout.CENTER)

        // Action buttons panel - use BorderLayout for flex-grow behavior
        val actionsPanel = JPanel(BorderLayout(0, 0))
        actionsPanel.isOpaque = false
        actionsPanel.border = JBUI.Borders.emptyTop(SEPARATOR_MARGIN)

        // Main action button (Connect/Disconnect) - expands to fill width
        if (device.isInProgress) {
            val progressBar = JProgressBar()
            progressBar.isIndeterminate = true
            progressBar.preferredSize = Dimension(0, BUTTON_HEIGHT)
            actionsPanel.add(progressBar, BorderLayout.CENTER)
        } else {
            val mainButton = createMainButton()
            mainButton.addActionListener {
                when (device.buttonType) {
                    ButtonType.CONNECT, ButtonType.CONNECT_DISABLED -> listener?.onConnectButtonClicked(device)
                    ButtonType.DISCONNECT -> listener?.onDisconnectButtonClicked(device)
                }
            }
            actionsPanel.add(mainButton, BorderLayout.CENTER)
        }

        // Icon buttons panel (fixed width buttons on the right)
        val iconButtonsPanel = JPanel()
        iconButtonsPanel.layout = BoxLayout(iconButtonsPanel, BoxLayout.X_AXIS)
        iconButtonsPanel.isOpaque = false

        if (device.isAdbCommandsButtonVisible) {
            iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
            val adbCommandsButton = createIconButton(Icons.ADB_COMMANDS, PluginBundle.message("adbCommandsTooltip"))
            adbCommandsButton.addActionListener {
                val event = MouseEvent(
                    adbCommandsButton,
                    MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(),
                    0,
                    0,
                    0,
                    1,
                    false
                )
                openAdbCommandsMenu(device, event)
            }
            iconButtonsPanel.add(adbCommandsButton)
        }

        if (device.isShareScreenButtonVisible) {
            iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
            val shareScreenButton = createIconButton(Icons.SHARE_SCREEN, PluginBundle.message("shareScreenTooltip"))
            shareScreenButton.addActionListener {
                listener?.onShareScreenClicked(device)
            }
            iconButtonsPanel.add(shareScreenButton)
        }

        if (device.isRemoveButtonVisible) {
            iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
            val removeButton = createIconButton(Icons.DELETE, PluginBundle.message("removeDeviceTooltip"))
            removeButton.addActionListener {
                listener?.onRemoveDeviceClicked(device)
            }
            iconButtonsPanel.add(removeButton)
        }

        val menuButton = createIconButton(Icons.MENU, null, showBorder = false)
        menuButton.addActionListener {
            val event = MouseEvent(
                menuButton,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                0,
                0,
                1,
                false
            )
            openDeviceMenu(device, event)
        }
        iconButtonsPanel.add(menuButton)

        actionsPanel.add(iconButtonsPanel, BorderLayout.EAST)
        add(actionsPanel, BorderLayout.SOUTH)
    }

    private fun createMainButton(): JButton {
        return when (device.buttonType) {
            ButtonType.CONNECT -> {
                if (device.isRemoveButtonVisible) {
                    createSecondaryConnectButton()
                } else {
                    createConnectButton()
                }
            }
            ButtonType.CONNECT_DISABLED -> {
                if (device.isRemoveButtonVisible) {
                    createSecondaryConnectButton().apply { isEnabled = false }
                } else {
                    createConnectButton().apply { isEnabled = false }
                }
            }
            ButtonType.DISCONNECT -> createDisconnectButton()
        }
    }

    private fun createConnectButton(): JButton {
        return object : JButton(PluginBundle.message("connectButton")) {
            init {
                preferredSize = Dimension(0, BUTTON_HEIGHT)
                minimumSize = Dimension(0, BUTTON_HEIGHT)
                isOpaque = false
                isContentAreaFilled = false
                isFocusPainted = false
                isBorderPainted = false
                background = Colors.GREEN_BUTTON_BG
                foreground = Colors.GREEN_BUTTON_TEXT
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                // Draw background
                g2.color = if (model.isPressed) Colors.GREEN_BUTTON_BG.darker() else background
                g2.fillRoundRect(0, 0, width, height, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Draw text centered
                g2.color = foreground
                g2.font = font
                val fm = g2.fontMetrics
                val textWidth = fm.stringWidth(text)
                val x = (width - textWidth) / 2
                val y = (height - fm.height) / 2 + fm.ascent
                g2.drawString(text, x, y)

                g2.dispose()
            }
        }
    }

    private fun createSecondaryConnectButton(): JButton {
        return object : JButton(PluginBundle.message("connectButton")) {
            init {
                preferredSize = Dimension(0, BUTTON_HEIGHT)
                minimumSize = Dimension(0, BUTTON_HEIGHT)
                isOpaque = false
                isContentAreaFilled = false
                isFocusPainted = false
                isBorderPainted = false
                background = Colors.ICON_BUTTON_BG
                foreground = Colors.PRIMARY_TEXT
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                // Draw background
                g2.color = if (model.isPressed) Colors.CARD_BORDER else background
                g2.fillRoundRect(0, 0, width, height, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Draw border
                g2.color = Colors.CARD_BORDER
                g2.drawRoundRect(0, 0, width - 1, height - 1, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Draw text centered
                g2.color = foreground
                g2.font = font
                val fm = g2.fontMetrics
                val textWidth = fm.stringWidth(text)
                val x = (width - textWidth) / 2
                val y = (height - fm.height) / 2 + fm.ascent
                g2.drawString(text, x, y)

                g2.dispose()
            }
        }
    }

    private fun createDisconnectButton(): JButton {
        return object : JButton(PluginBundle.message("disconnectButton")) {
            init {
                preferredSize = Dimension(0, BUTTON_HEIGHT)
                minimumSize = Dimension(0, BUTTON_HEIGHT)
                isOpaque = false
                isContentAreaFilled = false
                isFocusPainted = false
                isBorderPainted = false
                background = Colors.RED_BUTTON_BG
                foreground = Colors.RED_BUTTON_TEXT
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

                // Draw background
                g2.color = if (model.isPressed) Colors.RED_BUTTON_BORDER else background
                g2.fillRoundRect(0, 0, width, height, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Draw border
                g2.color = Colors.RED_BUTTON_BORDER
                g2.drawRoundRect(0, 0, width - 1, height - 1, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                // Draw text centered
                g2.color = foreground
                g2.font = font
                val fm = g2.fontMetrics
                val textWidth = fm.stringWidth(text)
                val x = (width - textWidth) / 2
                val y = (height - fm.height) / 2 + fm.ascent
                g2.drawString(text, x, y)

                g2.dispose()
            }
        }
    }

    private fun createIconButton(icon: Icon, tooltip: String?, showBorder: Boolean = true): JButton {
        return object : JButton() {
            init {
                preferredSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                minimumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                maximumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                isOpaque = false
                isContentAreaFilled = false
                isFocusPainted = false
                isBorderPainted = false
                toolTipText = tooltip
                background = Colors.ICON_BUTTON_BG
            }

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                if (showBorder) {
                    // Draw background
                    g2.color = if (model.isPressed) Colors.CARD_BORDER else background
                    g2.fillRoundRect(0, 0, width, height, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)

                    // Draw border
                    g2.color = Colors.CARD_BORDER
                    g2.drawRoundRect(0, 0, width - 1, height - 1, BUTTON_CORNER_RADIUS * 2, BUTTON_CORNER_RADIUS * 2)
                }

                // Draw icon centered
                val iconX = (width - icon.iconWidth) / 2
                val iconY = (height - icon.iconHeight) / 2
                icon.paintIcon(this, g2, iconX, iconY)

                g2.dispose()
            }
        }
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
            val isEnabled = !config.requiresPackage || selectedPackage != null
            items.add(AdbCommandMenuItem.Command(config, isEnabled))
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
                        createPackageSelectionStep(device, projectPackage, selectedPackage, event.component)
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
        selectedPackage: String?,
        menuComponent: Component
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
                    // Reopen the ADB commands menu with updated package
                    if (selectedValue !is PackageMenuItem.NoPackages) {
                        SwingUtilities.invokeLater {
                            reopenAdbCommandsMenu(device, menuComponent)
                        }
                    }
                }
            }
        }
    }

    private fun reopenAdbCommandsMenu(device: DeviceViewModel, component: Component) {
        val event = MouseEvent(
            component,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            0,
            0,
            0,
            1,
            false
        )
        openAdbCommandsMenu(device, event)
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
        private const val CORNER_RADIUS = 5
        private const val BUTTON_CORNER_RADIUS = 5
        private const val CARD_PADDING = 10
        private const val ICON_GAP = 8
        private const val SEPARATOR_MARGIN = 12
        private const val BUTTON_GAP = 8
        private const val BUTTON_HEIGHT = 32
        private const val ICON_BUTTON_SIZE = 32
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
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
import dev.polek.adbwifi.ui.view.buttons.IconButton
import dev.polek.adbwifi.ui.view.buttons.LoadingPanel
import dev.polek.adbwifi.ui.view.buttons.StyledButton
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

    private var isWideLayout = false

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

        buildLayout()

        addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent) {
                val shouldBeWide = width >= WIDE_LAYOUT_THRESHOLD
                if (shouldBeWide != isWideLayout) {
                    isWideLayout = shouldBeWide
                    rebuildLayout()
                }
            }
        })

        // For previously connected devices, double-clicking on the panel opens the Edit dialog
        if (device.isPreviouslyConnected) {
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        listener?.onEditDeviceClicked(device)
                    }
                }
            })
        }
    }

    private fun rebuildLayout() {
        removeAll()
        buildLayout()
        // Update maximum size since layout changed
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        revalidate()
        repaint()
        // Also revalidate parent to adjust for new size
        parent?.revalidate()
    }

    private fun buildLayout() {
        isWideLayout = width >= WIDE_LAYOUT_THRESHOLD

        if (isWideLayout) {
            buildWideLayout()
        } else {
            buildNarrowLayout()
        }
    }

    private fun buildNarrowLayout() {
        // Main content panel
        val contentPanel = JPanel(BorderLayout(ICON_GAP, 0))
        contentPanel.isOpaque = false

        // Device icon (only show when icon is available)
        device.icon?.let { icon ->
            val iconLabel = JBLabel(icon)
            iconLabel.verticalAlignment = SwingConstants.TOP
            iconLabel.border = JBUI.Borders.emptyTop(2)
            contentPanel.add(iconLabel, BorderLayout.WEST)
        }

        contentPanel.add(createInfoPanel(), BorderLayout.CENTER)
        add(contentPanel, BorderLayout.NORTH)

        // Separator
        add(createSeparator(), BorderLayout.CENTER)

        // Action buttons panel - use BorderLayout for flex-grow behavior
        val actionsPanel = JPanel(BorderLayout(0, 0))
        actionsPanel.isOpaque = false
        actionsPanel.border = JBUI.Borders.emptyTop(SEPARATOR_MARGIN)

        // Main action button (Connect/Disconnect) - expands to fill width
        if (device.isInProgress) {
            actionsPanel.add(LoadingPanel(LoadingPanel.Size.FULL_WIDTH), BorderLayout.CENTER)
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
        actionsPanel.add(createIconButtonsPanel(includeStrut = true), BorderLayout.EAST)
        add(actionsPanel, BorderLayout.SOUTH)
    }

    private fun buildWideLayout() {
        // Main content panel with device info on left and buttons on right
        val contentPanel = JPanel(BorderLayout(WIDE_LAYOUT_GAP, 0))
        contentPanel.isOpaque = false

        // Left side: device icon + info
        val leftPanel = JPanel(BorderLayout(ICON_GAP, 0))
        leftPanel.isOpaque = false

        device.icon?.let { icon ->
            val iconLabel = JBLabel(icon)
            iconLabel.verticalAlignment = SwingConstants.TOP
            iconLabel.border = JBUI.Borders.emptyTop(2)
            leftPanel.add(iconLabel, BorderLayout.WEST)
        }

        leftPanel.add(createInfoPanel(), BorderLayout.CENTER)
        contentPanel.add(leftPanel, BorderLayout.CENTER)

        // Right side: main button on top, icon buttons below (top-aligned)
        val rightPanel = JPanel()
        rightPanel.layout = BoxLayout(rightPanel, BoxLayout.Y_AXIS)
        rightPanel.isOpaque = false

        // Main action button (Connect/Disconnect) with fixed width
        if (device.isInProgress) {
            val loadingPanel = LoadingPanel(LoadingPanel.Size.FULL_WIDTH)
            loadingPanel.preferredSize = Dimension(MAIN_BUTTON_WIDTH, loadingPanel.preferredSize.height)
            loadingPanel.maximumSize = loadingPanel.preferredSize
            loadingPanel.alignmentX = RIGHT_ALIGNMENT
            rightPanel.add(loadingPanel)
        } else {
            val mainButton = createMainButton()
            mainButton.preferredSize = Dimension(MAIN_BUTTON_WIDTH, mainButton.preferredSize.height)
            mainButton.maximumSize = mainButton.preferredSize
            mainButton.alignmentX = RIGHT_ALIGNMENT
            mainButton.addActionListener {
                when (device.buttonType) {
                    ButtonType.CONNECT, ButtonType.CONNECT_DISABLED -> listener?.onConnectButtonClicked(device)
                    ButtonType.DISCONNECT -> listener?.onDisconnectButtonClicked(device)
                }
            }
            rightPanel.add(mainButton)
        }

        rightPanel.add(Box.createVerticalStrut(BUTTON_GAP))

        // Icon buttons below the main button
        val iconButtonsPanel = createIconButtonsPanel(includeStrut = false)
        iconButtonsPanel.alignmentX = RIGHT_ALIGNMENT
        rightPanel.add(iconButtonsPanel)

        contentPanel.add(rightPanel, BorderLayout.EAST)
        add(contentPanel, BorderLayout.NORTH)
    }

    private fun createInfoPanel(): JPanel {
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
                    if (device.isPreviouslyConnected) {
                        listener?.onEditDeviceClicked(device)
                    } else {
                        listener?.onRenameDeviceClicked(device)
                    }
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
        infoPanel.add(Box.createVerticalStrut(4))

        // IP address
        val addressText = device.address?.let { "$it:${device.device.port}" } ?: ""
        if (addressText.isNotEmpty()) {
            val addressLabel = JBLabel(addressText)
            addressLabel.foreground = Colors.SECONDARY_TEXT
            addressLabel.makeMonospaced()
            addressLabel.componentStyle = UIUtil.ComponentStyle.SMALL
            addressLabel.alignmentX = LEFT_ALIGNMENT
            infoPanel.add(addressLabel)
        }

        return infoPanel
    }

    private fun createSeparator(): JPanel {
        val separatorPanel = JPanel(BorderLayout())
        separatorPanel.isOpaque = false
        separatorPanel.border = JBUI.Borders.emptyTop(SEPARATOR_MARGIN)
        val separator = JSeparator()
        separator.foreground = Colors.SEPARATOR
        separatorPanel.add(separator, BorderLayout.CENTER)
        return separatorPanel
    }

    private fun createIconButtonsPanel(includeStrut: Boolean): JPanel {
        val iconButtonsPanel = JPanel()
        iconButtonsPanel.layout = BoxLayout(iconButtonsPanel, BoxLayout.X_AXIS)
        iconButtonsPanel.isOpaque = false

        if (device.isAdbCommandsButtonVisible) {
            if (includeStrut) {
                iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
            }
            val adbCommandsButton = IconButton(Icons.ADB_COMMANDS, PluginBundle.message("adbCommandsTooltip"))
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
            if (device.isShareScreenInProgress) {
                iconButtonsPanel.add(LoadingPanel(LoadingPanel.Size.ICON))
            } else {
                val shareScreenButton = IconButton(Icons.SHARE_SCREEN, PluginBundle.message("shareScreenTooltip"))
                shareScreenButton.addActionListener {
                    listener?.onShareScreenClicked(device)
                }
                iconButtonsPanel.add(shareScreenButton)
            }
        }

        if (device.isRemoveButtonVisible) {
            iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
            val removeButton = IconButton(Icons.DELETE, PluginBundle.message("removeDeviceTooltip"))
            removeButton.addActionListener {
                listener?.onRemoveDeviceClicked(device)
            }
            iconButtonsPanel.add(removeButton)
        }

        iconButtonsPanel.add(Box.createHorizontalStrut(BUTTON_GAP))
        val menuButton = IconButton(Icons.MENU, showBorder = false)
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

        return iconButtonsPanel
    }

    private fun createMainButton(): JButton {
        val connectText = PluginBundle.message("connectButton")
        val disconnectText = PluginBundle.message("disconnectButton")

        return when (device.buttonType) {
            ButtonType.CONNECT -> {
                if (device.isPreviouslyConnected) {
                    StyledButton(connectText, StyledButton.Style.SECONDARY)
                } else {
                    StyledButton(connectText, StyledButton.Style.PRIMARY)
                }
            }
            ButtonType.CONNECT_DISABLED -> {
                if (device.isPreviouslyConnected) {
                    StyledButton(connectText, StyledButton.Style.SECONDARY).apply { isEnabled = false }
                } else {
                    StyledButton(connectText, StyledButton.Style.PRIMARY).apply { isEnabled = false }
                }
            }
            ButtonType.DISCONNECT -> StyledButton(disconnectText, StyledButton.Style.DANGER)
        }
    }

    private fun openDeviceMenu(device: DeviceViewModel, event: MouseEvent) {
        val items = if (device.isPreviouslyConnected) {
            listOf(
                DeviceMenuItem.Edit,
                DeviceMenuItem.CopyId,
                DeviceMenuItem.CopyAddress(device.hasAddress)
            )
        } else {
            listOf(
                DeviceMenuItem.Rename,
                DeviceMenuItem.CopyId,
                DeviceMenuItem.CopyAddress(device.hasAddress)
            )
        }

        val step = object : BaseListPopupStep<DeviceMenuItem>(null, items) {
            override fun getTextFor(value: DeviceMenuItem): String = when (value) {
                is DeviceMenuItem.Rename -> PluginBundle.message("renameDeviceMenuItem")
                is DeviceMenuItem.Edit -> PluginBundle.message("editDeviceMenuItem")
                is DeviceMenuItem.CopyId -> PluginBundle.message("copyDeviceIdMenuItem")
                is DeviceMenuItem.CopyAddress -> PluginBundle.message("copyIpAddressMenuItem")
            }

            override fun getIconFor(value: DeviceMenuItem): Icon = when (value) {
                is DeviceMenuItem.Rename -> AllIcons.Actions.Edit
                is DeviceMenuItem.Edit -> AllIcons.Actions.Edit
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
                        is DeviceMenuItem.Edit -> listener?.onEditDeviceClicked(device)
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
        data object Edit : DeviceMenuItem()
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
        fun onEditDeviceClicked(device: DeviceViewModel)
        fun onCopyDeviceIdClicked(device: DeviceViewModel)
        fun onCopyDeviceAddressClicked(device: DeviceViewModel)
        fun onAdbCommandClicked(device: DeviceViewModel, command: AdbCommandConfig)
        fun onPackageSelected(device: DeviceViewModel, packageName: String?)
        fun getInstalledPackages(device: DeviceViewModel): List<String>
        fun getSelectedPackage(device: DeviceViewModel): String?
    }

    private companion object {
        private const val CORNER_RADIUS = 5
        private const val CARD_PADDING = 10
        private const val ICON_GAP = 8
        private const val SEPARATOR_MARGIN = 12
        private const val BUTTON_GAP = 8
        private const val WIDE_LAYOUT_THRESHOLD = 400
        private const val WIDE_LAYOUT_GAP = 16
        private const val MAIN_BUTTON_WIDTH = 120
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DoNotAskOption
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.actions.OpenSettingsNotificationAction
import dev.polek.adbwifi.model.AdbCommandConfig
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.services.PropertiesService
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.presenter.ToolWindowPresenter
import dev.polek.adbwifi.utils.GridBagLayoutPanel
import dev.polek.adbwifi.utils.Icons
import dev.polek.adbwifi.utils.panel
import dev.polek.adbwifi.utils.setFontSize
import java.awt.GridBagConstraints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

class AdbWiFiToolWindow(
    private val project: Project,
    private val toolWindow: ToolWindow
) : BorderLayoutPanel(), Disposable, ToolWindowView {

    private val presenter = ToolWindowPresenter(project)
    private val propertiesService = service<PropertiesService>()

    private val devicePanelListener = object : DevicePanel.Listener {

        override fun onConnectButtonClicked(device: DeviceViewModel) {
            presenter.onConnectButtonClicked(device)
        }

        override fun onDisconnectButtonClicked(device: DeviceViewModel) {
            presenter.onDisconnectButtonClicked(device)
        }

        override fun onShareScreenClicked(device: DeviceViewModel) {
            presenter.onShareScreenButtonClicked(device)
        }

        override fun onRemoveDeviceClicked(device: DeviceViewModel) {
            presenter.onRemoveDeviceButtonClicked(device)
        }

        override fun onRenameDeviceClicked(device: DeviceViewModel) {
            presenter.onRenameDeviceClicked(device)
        }

        override fun onCopyDeviceIdClicked(device: DeviceViewModel) {
            presenter.onCopyDeviceIdClicked(device)
        }

        override fun onCopyDeviceAddressClicked(device: DeviceViewModel) {
            presenter.onCopyDeviceAddressClicked(device)
        }

        override fun onAdbCommandClicked(device: DeviceViewModel, command: AdbCommandConfig) {
            presenter.onAdbCommandClicked(device, command)
        }

        override fun onPackageSelected(device: DeviceViewModel, packageName: String?) {
            presenter.setSelectedPackage(device, packageName)
        }

        override fun getInstalledPackages(device: DeviceViewModel): List<String> {
            return presenter.getInstalledPackages(device)
        }

        override fun getSelectedPackage(device: DeviceViewModel): String? {
            return presenter.getSelectedPackage(device)
        }
    }

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", DEFAULT_PANEL_PROPORTION)
    private val deviceListPanel = DeviceListPanel(devicePanelListener)
    private val pinnedDeviceListPanel = DeviceListPanel(
        devicePanelListener,
        showHeader = true,
        title = PluginBundle.message("previouslyConnectedTitle"),
        isHeaderExpanded = propertiesService.isPreviouslyConnectedDevicesExpanded,
        onHeaderExpandChanged = { isExpanded ->
            propertiesService.isPreviouslyConnectedDevicesExpanded = isExpanded
        }
    )
    private val logPanel = LogPanel()
    private val topPanel = JBScrollPane().apply {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)
        panel.add(deviceListPanel)
        panel.add(pinnedDeviceListPanel)
        this.setViewportView(panel)
    }
    private val bottomPanel: JComponent
    private val emptyStatePanel = EmptyStatePanel(
        onEnterIpPort = {
            ConnectDeviceDialogWrapper().show()
        }
    )
    private val errorMessagePanel = GridBagLayoutPanel().apply {
        background = JBColor.background()
        border = BorderFactory.createLineBorder(JBColor.border())

        val label = JBLabel().apply {
            @Suppress("DialogTitleCapitalization")
            text = PluginBundle.message("adbPathVerificationErrorMessage", location)
            icon = Icons.DEVICE_WARNING
            horizontalAlignment = SwingConstants.CENTER
            horizontalTextPosition = SwingConstants.CENTER
            verticalTextPosition = SwingConstants.BOTTOM
            setFontSize(16f)
            foreground = JBColor.gray
        }
        add(
            label,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
            }
        )

        val settingsButton = HyperlinkLabel(PluginBundle.message("goToSettingsButton")).apply {
            setFontSize(16f)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    ShowSettingsUtil.getInstance().showSettingsDialog(
                        null,
                        PluginBundle.message("settingsPageName")
                    )
                }
            })
        }
        add(
            settingsButton,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 1
                insets = JBUI.insets(20, 0)
            }
        )
    }

    init {
        val actionManager = ActionManager.getInstance()
        val toolbarActionGroup = actionManager.getAction("AdbWifi.ToolbarActions") as DefaultActionGroup
        val toolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_TITLE,
            toolbarActionGroup,
            true
        )
        toolbar.targetComponent = this
        addToTop(toolbar.component)
        addToCenter(splitter)

        val logToolbarActionGroup = actionManager.getAction("AdbWifi.LogToolbarActions") as DefaultActionGroup
        val logToolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_CONTENT,
            logToolbarActionGroup,
            false
        )
        logToolbar.targetComponent = this
        bottomPanel = panel(center = JBScrollPane(logPanel), left = logToolbar.component)

        splitter.firstComponent = topPanel

        project.messageBus
            .connect(this)
            .subscribe(
                ToolWindowManagerListener.TOPIC,
                object : ToolWindowManagerListener {
                    override fun stateChanged(toolWindowManager: ToolWindowManager) {
                        if (toolWindow.isVisible) {
                            presenter.onViewOpen()
                        } else {
                            presenter.onViewClosed()
                        }
                    }
                }
            )

        presenter.attach(this)
    }

    override fun dispose() {
        presenter.detach()
    }

    override fun showDevices(devices: List<DeviceViewModel>) {
        splitter.firstComponent = topPanel
        deviceListPanel.devices = devices
    }

    override fun showPinnedDevices(devices: List<DeviceViewModel>) {
        pinnedDeviceListPanel.devices = devices
        pinnedDeviceListPanel.isVisible = devices.isNotEmpty()
    }

    override fun showEmptyMessage() {
        splitter.firstComponent = emptyStatePanel
    }

    override fun showInvalidAdbLocationError() {
        splitter.firstComponent = errorMessagePanel
    }

    override fun openLog() {
        splitter.secondComponent = bottomPanel
    }

    override fun closeLog() {
        splitter.secondComponent = null
    }

    override fun setLogEntries(entries: List<LogEntry>) {
        logPanel.setLogEntries(entries)
    }

    override fun showInvalidScrcpyLocationError() {
        @Suppress("DialogTitleCapitalization")
        val notification = NOTIFICATION_GROUP.createNotification(
            PluginBundle.message("name"),
            PluginBundle.message("scrcpyPathVerificationErrorMessage"),
            NotificationType.ERROR
        )
        notification.addAction(OpenSettingsNotificationAction())
        notification.notify(project)
    }

    override fun showScrcpyError(error: String) {
        @Suppress("DialogTitleCapitalization")
        val notification = NOTIFICATION_GROUP.createNotification(
            PluginBundle.message("name"),
            error,
            NotificationType.ERROR
        )
        notification.addAction(OpenSettingsNotificationAction())
        notification.notify(project)
    }

    override fun showRemoveDeviceConfirmation(device: DeviceViewModel) {
        val doNotAskAgain = object : DoNotAskOption.Adapter() {
            override fun rememberChoice(isSelected: Boolean, exitCode: Int) {
                if (exitCode == Messages.OK) {
                    presenter.onRemoveDeviceConfirmed(device, doNotAskAgain = isSelected)
                }
            }

            override fun getDoNotShowMessage(): String {
                return PluginBundle.message("doNotAskAgain")
            }
        }
        MessageDialogBuilder.yesNo(title = device.titleText, message = PluginBundle.message("removeDeviceConfirmation"))
            .yesText(PluginBundle.message("removeButton"))
            .noText(PluginBundle.message("cancelButton"))
            .doNotAsk(doNotAskAgain)
            .ask(project)
    }

    override fun showRenameDeviceDialog(device: DeviceViewModel) {
        RenameDeviceDialogWrapper(device).show()
    }

    private companion object {
        private const val DEFAULT_PANEL_PROPORTION = 0.6f
        private val NOTIFICATION_GROUP = NotificationGroupManager.getInstance()
            .getNotificationGroup("adb_wifi_notification_group")
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.ui.presenter.ToolWindowPresenter
import dev.polek.adbwifi.utils.panel
import dev.polek.adbwifi.utils.setFontSize
import javax.swing.JComponent
import javax.swing.SwingConstants

class AdbWiFiToolWindow(
    project: Project,
    private val toolWindow: ToolWindow
) : BorderLayoutPanel(), Disposable, ToolWindowView {

    private val presenter = ToolWindowPresenter()

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", DEFAULT_PANEL_PROPORTION)
    private val deviceListPanel = DeviceListPanel(presenter)
    private val logPanel = LogPanel()
    private val topPanel = JBScrollPane(deviceListPanel)
    private val bottomPanel: JComponent
    private val emptyMessageLabel = JBLabel().apply {
        text = PluginBundle.message("deviceListEmptyMessage")
        icon = IconLoader.getIcon("/icons/devices-lineup.png")
        horizontalAlignment = SwingConstants.CENTER
        horizontalTextPosition = SwingConstants.CENTER
        verticalTextPosition = SwingConstants.BOTTOM
        setFontSize(16f)
        background = JBColor.background()
        foreground = JBColor.gray
        isOpaque = true
    }
    private val errorMessageLabel = JBLabel().apply {
        text = PluginBundle.message("adbLocationVerificationErrorMessage", location)
        icon = IconLoader.getIcon("/icons/deviceWarning.png")
        horizontalAlignment = SwingConstants.CENTER
        horizontalTextPosition = SwingConstants.CENTER
        verticalTextPosition = SwingConstants.BOTTOM
        setFontSize(16f)
        background = JBColor.background()
        foreground = JBColor.gray
        isOpaque = true
    }

    init {
        val actionManager = ActionManager.getInstance()
        val toolbarActionGroup = actionManager.getAction("AdbWifi.ToolbarActions") as DefaultActionGroup
        val toolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_TITLE,
            toolbarActionGroup,
            true
        )
        toolbar.setTargetComponent(this)
        addToTop(toolbar.component)
        addToCenter(splitter)

        val logToolbarActionGroup = actionManager.getAction("AdbWifi.LogToolbarActions") as DefaultActionGroup
        val logToolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_CONTENT,
            logToolbarActionGroup,
            false
        )
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

    override fun showEmptyMessage() {
        splitter.firstComponent = emptyMessageLabel
    }

    override fun showInvalidAdbLocationError(location: String) {
        splitter.firstComponent = errorMessageLabel
    }

    override fun showConfigurationError() {
        TODO("Not yet implemented")
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

    private companion object {
        const val DEFAULT_PANEL_PROPORTION = 0.6f
    }
}

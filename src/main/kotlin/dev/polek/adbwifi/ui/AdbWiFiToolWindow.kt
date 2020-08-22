package dev.polek.adbwifi.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.LogService
import dev.polek.adbwifi.utils.panel
import javax.swing.JComponent

class AdbWiFiToolWindow(project: Project, private val toolWindow: ToolWindow) : BorderLayoutPanel(), Disposable {

    private val adbService by lazy { ServiceManager.getService(AdbService::class.java) }
    private val logService by lazy { ServiceManager.getService(LogService::class.java) }

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", DEFAULT_PANEL_PROPORTION)
    private val deviceListPanel = DeviceListPanel()
    private val logPanel = LogPanel()
    private val topPanel = JBScrollPane(deviceListPanel)
    private val bottomPanel: JComponent

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

        updateLogPanel(logService.isLogVisible)
        logService.logVisibilityListener = { isVisible ->
            updateLogPanel(isVisible)
        }

        project.messageBus
            .connect(this)
            .subscribe(
                ToolWindowManagerListener.TOPIC,
                object : ToolWindowManagerListener {
                    override fun stateChanged(toolWindowManager: ToolWindowManager) {
                        if (toolWindow.isVisible) {
                            subscribeToDeviceList()
                        } else {
                            unsubscribeFromDeviceList()
                        }
                    }
                }
            )

        if (toolWindow.isVisible) {
            subscribeToDeviceList()
        }
    }

    override fun dispose() {
        unsubscribeFromDeviceList()
        logService.logVisibilityListener = null
    }

    private fun subscribeToDeviceList() {
        if (adbService.deviceListListener != null) {
            // Already subscribed
            return
        }
        adbService.deviceListListener = { devices ->
            deviceListPanel.devices = devices
        }
    }

    private fun unsubscribeFromDeviceList() {
        if (adbService.deviceListListener == null) {
            // Already unsubscribed
            return
        }
        adbService.deviceListListener = null
    }

    private fun updateLogPanel(isLogVisible: Boolean) {
        if (isLogVisible) {
            splitter.secondComponent = bottomPanel

            logPanel.setLogEntries(adbService.commandHistory.getLogEntries())

            adbService.commandHistory.listener = object : CommandHistory.Listener {
                override fun onLogEntriesModified(entries: List<LogEntry>) {
                    logPanel.setLogEntries(entries)
                }
            }
        } else {
            splitter.secondComponent = null
        }
    }

    private companion object {
        const val DEFAULT_PANEL_PROPORTION = 0.6f
    }
}

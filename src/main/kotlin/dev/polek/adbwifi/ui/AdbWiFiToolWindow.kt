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
import dev.polek.adbwifi.LOG
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.ShellService

class AdbWiFiToolWindow(project: Project, private val toolWindow: ToolWindow) : BorderLayoutPanel(), Disposable {

    private val adbService by lazy { ServiceManager.getService(AdbService::class.java) }
    private val shellService by lazy { ServiceManager.getService(ShellService::class.java) }

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", DEFAULT_PANEL_PROPORTION)
    private val deviceListPanel = DeviceListPanel()
    private val shellPanel = ShellPanel()
    private val topPanel = JBScrollPane(deviceListPanel)
    private val bottomPanel = JBScrollPane(shellPanel)

    init {
        val actionManager = ActionManager.getInstance()
        val actionGroup = (actionManager.getAction("AdbWifi.ToolbarActions") as DefaultActionGroup)
        val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, true)
        toolbar.setTargetComponent(this)
        addToTop(toolbar.component)
        addToCenter(splitter)

        splitter.firstComponent = topPanel

        updateShellPanel(shellService.isShellVisible)
        shellService.shellVisibilityListener = { isVisible ->
            updateShellPanel(isVisible)
        }

        project.messageBus
            .connect(this)
            .subscribe(
                ToolWindowManagerListener.TOPIC,
                object : ToolWindowManagerListener {
                    override fun stateChanged(toolWindowManager: ToolWindowManager) {
                        LOG.warn("ToolWindow. isActive: ${toolWindow.isActive}, isVisible: ${toolWindow.isVisible}")
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
        shellService.shellVisibilityListener = null
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

    private fun updateShellPanel(isShellVisible: Boolean) {
        if (isShellVisible) {
            splitter.secondComponent = bottomPanel

            shellPanel.setLogEntries(adbService.commandHistory.getLogEntries())

            adbService.commandHistory.listener = object : CommandHistory.Listener {
                override fun onLogEntriesModified(entries: List<LogEntry>) {
                    shellPanel.setLogEntries(entries)
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

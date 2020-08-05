package dev.polek.adbwifi.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.Command
import dev.polek.adbwifi.model.CommandHistory
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.ShellService

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : BorderLayoutPanel() {

    private val adbService by lazy { ServiceManager.getService(AdbService::class.java) }
    private val shellService by lazy { ServiceManager.getService(ShellService::class.java) }

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", 0.6f)
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

        adbService.deviceListListener = { devices ->
            deviceListPanel.devices = devices
        }

        updateShellPanel(shellService.isShellVisible)
        shellService.shellVisibilityListener = { isVisible ->
            updateShellPanel(isVisible)
        }
    }

    private fun updateShellPanel(isShellVisible: Boolean) {
        if (isShellVisible) {
            splitter.secondComponent = bottomPanel
            adbService.commandHistory.listener = object : CommandHistory.Listener {
                override fun onCommandHistoryModified(commands: List<Command>) {
                    shellPanel.setCommands(commands)
                }
            }
        } else {
            splitter.secondComponent = null
        }
    }
}

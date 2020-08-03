package dev.polek.adbwifi.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.services.AdbService
import dev.polek.adbwifi.services.ShellService

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : BorderLayoutPanel() {

    private val splitter = JBSplitter(true, "AdbWifi.ShellPaneProportion", 0.6f)
    private val deviceListPanel = DeviceListPanel()
    private val shellPanel = JBScrollPane()

    init {
        val actionManager = ActionManager.getInstance()
        val actionGroup = (actionManager.getAction("AdbWifi.ToolbarActions") as DefaultActionGroup)
        val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, true)
        toolbar.setTargetComponent(this)
        addToTop(toolbar.component)

        splitter.firstComponent = JBScrollPane(deviceListPanel)

        addToCenter(splitter)

        val shellService = ServiceManager.getService(ShellService::class.java)
        updateShellPanel(shellService.isShellVisible)
        shellService.shellVisibilityListener = { isVisible ->
            updateShellPanel(isVisible)
        }

        val adbService = ServiceManager.getService(AdbService::class.java)
        adbService.deviceListListener = { devices ->
            deviceListPanel.devices = devices
        }
    }

    private fun updateShellPanel(isShellVisible: Boolean) {
        splitter.secondComponent = if (isShellVisible) shellPanel else null
    }
}

package dev.polek.adbwifi.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.model.Device
import dev.polek.adbwifi.services.AdbService

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : BorderLayoutPanel() {

    private val deviceListPanel = DeviceListPanel()

    init {
        val actionManager = ActionManager.getInstance()
        val actionGroup = (actionManager.getAction("AdbWifi.ToolbarActions") as DefaultActionGroup)
        val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, true)
        toolbar.setTargetComponent(this)
        addToTop(toolbar.component)

        addToCenter(JBScrollPane(deviceListPanel))

        val adbService = ServiceManager.getService(AdbService::class.java)
        adbService.deviceListListener = { devices ->
            deviceListPanel.devices = devices
        }
    }
}

package dev.polek.adbwifi.ui

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.model.Device
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : BorderLayoutPanel() {

    private val adb = Adb()
    private val deviceListPanel = DeviceListPanel()

    init {
        addToCenter(JBScrollPane(deviceListPanel))




        deviceListPanel.devices = listOf(
                Device("123", "Samsung", "192.168.1.101", false),
                Device("12345", "OnePlus", "192.168.1.102", true),
                Device("6789", "Pixel", "192.168.1.103", false),
                Device("1011", "Nexus", "192.168.1.104", false),
                Device("1213", "Nokia", "192.168.1.105", false),
                Device("1214", "LG", "192.168.1.106", false),
                Device("1215", "Motorola", "192.168.1.105", false),
                Device("1216", "Pixel 2", "192.168.1.105", false)
        )
    }
}

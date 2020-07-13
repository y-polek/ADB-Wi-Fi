package dev.polek.adbwifi.ui

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.table.JBTable
import dev.polek.adbwifi.MyBundle
import dev.polek.adbwifi.adb.Adb
import java.awt.BorderLayout
import javax.swing.JPanel

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : JPanel() {

    private val tableModel = DevicesTableModel()
    private val table = JBTable(tableModel)
    private val adb = Adb()

    init {
        layout = BorderLayout()
        add(table, BorderLayout.CENTER)

        table.emptyText.text = MyBundle.message("noConnectedDevices")


        tableModel.devices = adb.devices()
    }
}

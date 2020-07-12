package dev.polek.adbwifi.ui

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.table.JBTable
import dev.polek.adbwifi.model.Device
import java.awt.BorderLayout
import javax.swing.JPanel

class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : JPanel() {

    private val tableModel = DevicesTableModel()
    private val table = JBTable(tableModel)

    init {
        layout = BorderLayout()
        add(table, BorderLayout.CENTER)


        tableModel.devices = listOf(
                Device("3e853ff4", "ONE_A2001", "192.168.1.100"),
                Device("R28M51Y8E0H", "SM_G9700", "192.168.1.101"),
                Device("3", "SG-9700", "192.168.1.100")
        )
    }
}

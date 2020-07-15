package dev.polek.adbwifi.ui

import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.table.JBTable
import dev.polek.adbwifi.MyBundle
import dev.polek.adbwifi.adb.Adb
import dev.polek.adbwifi.model.Device
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JPanel


class AdbWiFiToolWindow(private val toolWindow: ToolWindow) : JPanel() {

    private val tableModel = DevicesTableModel()
    private val table = JBTable(tableModel).apply {
        emptyText.text = MyBundle.message("noConnectedDevices")
        //rowSelectionAllowed = false
        //cellSelectionEnabled = false
        getColumn(DevicesTableModel.Columns.CONNECTED_STATE.name).cellRenderer = ButtonCellRenderer()
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val table = this@apply
                val column = table.columnModel.getColumnIndexAtX(e.x)

                val row = e.y / table.rowHeight

                /*Checking the row or column is valid or not*/
                if (row < table.rowCount && row >= 0 && column < table.columnCount && column >= 0) {
                    val value = table.getValueAt(row, column)
                    (value as? JButton)?.doClick()
                }
            }
        })
    }
    private val adb = Adb()

    init {
        layout = BorderLayout()
        add(table, BorderLayout.CENTER)




        tableModel.devices = listOf(
                Device("123", "Samsung", "192.168.1.101", false),
                Device("12345", "OnePlus", "192.168.1.102", true)
        )
    }
}

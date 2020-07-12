package dev.polek.adbwifi.ui

import dev.polek.adbwifi.model.Device
import java.lang.RuntimeException
import javax.swing.table.AbstractTableModel

class DevicesTableModel : AbstractTableModel() {

    var devices: List<Device> = emptyList()
        set(value) {
            field = value
            fireTableDataChanged()
        }

    override fun getRowCount() = devices.size

    override fun getColumnCount() = 4

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val device = devices[rowIndex]
        return when (columnIndex) {
            0 -> device.name
            1 -> device.id
            2 -> device.address
            3 -> device.isConnected
            else -> throw RuntimeException("Unknown column #$columnIndex")
        }
    }

}

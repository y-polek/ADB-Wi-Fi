package dev.polek.adbwifi.ui

import dev.polek.adbwifi.model.Device
import java.lang.RuntimeException
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.table.AbstractTableModel

class DevicesTableModel : AbstractTableModel() {

    var devices: List<Device> = emptyList()
        set(value) {
            field = value
            fireTableDataChanged()
        }

    override fun getRowCount() = devices.size

    override fun getColumnCount() = Columns.values().size

    override fun getColumnName(column: Int): String {
        return Columns.values()[column].name
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val device = devices[rowIndex]
        return when (columnIndex) {
            0 -> device.name
            1 -> device.id
            2 -> device.address
            3 -> createButton(if (device.isConnected) "Disconnect" else "Connect", rowIndex)
            else -> throw RuntimeException("Unknown column #$columnIndex")
        }
    }

    private fun createButton(text: String, rowIndex: Int): JButton {
        val button = JButton(text)
        button.addActionListener {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(button), "Button clicked $rowIndex")
        }
        return button
    }

    enum class Columns {
        NAME,
        ID,
        ADDRESS,
        CONNECTED_STATE
    }
}

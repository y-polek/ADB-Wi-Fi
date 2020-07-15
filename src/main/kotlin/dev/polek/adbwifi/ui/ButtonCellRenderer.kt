package dev.polek.adbwifi.ui

import java.awt.Component
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.TableCellRenderer

class ButtonCellRenderer : TableCellRenderer {

    override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        return value as JButton
    }
}

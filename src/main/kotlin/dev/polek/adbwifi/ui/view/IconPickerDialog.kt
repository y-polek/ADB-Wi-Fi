package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.CommandIcon
import dev.polek.adbwifi.model.CommandIconsProvider
import dev.polek.adbwifi.model.IconColor
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer
import javax.swing.event.DocumentEvent

class IconPickerDialog(
    currentIconId: String? = null,
    currentIconColor: IconColor = IconColor.PRIMARY
) : DialogWrapper(true) {

    private val searchField = JBTextField()
    private val listModel = DefaultListModel<IconListItem>()
    private val iconList = JBList(listModel)
    private val colorButtons = mutableListOf<ColorButton>()

    var selectedIcon: CommandIcon? = null
        private set
    var selectedColor: IconColor = currentIconColor
        private set

    init {
        title = PluginBundle.message("iconPickerTitle")
        selectedIcon = currentIconId?.let { CommandIconsProvider.getIconById(it) }
        init()
        loadIcons("")
        selectCurrentIcon()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, JBUI.scale(8)))

        // Search field
        searchField.emptyText.text = PluginBundle.message("iconPickerSearchPlaceholder")
        searchField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                loadIcons(searchField.text)
            }
        })
        panel.add(searchField, BorderLayout.NORTH)

        // Icon list
        iconList.cellRenderer = IconListCellRenderer()
        iconList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val item = iconList.selectedValue
                if (item != null) {
                    selectedIcon = item.icon
                }
            }
        }
        iconList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && iconList.selectedIndex >= 0) {
                    doOKAction()
                }
            }
        })

        val scrollPane = JBScrollPane(iconList)
        scrollPane.preferredSize = Dimension(JBUI.scale(DIALOG_WIDTH), JBUI.scale(DIALOG_HEIGHT))
        panel.add(scrollPane, BorderLayout.CENTER)

        // Color selector row
        val colorPanel = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(4), 0))
        colorPanel.add(JBLabel(PluginBundle.message("iconPickerColorLabel")))
        for (color in IconColor.entries) {
            val button = ColorButton(color, color == selectedColor)
            button.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    selectColor(color)
                }
            })
            colorButtons.add(button)
            colorPanel.add(button)
        }
        panel.add(colorPanel, BorderLayout.SOUTH)

        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = searchField

    private fun loadIcons(query: String) {
        listModel.clear()

        // "No icon" item when not searching
        if (query.isBlank()) {
            listModel.addElement(IconListItem(null))
        }

        // Icon items
        val icons = CommandIconsProvider.search(query)
        for (icon in icons) {
            listModel.addElement(IconListItem(icon))
        }
    }

    private fun selectCurrentIcon() {
        for (i in 0 until listModel.size()) {
            val item = listModel.getElementAt(i)
            if (item.icon?.id == selectedIcon?.id) {
                iconList.selectedIndex = i
                iconList.ensureIndexIsVisible(i)
                return
            }
        }
        // If no icon is selected, select "No icon" (first item)
        if (selectedIcon == null && listModel.size() > 0) {
            iconList.selectedIndex = 0
        }
    }

    private fun selectColor(color: IconColor) {
        selectedColor = color
        for (button in colorButtons) {
            button.isColorSelected = button.iconColor == color
        }
        iconList.repaint()
    }

    private data class IconListItem(val icon: CommandIcon?)

    private inner class IconListCellRenderer : ListCellRenderer<IconListItem> {
        private val panel = JPanel(FlowLayout(FlowLayout.LEFT, JBUI.scale(8), 0)).apply {
            isOpaque = true
            border = JBUI.Borders.empty(2, 4)
        }
        private val iconLabel = JBLabel()
        private val nameLabel = JBLabel()

        init {
            panel.add(iconLabel)
            panel.add(nameLabel)
        }

        override fun getListCellRendererComponent(
            list: JList<out IconListItem>,
            value: IconListItem?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val bg = if (isSelected) list.selectionBackground else list.background
            val fg = if (isSelected) list.selectionForeground else list.foreground

            panel.background = bg
            nameLabel.foreground = fg

            if (value?.icon != null) {
                iconLabel.icon = CommandIconsProvider.getIcon(value.icon.id, selectedColor)
                nameLabel.text = value.icon.displayName
            } else {
                iconLabel.icon = null
                nameLabel.text = PluginBundle.message("iconPickerNoIcon")
            }

            return panel
        }
    }

    private class ColorButton(
        val iconColor: IconColor,
        initialSelected: Boolean
    ) : JPanel() {
        var isColorSelected: Boolean = initialSelected
            set(value) {
                field = value
                repaint()
            }

        init {
            preferredSize = Dimension(JBUI.scale(COLOR_BUTTON_SIZE), JBUI.scale(COLOR_BUTTON_SIZE))
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isOpaque = false
            toolTipText = iconColor.displayName
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val w = width.toDouble()
            val h = height.toDouble()
            val diameter = minOf(w, h)

            if (isColorSelected) {
                // Smaller dot with ring
                val dotDiameter = diameter * 0.5
                val dotX = (w - dotDiameter) / 2.0
                val dotY = (h - dotDiameter) / 2.0
                g2.color = iconColor.color
                g2.fill(Ellipse2D.Double(dotX, dotY, dotDiameter, dotDiameter))

                // Ring
                val strokeWidth = JBUI.scale(1).toFloat()
                val ringDiameter = diameter - strokeWidth
                val ringX = (w - ringDiameter) / 2.0
                val ringY = (h - ringDiameter) / 2.0
                g2.stroke = java.awt.BasicStroke(strokeWidth)
                g2.draw(Ellipse2D.Double(ringX, ringY, ringDiameter, ringDiameter))
            } else {
                // Plain dot
                val dotDiameter = diameter * 0.7
                val dotX = (w - dotDiameter) / 2.0
                val dotY = (h - dotDiameter) / 2.0
                g2.color = iconColor.color
                g2.fill(Ellipse2D.Double(dotX, dotY, dotDiameter, dotDiameter))
            }

            g2.dispose()
        }
    }

    private companion object {
        private const val COLOR_BUTTON_SIZE = 24
        private const val DIALOG_WIDTH = 300
        private const val DIALOG_HEIGHT = 350
    }
}

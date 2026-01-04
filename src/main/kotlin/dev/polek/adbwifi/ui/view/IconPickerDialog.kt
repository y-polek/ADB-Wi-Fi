package dev.polek.adbwifi.ui.view

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.model.ActionIcon
import dev.polek.adbwifi.model.ActionIconsProvider
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentEvent

class IconPickerDialog(
    private val currentIconId: String? = null
) : DialogWrapper(true) {

    private val searchField = JBTextField()
    private val listModel = DefaultListModel<ActionIcon?>()
    private val iconList = JBList(listModel)

    var selectedIcon: ActionIcon? = null
        private set

    init {
        title = PluginBundle.message("iconPickerTitle")
        init()
        loadIcons("")
        selectCurrentIcon()
    }

    private fun selectCurrentIcon() {
        if (currentIconId.isNullOrEmpty()) {
            iconList.selectedIndex = 0 // Select "No icon"
        } else {
            ActionIconsProvider.getIconById(currentIconId)?.let { icon ->
                iconList.setSelectedValue(icon, true)
            }
        }
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
        iconList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        iconList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    doOKAction()
                }
            }
        })

        val scrollPane = JBScrollPane(iconList)
        scrollPane.preferredSize = Dimension(JBUI.scale(400), JBUI.scale(300))
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = searchField

    override fun doOKAction() {
        selectedIcon = iconList.selectedValue
        super.doOKAction()
    }

    private fun loadIcons(query: String) {
        val icons = ActionIconsProvider.search(query)
        listModel.clear()
        // Add "No icon" option at the top (only when not searching or search is empty)
        if (query.isBlank()) {
            listModel.addElement(null)
        }
        icons.forEach { listModel.addElement(it) }
    }

    private inner class IconListCellRenderer : ListCellRenderer<ActionIcon?> {
        private val panel = JPanel(BorderLayout(JBUI.scale(8), 0))
        private val iconLabel = JBLabel()
        private val textLabel = JBLabel()

        init {
            panel.border = JBUI.Borders.empty(4, 8)
            panel.isOpaque = true
            iconLabel.isOpaque = false
            textLabel.isOpaque = false
            panel.add(iconLabel, BorderLayout.WEST)
            panel.add(textLabel, BorderLayout.CENTER)
        }

        override fun getListCellRendererComponent(
            list: JList<out ActionIcon?>,
            value: ActionIcon?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            if (isSelected) {
                panel.background = list.selectionBackground
                textLabel.foreground = list.selectionForeground
            } else {
                panel.background = list.background
                textLabel.foreground = list.foreground
            }

            if (value != null) {
                iconLabel.icon = value.icon
                textLabel.text = value.displayName
            } else {
                iconLabel.icon = null
                textLabel.text = PluginBundle.message("iconPickerNoIcon")
            }

            return panel
        }
    }
}

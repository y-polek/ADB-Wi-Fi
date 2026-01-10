package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.Colors
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class DeviceListPanel(
    private val devicePanelListener: DevicePanel.Listener,
    showHeader: Boolean = false,
    private val title: String? = null,
    isHeaderExpanded: Boolean = true,
    private val onHeaderExpandChanged: ((isExpanded: Boolean) -> Unit)? = null
) : JBPanel<DeviceListPanel>() {

    var devices: List<DeviceViewModel> = emptyList()
        set(value) {
            field = value
            rebuildUi()
        }

    private var isExpanded: Boolean = isHeaderExpanded
        set(value) {
            field = value
            rebuildUi()
            onHeaderExpandChanged?.invoke(value)
        }

    private var header: JPanel? = null
    private var headerLabel: JBLabel? = null
    private var headerIcon: JBLabel? = null
    private var contentPanel: JPanel? = null

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        background = Colors.PANEL_BACKGROUND
        border = JBUI.Borders.empty(0, HORIZONTAL_PADDING)

        if (showHeader) {
            buildHeader()
        }

        // Content panel for device cards
        contentPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
            isOpaque = false
            alignmentX = LEFT_ALIGNMENT
        }
        add(contentPanel)

        rebuildUi()
    }

    private fun buildHeader() {
        val headerPanel = JPanel(GridBagLayout())
        headerPanel.isOpaque = false
        headerPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        headerPanel.border = JBUI.Borders.empty(HEADER_VERTICAL_PADDING, 0)
        this.header = headerPanel

        headerLabel = JBLabel().apply {
            foreground = Colors.SECONDARY_TEXT
            font = font.deriveFont(java.awt.Font.BOLD, 12f)
        }
        headerPanel.add(
            headerLabel!!,
            GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
            }
        )

        headerIcon = JBLabel(ICON_EXPANDED)
        headerIcon!!.foreground = Colors.SECONDARY_TEXT
        headerPanel.add(
            headerIcon!!,
            GridBagConstraints().apply {
                gridx = 1
                gridy = 0
            }
        )

        headerPanel.minimumSize = Dimension(0, HEADER_HEIGHT)
        headerPanel.maximumSize = Dimension(Int.MAX_VALUE, HEADER_HEIGHT)
        headerPanel.preferredSize = Dimension(0, HEADER_HEIGHT)
        headerPanel.alignmentX = LEFT_ALIGNMENT
        add(headerPanel)

        headerPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                isExpanded = !isExpanded
            }
        })
    }

    private fun rebuildUi() {
        headerLabel?.text = "$title (${devices.size})"
        headerIcon?.icon = if (isExpanded) ICON_EXPANDED else ICON_COLLAPSED

        contentPanel?.let { panel ->
            panel.removeAll()

            if (isExpanded) {
                devices.forEachIndexed { index, device ->
                    if (index > 0) {
                        panel.add(Box.createVerticalStrut(CARD_GAP))
                    }
                    val devicePanel = DevicePanel(device)
                    devicePanel.listener = devicePanelListener
                    devicePanel.alignmentX = LEFT_ALIGNMENT
                    devicePanel.maximumSize = Dimension(Int.MAX_VALUE, devicePanel.preferredSize.height)
                    panel.add(devicePanel)
                }
            }
        }

        revalidate()
        repaint()
    }

    private companion object {
        private const val HORIZONTAL_PADDING = 8
        private const val HEADER_HEIGHT = 36
        private const val HEADER_VERTICAL_PADDING = 8
        private const val CARD_GAP = 8

        private val ICON_EXPANDED = AllIcons.General.ChevronUp
        private val ICON_COLLAPSED = AllIcons.General.ChevronDown
    }
}

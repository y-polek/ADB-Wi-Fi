package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.PluginBundle
import dev.polek.adbwifi.ui.model.DeviceViewModel
import dev.polek.adbwifi.utils.Colors
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.RenderingHints
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
    private val onHeaderExpandChanged: ((isExpanded: Boolean) -> Unit)? = null,
    private val onClearClicked: (() -> Unit)? = null
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
        val headerPanel = object : JPanel(GridBagLayout()) {
            var isHovered = false

            override fun paintComponent(g: Graphics) {
                val g2 = g.create() as Graphics2D
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

                // Draw rounded background
                g2.color = if (isHovered) Colors.BUTTON_HOVER_BG else Colors.ICON_BUTTON_BG
                g2.fillRoundRect(0, 0, width, height, HEADER_CORNER_RADIUS * 2, HEADER_CORNER_RADIUS * 2)

                // Draw border
                g2.color = Colors.CARD_BORDER
                g2.drawRoundRect(0, 0, width - 1, height - 1, HEADER_CORNER_RADIUS * 2, HEADER_CORNER_RADIUS * 2)

                g2.dispose()
                super.paintComponent(g)
            }
        }
        headerPanel.isOpaque = false
        headerPanel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        headerPanel.border = JBUI.Borders.empty(0, HEADER_HORIZONTAL_PADDING)
        this.header = headerPanel

        headerLabel = JBLabel().apply {
            foreground = Colors.PRIMARY_TEXT
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

        // Clear link
        if (onClearClicked != null) {
            val clearLink = HyperlinkLabel(PluginBundle.message("clearPreviouslyConnectedButton"))
            clearLink.addHyperlinkListener {
                onClearClicked.invoke()
            }
            headerPanel.add(
                clearLink,
                GridBagConstraints().apply {
                    gridx = 1
                    gridy = 0
                    insets = JBUI.insets(0, 8)
                }
            )
        }

        headerIcon = JBLabel(ICON_EXPANDED)
        headerPanel.add(
            headerIcon!!,
            GridBagConstraints().apply {
                gridx = 2
                gridy = 0
            }
        )

        headerPanel.minimumSize = Dimension(0, HEADER_HEIGHT)
        headerPanel.maximumSize = Dimension(Int.MAX_VALUE, HEADER_HEIGHT)
        headerPanel.preferredSize = Dimension(0, HEADER_HEIGHT)
        headerPanel.alignmentX = LEFT_ALIGNMENT

        // Wrap in a container to add vertical margin
        val headerContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentX = LEFT_ALIGNMENT
            add(Box.createVerticalStrut(HEADER_VERTICAL_MARGIN))
            add(headerPanel)
        }
        add(headerContainer)

        headerPanel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                isExpanded = !isExpanded
            }

            override fun mouseEntered(e: MouseEvent) {
                headerPanel.isHovered = true
                headerPanel.repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                headerPanel.isHovered = false
                headerPanel.repaint()
            }
        })
    }

    private fun rebuildUi() {
        headerLabel?.text = "$title (${devices.size})"
        headerIcon?.icon = if (isExpanded) ICON_EXPANDED else ICON_COLLAPSED

        contentPanel?.let { panel ->
            panel.removeAll()

            if (isExpanded && devices.isNotEmpty()) {
                panel.add(Box.createVerticalStrut(TOP_PADDING))
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
                panel.add(Box.createVerticalStrut(BOTTOM_PADDING))
            }
        }

        revalidate()
        repaint()
    }

    private companion object {
        private const val HORIZONTAL_PADDING = 8
        private const val TOP_PADDING = 8
        private const val BOTTOM_PADDING = 8
        private const val HEADER_HEIGHT = 32
        private const val HEADER_CORNER_RADIUS = 5
        private const val HEADER_HORIZONTAL_PADDING = 10
        private const val HEADER_VERTICAL_MARGIN = 8
        private const val CARD_GAP = 8

        private val ICON_EXPANDED = AllIcons.General.ChevronUp
        private val ICON_COLLAPSED = AllIcons.General.ChevronDown
    }
}

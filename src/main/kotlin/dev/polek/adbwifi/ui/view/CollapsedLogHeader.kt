package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.ui.JBUI
import dev.polek.adbwifi.utils.Colors
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class CollapsedLogHeader(
    private val onExpandClicked: () -> Unit
) : JPanel(BorderLayout()) {

    init {
        background = Colors.CARD_BACKGROUND
        border = JBUI.Borders.customLine(Colors.CARD_BORDER, 1, 0, 0, 0)
        preferredSize = Dimension(0, HEADER_HEIGHT)
        minimumSize = Dimension(0, HEADER_HEIGHT)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        // Left side: Icon and "Log" label
        val leftPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.emptyLeft(HORIZONTAL_PADDING)
        }

        val iconLabel = JLabel(AllIcons.Debugger.Console).apply {
            border = JBUI.Borders.emptyRight(ICON_TEXT_GAP)
        }

        val textLabel = JLabel("Log").apply {
            font = font.deriveFont(Font.PLAIN, 12f)
            foreground = Colors.PRIMARY_TEXT
        }

        val tabContent = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(iconLabel, BorderLayout.WEST)
            add(textLabel, BorderLayout.CENTER)
        }
        leftPanel.add(tabContent, BorderLayout.WEST)
        add(leftPanel, BorderLayout.WEST)

        // Right side: Expand button
        val actionManager = ActionManager.getInstance()
        val expandActionGroup = actionManager.getAction("AdbWifi.ExpandLogActions") as? DefaultActionGroup
        if (expandActionGroup != null) {
            val expandToolbar = actionManager.createActionToolbar(
                ActionPlaces.TOOLWINDOW_CONTENT,
                expandActionGroup,
                true
            )
            expandToolbar.targetComponent = this
            expandToolbar.component.isOpaque = false

            val rightWrapper = JPanel(BorderLayout()).apply {
                isOpaque = false
                border = JBUI.Borders.empty()
                add(expandToolbar.component, BorderLayout.EAST)
            }
            add(rightWrapper, BorderLayout.EAST)
        }

        // Click anywhere on the header to expand
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onExpandClicked()
            }
        })
    }

    private companion object {
        private const val HEADER_HEIGHT = 36
        private const val HORIZONTAL_PADDING = 12
        private const val ICON_TEXT_GAP = 8
    }
}

package dev.polek.adbwifi.ui.view

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.LogEntry
import dev.polek.adbwifi.utils.Colors
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities

class LogPanel : BorderLayoutPanel() {

    private var wrapContent: Boolean = true

    private val textPane = object : JTextPane() {
        override fun getScrollableTracksViewportWidth(): Boolean {
            return wrapContent
        }
    }.apply {
        isEditable = false
        contentType = "text/html"
        background = Colors.PANEL_BACKGROUND
        border = JBUI.Borders.empty(CONTENT_INSET)
    }

    private val scrollPane = JBScrollPane(textPane).apply {
        border = null
        viewport.background = Colors.PANEL_BACKGROUND
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
    }

    private val headerPanel = createHeaderPanel()

    private var currentEntries: List<LogEntry> = emptyList()

    init {
        background = Colors.PANEL_BACKGROUND
        border = JBUI.Borders.empty()
        addToTop(headerPanel)
        addToCenter(scrollPane)
    }

    private fun createHeaderPanel(): JPanel {
        val panel = JPanel(BorderLayout()).apply {
            background = Colors.CARD_BACKGROUND
            border = JBUI.Borders.customLine(Colors.CARD_BORDER, 1, 0, 1, 0)
            preferredSize = Dimension(0, HEADER_HEIGHT)
            minimumSize = Dimension(0, HEADER_HEIGHT)
        }

        // Left side: Tab with icon and "Log" label
        val tabPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty(0, TAB_HORIZONTAL_PADDING)
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
        tabPanel.add(tabContent, BorderLayout.CENTER)

        val leftWrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty()
            add(tabPanel, BorderLayout.WEST)
        }
        panel.add(leftWrapper, BorderLayout.WEST)

        // Right side: Action buttons
        val actionManager = ActionManager.getInstance()
        val logToolbarActionGroup = actionManager.getAction("AdbWifi.LogToolbarActions") as DefaultActionGroup
        val logToolbar = actionManager.createActionToolbar(
            ActionPlaces.TOOLWINDOW_CONTENT,
            logToolbarActionGroup,
            true
        )
        logToolbar.targetComponent = this
        logToolbar.component.isOpaque = false

        val rightWrapper = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty()
            add(logToolbar.component, BorderLayout.EAST)
        }
        panel.add(rightWrapper, BorderLayout.EAST)

        return panel
    }

    fun setLogEntries(entries: List<LogEntry>) {
        currentEntries = entries
        updateContent()
    }

    fun setWrapContent(wrap: Boolean) {
        if (wrapContent != wrap) {
            wrapContent = wrap
            // Revalidate the text pane so scrollbar updates
            textPane.revalidate()
            updateContent()
        }
    }

    private fun updateContent() {
        textPane.text = html(currentEntries, wrapContent)
        // Auto-scroll to bottom (vertically only)
        SwingUtilities.invokeLater {
            val viewport = scrollPane.viewport
            val viewSize = viewport.viewSize
            val extentSize = viewport.extentSize
            viewport.viewPosition = java.awt.Point(0, maxOf(0, viewSize.height - extentSize.height))
        }
    }

    private companion object {
        private const val CONTENT_INSET = 12
        private const val HEADER_HEIGHT = 36
        private const val TAB_HORIZONTAL_PADDING = 12
        private const val ICON_TEXT_GAP = 8

        private val COMMAND_COLOR = toHex(Colors.SECONDARY_TEXT)
        private val OUTPUT_COLOR = toHex(Colors.PRIMARY_TEXT)

        private fun toHex(color: java.awt.Color): String {
            return String.format(java.util.Locale.US, "#%02x%02x%02x", color.red, color.green, color.blue)
        }

        private fun html(entries: List<LogEntry>, wrapContent: Boolean): String {
            val fontFamily = JBUI.Fonts.label().family
            val whiteSpace = if (wrapContent) "pre-wrap" else "pre"
            return """
                <html>
                    <head>
                        <style>
                            body {
                                font-family: monospace, '$fontFamily';
                                font-size: 9px;
                                line-height: 1.6;
                                margin: 0;
                                padding: 0;
                                white-space: $whiteSpace;
                            }
                            .command {
                                color: $COMMAND_COLOR;
                            }
                            .output {
                                color: $OUTPUT_COLOR;
                            }
                        </style>
                    </head>
                    <body>
                        ${entries.joinToString(separator = "") { entryHtml(it, wrapContent) }}
                    </body>
                </html>
            """.trimIndent()
        }

        private fun entryHtml(entry: LogEntry, wrapContent: Boolean) = buildString {
            when (entry) {
                is LogEntry.Command -> {
                    val text = "$ ${escapeHtml(entry.text)}"
                    if (wrapContent) {
                        appendLine("<div class=\"command\">$text</div>")
                    } else {
                        appendLine("<div class=\"command\"><nobr>$text</nobr></div>")
                    }
                }
                is LogEntry.Output -> {
                    if (entry.text.isNotBlank()) {
                        if (wrapContent) {
                            val formattedText = escapeHtml(entry.text).replace("\n", "<br/>")
                            appendLine("<div class=\"output\">$formattedText</div>")
                        } else {
                            // Wrap each line in <nobr> to prevent wrapping
                            val lines = entry.text.split("\n")
                            val formattedText = lines.joinToString("<br/>") { "<nobr>${escapeHtml(it)}</nobr>" }
                            appendLine("<div class=\"output\">$formattedText</div>")
                        }
                    }
                }
            }
        }

        private fun escapeHtml(text: String): String {
            return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
        }
    }
}

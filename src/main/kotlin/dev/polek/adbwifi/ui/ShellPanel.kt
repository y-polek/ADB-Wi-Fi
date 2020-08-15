package dev.polek.adbwifi.ui

import com.intellij.ui.JBColor
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.LogEntry
import javax.swing.JTextPane
import javax.swing.border.EmptyBorder

class ShellPanel : BorderLayoutPanel() {

    private val textPane = JTextPane().apply {
        isEditable = false
        contentType = "text/html"
        background = JBColor.background()
        border = EmptyBorder(INSET, INSET, INSET, INSET)
    }

    init {
        addToCenter(textPane)
    }

    fun setLogEntries(entries: List<LogEntry>) {
        textPane.text = html(entries)
    }

    private companion object {
        private const val INSET = 10

        private fun html(entries: List<LogEntry>): String {
            return """
                <html>
                    <head>
                        <style>
                        </style>
                    </head>
                    <body>
                        <code>
                            ${entries.joinToString(separator = "", transform = ::commandHtml)}
                        </code>
                    </body>
                </html>
            """.trimIndent()
        }

        private fun commandHtml(entry: LogEntry) = buildString {
            when (entry) {
                is LogEntry.Command -> {
                    appendln("<b>> ${entry.text}</b>")
                    appendln("<br/>")
                }
                is LogEntry.Output -> {
                    if (entry.text.isNotBlank()) {
                        appendln(entry.text)
                        appendln("<br/>")
                    }
                }
            }
        }
    }
}

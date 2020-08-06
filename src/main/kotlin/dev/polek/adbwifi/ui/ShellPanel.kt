package dev.polek.adbwifi.ui

import com.intellij.ui.JBColor
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.Command
import javax.swing.JTextPane
import javax.swing.border.EmptyBorder

class ShellPanel : BorderLayoutPanel() {

    private val textPane = JTextPane().apply {
        isEditable = false
        contentType = "text/html"
        background = JBColor.background()
        border = EmptyBorder(10, 10, 10, 10)
    }

    init {
        addToCenter(textPane)
    }

    fun setCommands(commands: List<Command>) {
        textPane.text = html(commands)
    }

    private companion object {
        private fun html(commands: List<Command>): String {
            return """
                <html>
                    <head>
                        <style>
                        </style>
                    </head>
                    <body>
                        <code>
                            ${commands.joinToString(separator = "", transform = ::commandHtml)}
                        </code>
                    </body>
                </html>
            """.trimIndent()
        }

        private fun commandHtml(command: Command) = buildString {
            appendln("<b>> ${command.command}</b>")
            appendln("<br/>")
            if (command.output.isNotBlank()) {
                appendln(command.output)
                appendln("<br/>")
            }
        }
    }
}

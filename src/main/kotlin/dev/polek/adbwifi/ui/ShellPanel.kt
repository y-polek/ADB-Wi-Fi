package dev.polek.adbwifi.ui

import com.intellij.ui.JBColor
import com.intellij.util.ui.components.BorderLayoutPanel
import dev.polek.adbwifi.model.Command
import javax.swing.JTextPane

class ShellPanel : BorderLayoutPanel() {

    private val textPane = JTextPane().apply {
        isEditable = false
        background = JBColor.background()
    }

    init {
        addToCenter(textPane)
    }

    fun setCommands(commands: List<Command>) {
        textPane.text = commands.flatMap { command ->
            listOf("> ${command.command}", command.output)
        }.joinToString(separator = "\n")
    }
}

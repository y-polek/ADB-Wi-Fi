package dev.polek.adbwifi.utils

import com.intellij.ui.HyperlinkLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Component
import java.awt.Container
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

fun panel(
    center: JComponent? = null,
    left: JComponent? = null,
    top: JComponent? = null,
    right: JComponent? = null,
    bottom: JComponent? = null
): JPanel {
    val panel = BorderLayoutPanel()
    panel.isOpaque = false
    center?.let(panel::addToCenter)
    left?.let(panel::addToLeft)
    top?.let(panel::addToTop)
    right?.let(panel::addToRight)
    bottom?.let(panel::addToBottom)
    return panel
}

fun flowPanel(vararg components: JComponent, hgap: Int = 5, vgap: Int = 0): JPanel {
    val panel = FlowLayoutPanel(hgap = hgap, vgap = vgap)
    components.forEach { component ->
        panel.add(component)
    }
    return panel
}

fun JLabel.makeBold() {
    val oldFont = this.font
    this.font = oldFont.deriveFont(oldFont.style or Font.BOLD)
}

fun JTextField.makeMonospaced() {
    val oldFont = this.font
    this.font = JBUI.Fonts.create(Font.MONOSPACED, oldFont.size)
}

fun JLabel.setFontSize(size: Float) {
    val oldFont = this.font
    this.font = oldFont.deriveFont(size)
}

fun HyperlinkLabel.setFontSize(size: Float) {
    val oldFont = this.font
    this.font = oldFont.deriveFont(size)
}

inline fun Container.removeIf(shouldRemove: (child: Component) -> Boolean) {
    for (child in this.components) {
        if (shouldRemove(child)) {
            this.remove(child)
        }
    }
}

fun JTextComponent.onTextChanged(action: () -> Unit) {
    document.addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) = action()
        override fun removeUpdate(e: DocumentEvent) = action()
        override fun changedUpdate(e: DocumentEvent) = action()
    })
}

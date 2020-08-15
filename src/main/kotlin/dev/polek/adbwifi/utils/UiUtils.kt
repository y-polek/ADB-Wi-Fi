package dev.polek.adbwifi.utils

import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

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

fun JLabel.makeBold() {
    val oldFont = this.font
    this.font = oldFont.deriveFont(oldFont.style or Font.BOLD)
}

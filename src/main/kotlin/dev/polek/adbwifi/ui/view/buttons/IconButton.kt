package dev.polek.adbwifi.ui.view.buttons

import dev.polek.adbwifi.utils.Colors
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.JButton

/**
 * A square button that displays only an icon.
 */
class IconButton(
    private val icon: Icon,
    tooltip: String? = null,
    private val showBorder: Boolean = true
) : JButton() {

    init {
        preferredSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
        minimumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
        maximumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
        isOpaque = false
        isContentAreaFilled = false
        isFocusPainted = false
        isBorderPainted = false
        isRolloverEnabled = true
        toolTipText = tooltip
        background = Colors.ICON_BUTTON_BG
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (showBorder) {
            g2.color = when {
                model.isPressed -> Colors.BUTTON_HOVER_BG
                model.isRollover -> Colors.BUTTON_HOVER_BG
                else -> background
            }
            g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

            g2.color = Colors.CARD_BORDER
            g2.drawRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2)
        }

        // Draw icon centered
        val iconX = (width - icon.iconWidth) / 2
        val iconY = (height - icon.iconHeight) / 2
        icon.paintIcon(this, g2, iconX, iconY)

        g2.dispose()
    }

    private companion object {
        private const val ICON_BUTTON_SIZE = 32
        private const val CORNER_RADIUS = 5
    }
}

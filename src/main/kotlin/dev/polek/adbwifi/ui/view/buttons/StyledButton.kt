package dev.polek.adbwifi.ui.view.buttons

import dev.polek.adbwifi.utils.Colors
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.Icon
import javax.swing.JButton

/**
 * A styled button with customizable colors and optional border.
 */
class StyledButton(
    text: String,
    private val style: Style,
    private val icon: Icon? = null
) : JButton(text) {

    init {
        preferredSize = Dimension(0, BUTTON_HEIGHT)
        minimumSize = Dimension(0, BUTTON_HEIGHT)
        maximumSize = Dimension(Int.MAX_VALUE, BUTTON_HEIGHT)
        isOpaque = false
        isContentAreaFilled = false
        isFocusPainted = false
        isBorderPainted = false
        isRolloverEnabled = true
        background = style.backgroundColor
        foreground = style.textColor
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        if (isEnabled) {
            // Draw background with hover/pressed states
            g2.color = when {
                model.isPressed -> style.hoverColor
                model.isRollover -> style.hoverColor
                else -> background
            }
            g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

            // Draw border if specified
            style.borderColor?.let { borderColor ->
                g2.color = borderColor
                g2.drawRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2)
            }

            g2.color = foreground
        } else {
            // Draw grayed out background
            g2.color = Colors.DISABLED_BUTTON_BG
            g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

            g2.color = Colors.SECONDARY_TEXT
        }

        g2.font = font
        val fm = g2.fontMetrics
        val textWidth = fm.stringWidth(text)

        if (icon != null) {
            // Draw icon + text centered
            val iconWidth = icon.iconWidth
            val totalWidth = iconWidth + ICON_TEXT_GAP + textWidth
            val startX = (width - totalWidth) / 2

            val iconY = (height - icon.iconHeight) / 2
            icon.paintIcon(this, g2, startX, iconY)

            val textX = startX + iconWidth + ICON_TEXT_GAP
            val textY = (height - fm.height) / 2 + fm.ascent
            g2.drawString(text, textX, textY)
        } else {
            // Draw text only, centered
            val x = (width - textWidth) / 2
            val y = (height - fm.height) / 2 + fm.ascent
            g2.drawString(text, x, y)
        }

        g2.dispose()
    }

    /**
     * Button style configuration.
     */
    data class Style(
        val backgroundColor: Color,
        val hoverColor: Color,
        val textColor: Color,
        val borderColor: Color? = null
    ) {
        companion object {
            /** Green primary button style */
            val PRIMARY = Style(
                backgroundColor = Colors.GREEN_BUTTON_BG,
                hoverColor = Colors.GREEN_BUTTON_BG.darker(),
                textColor = Colors.GREEN_BUTTON_TEXT
            )

            /** Secondary button with border */
            val SECONDARY = Style(
                backgroundColor = Colors.ICON_BUTTON_BG,
                hoverColor = Colors.BUTTON_HOVER_BG,
                textColor = Colors.PRIMARY_TEXT,
                borderColor = Colors.CARD_BORDER
            )

            /** Red disconnect button style */
            val DANGER = Style(
                backgroundColor = Colors.RED_BUTTON_BG,
                hoverColor = Colors.RED_BUTTON_BORDER,
                textColor = Colors.RED_BUTTON_TEXT,
                borderColor = Colors.RED_BUTTON_BORDER
            )
        }
    }

    private companion object {
        private const val BUTTON_HEIGHT = 32
        private const val CORNER_RADIUS = 5
        private const val ICON_TEXT_GAP = 8
    }
}

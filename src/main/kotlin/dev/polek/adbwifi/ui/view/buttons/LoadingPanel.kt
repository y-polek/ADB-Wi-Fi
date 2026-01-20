package dev.polek.adbwifi.ui.view.buttons

import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.JBLabel
import dev.polek.adbwifi.utils.Colors
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridBagLayout
import java.awt.RenderingHints
import javax.swing.JPanel

/**
 * A panel that displays a loading spinner with customizable size and style.
 */
class LoadingPanel(
    private val size: Size
) : JPanel(GridBagLayout()) {

    private val spinnerIcon = AnimatedIcon.Default()

    init {
        when (size) {
            Size.FULL_WIDTH -> {
                preferredSize = Dimension(0, BUTTON_HEIGHT)
                minimumSize = Dimension(0, BUTTON_HEIGHT)
            }
            Size.ICON -> {
                preferredSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                minimumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
                maximumSize = Dimension(ICON_BUTTON_SIZE, ICON_BUTTON_SIZE)
            }
        }
        isOpaque = false
        add(JBLabel(spinnerIcon))
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        when (size) {
            Size.FULL_WIDTH -> {
                // Draw grayed out background (same as disabled button)
                g2.color = Colors.DISABLED_BUTTON_BG
                g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)
            }
            Size.ICON -> {
                // Draw background with border (same style as icon buttons)
                g2.color = Colors.ICON_BUTTON_BG
                g2.fillRoundRect(0, 0, width, height, CORNER_RADIUS * 2, CORNER_RADIUS * 2)

                g2.color = Colors.CARD_BORDER
                g2.drawRoundRect(0, 0, width - 1, height - 1, CORNER_RADIUS * 2, CORNER_RADIUS * 2)
            }
        }

        g2.dispose()
        super.paintComponent(g)
    }

    enum class Size {
        /** Full-width loading panel for main button area */
        FULL_WIDTH,

        /** Icon-sized loading panel for icon button area */
        ICON
    }

    private companion object {
        private const val BUTTON_HEIGHT = 32
        private const val ICON_BUTTON_SIZE = 32
        private const val CORNER_RADIUS = 5
    }
}
